/*  Copyright (C) 2012  Nicholas Wright
	
	part of 'Aid', an imageboard downloader.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package app;

import filter.FilterUpdateDaemon;
import filter.LastModCheck;
import gui.Aid;
import gui.BlockList;
import gui.BlockListDataModel;
import gui.BoardListDataModel;
import gui.Filterlist;
import gui.Stats;
import io.AidDAO;
import io.CachePruneDaemon;
import io.FileWriter;
import io.ImageLoader;
import io.ThumbnailLoader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import board.Board;
import board.FourChanStrategy;
import board.SiteStrategy;

import com.github.dozedoff.commonj.gui.Log;
import com.github.dozedoff.commonj.io.BoneConnectionPool;
import com.github.dozedoff.commonj.io.ConnectionPool;

import config.AppSetting;
import config.DefaultAppSettings;
import config.DefaultMySQLconnection;

/**
 * This is a Image board downloader that will check for new files at regular intervals
 */
public class Main implements ActionListener{
	//TODO add a setting file (window pos, ect.)
	//TODO add favorite tag to folders (keywords)

	String saveDirectory = "";				// Default Drive
	String defaultDirectory = null;

	final int STARTUP_DELAY = 7; // delay between board starts when using "Start All", in minutes

	boolean SkipLogEnabled = false;

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private PropertyChangeSupport change = new PropertyChangeSupport(this);

	private io.FileWriter fileWriter;
	private filter.Filter filter;
	private ImageLoader imageLoader;

	private Aid aid;
	private Filterlist filterlist;
	private BlockList blockList;
	private BlockListDataModel blockListModel;
	private ThumbnailLoader thumbLoader;
	private ConnectionPool connPool;
	private AidDAO mySQL;
	private SiteStrategy strategy;
	private LastModCheck lastModCheck;

	private BoardListDataModel boards = new BoardListDataModel();
	Properties appSettings = new DefaultAppSettings();
	Properties sqlProps = new DefaultMySQLconnection();

	private final String PWD = System.getProperty("user.dir");
	private final String MYSQL_CFG_FILENAME = "mysql.ini";
	private final String APP_CFG_FILENAME = "config.ini";
	private final String FILTER_DATA_FILENAME = "filter.dat";
	
	private final String DEFAULT_IMAGE_THREADS = "1";
	private final String DEFAULT_WRITE_BLOCKED = "false";
	private final String DEFAULT_BASE_URL = "http://boards.4chan.org/";
	private final String DEFAULT_SUB_PAGES = "a;15,w;15,wg;15";
	
	private final int FILTER_UPDATER_STARTUP_DELAY = 4 * 1000 * 60 * 60;
	private final int FILTER_UPDATER_REFRESH_INTERVAL = 1000 * 60;
	
	private final int CACHE_PRUNE_STARTUP_DELAY = 120 * 60 * 1000;
	private final int CACHE_PRUNE_INTERVAL = 15 * 60 * 1000;
	private final int CACHE_PRUNE_MAX_AGE_SEC = 240 * 60;
	
	private final Timer daemons = new Timer("Daemon thread", true);
	
	private URL siteUrl;
	
	public static void main(String[] args) throws Exception {
		new Main().init();
	}

	/**
	 * This method constructs all Objects
	 * @throws SQLException 
	 */
	final private void build() throws SQLException{
		String image, writeBlocked, baseUrl = "", preferredBoards = "";
		int imageThreads = 1;
		boolean writeBlock = false;
	
		
		
		//  -------------- Configuration loading starts here --------------
		appSettings = loadAppConfig(APP_CFG_FILENAME);

		if(! SettingValidator.validateAppSettings(appSettings)){
			String message = "One or more program settings are invalid. Please correct them and restart the program.\n"
					+ " to reset to the default values, delete "+APP_CFG_FILENAME+" and restart the program.";
			logger.error(message);
			dieWithError(message, 1);
		}

		image = appSettings.getProperty(AppSetting.image_threads.toString(),DEFAULT_IMAGE_THREADS);
		writeBlocked = appSettings.getProperty(AppSetting.write_blocked.toString(),DEFAULT_WRITE_BLOCKED);
		baseUrl = appSettings.getProperty(AppSetting.base_url.toString(),DEFAULT_BASE_URL);
		preferredBoards = appSettings.getProperty(AppSetting.preferredBoards.toString(),DEFAULT_SUB_PAGES);
	 

		if(image != null){imageThreads = Integer.parseInt(image);}
		if(writeBlocked != null){writeBlock = Boolean.parseBoolean(writeBlocked);}
		
		defaultDirectory = appSettings.getProperty("default_directory",null);

		if(defaultDirectory == null){
			saveDirectory = (String)JOptionPane.showInputDialog(
					aid,
					"Enter a path save path",
					"Select Drive",
					JOptionPane.PLAIN_MESSAGE
					);
			if(saveDirectory != null){
				new File(saveDirectory).mkdirs();
				appSettings.setProperty("default_directory", saveDirectory);
			}
		}else{
			saveDirectory = defaultDirectory;
		}

		if((saveDirectory == null)||(saveDirectory.equals(""))){
			String message = saveDirectory+" is an invalid save location. Quitting...";
			dieWithError(message, 2);
		}

		File basePath = new File(saveDirectory);
		if(! basePath.exists()){
			String message = saveDirectory + " is an invalid save location. Quitting...";
			dieWithError(message, 3);
		}

		sqlProps = loadMySqlConfig(MYSQL_CFG_FILENAME);

		if(sqlProps == null){
			String message = "Unable to load MySQL config.\nCheck settings and restart the Programm.";
			dieWithError(message, 4);
		}

		URL checkAliveUrl = null;
		try {
			checkAliveUrl = new URL(baseUrl);
		} catch (MalformedURLException e) {
			String message = "URL is invalid: ";
			logger.warn(message + e.getMessage());
		}

		if (checkAliveUrl == null) {
			try {
				checkAliveUrl = new URL(baseUrl);
			} catch (MalformedURLException e) {
				String message = "URL is invalid: ";
				logger.warn(message + e.getMessage());
			}
		}
		 
		if(checkAliveUrl == null){
			dieWithError("No valid baseUrl was specified: "+baseUrl
							+", will now exit...", 8);
		}
		
		strategy = findSiteStrategy(checkAliveUrl); //TODO change settings to contain list of site URLs
		siteUrl = checkAliveUrl;
		
		//  -------------- Class instantiation starts here --------------  //
		connPool = new BoneConnectionPool(sqlProps,10); // connection pool for database connections
		try {
			connPool.startPool();
		} catch (Exception e) {
			String message = "Unable to connect to database:\n"
						+e.getMessage();
			dieWithError(message, 7);
		}
		blockListModel = new BlockListDataModel();
		mySQL = new AidDAO(connPool);
		lastModCheck = new LastModCheck(connPool.getConnectionSource());
		thumbLoader = new ThumbnailLoader(mySQL);
		DefaultListModel<String> fileNameModel = new DefaultListModel<>();
		DefaultListModel<String> postContentModel = new DefaultListModel<>();
		filter = new filter.Filter(mySQL,blockListModel,fileNameModel, postContentModel, thumbLoader); // filter handler
		filterlist = new Filterlist(filter, fileNameModel, postContentModel); // filter GUI
		fileWriter = new FileWriter(filter); // disk IO

		imageLoader = new ImageLoader(fileWriter, filter, basePath,imageThreads);
		logger.info("Saving files to the basePath "+basePath.toString());
		blockList = new BlockList(filter,blockListModel);
		
		// parse subpages
		String[] subP = preferredBoards.split(",");
		
		
		//TODO put this into setting loader class?
		Document mainpage = loadPage(checkAliveUrl);
		Map<String, URL> boardMap = strategy.findBoards(mainpage);
		Map<String, URL> shortcutMap = createShortcutMap(boardMap);

		for (String s : subP) {
			try {
				if (shortcutMap.containsKey(s)) {
					Board b = new Board(shortcutMap.get(s), s, strategy, filter, imageLoader, lastModCheck);
					boards.addElement(b);
				}
			} catch (IndexOutOfBoundsException oob) {
				logger.warn("Sub_pages is not configured correctly");
			}
		}

		aid = new Aid(boards,this);
		Stats.addStatListener(aid);
		fileWriter.setWriteBlocked(writeBlock);
		
		int x = 0, y = 0;
		
		x = Integer.parseInt(appSettings.getProperty(AppSetting.xpos.toString()));
		y = Integer.parseInt(appSettings.getProperty(AppSetting.ypos.toString()));
		
		aid.setLocation(x,y);
	}
	
	private Map<String, URL> createShortcutMap(Map<String, URL> boardMap) {
		Map<String, URL> shortcutMap = new HashMap<>();
		
		Collection<URL> boardUrls = boardMap.values();
		
		for (URL boardUrl : boardUrls) {
			String shortcut = strategy.getBoardShortcut(boardUrl);
			shortcutMap.put(shortcut, boardUrl);
		}
		
		return shortcutMap;
	}
	
	private SiteStrategy findSiteStrategy(URL boardUrl) {
		//TODO code me
		// get strategy list, iterate and test
		return new FourChanStrategy();
	}

	/**
	 * This Method initializes the Program
	 */
	private void init() throws Exception{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e){
			logger.warn("Unable to load look and feel: "+e.getMessage());
		}

		// shutdown Thread will be called on exit
		Runtime.getRuntime().addShutdownHook(new Shutdown());

		// so java will not be appended to http user-agent field
		System.setProperty("http.agent", ""); 

		logger.info("Working directory is: " + PWD);

		// create all needed classes
		build();
		//FIXME re-enable Schema updater
//		try {
//			SchemaUpdater.update(new AidDAO(connPool), new InternalSetting());
//		} catch (SchemaUpdateException e) {
//			String message = "Schema update failed: "+e.getMessage();
//			dieWithError(message, 6);
//		}
		
		InputStream is = null;
		try{
			is = new FileInputStream(FILTER_DATA_FILENAME);
			if(is != null){
				filter.loadFilter(is); // load list of filtered items
				is.close();
			}
		}catch(IOException ioe){
			logger.warn("Error accessing file " + ioe.getMessage());
		}

		startDaemons(connPool);
		aid.setVisible(true);

		String startupMessage = "Startup complete";
		logger.info(startupMessage);
		Log.add(startupMessage);
	}
	
	private void dieWithError(String message, int errorCode){
		logger.error(message);
		JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
		System.exit(errorCode);
	}
	
	private Document loadPage(URL url) {
		try {
			return Jsoup.connect(url.toString()).userAgent("Mozilla").get();
		} catch (IOException e) {
			logger.warn("Failed to load page {} with error {}", url, e);
			return Jsoup.parse("");
		}
	}
	
	/**
	 * Unleash the daemons!
	 * @param pool daemons need connections too...
	 */
	private void startDaemons(ConnectionPool pool) {
		try {
			logger.info("Starting daemon {}...", FilterUpdateDaemon.class);
			daemons.schedule(
					new FilterUpdateDaemon(pool.getConnectionSource()),
					FILTER_UPDATER_STARTUP_DELAY,
					FILTER_UPDATER_REFRESH_INTERVAL);
		} catch (Exception e) {
			logger.warn("Failed to start {}", FilterUpdateDaemon.class, e);
		}

		try {
			logger.info("Starting daemon {}...", CachePruneDaemon.class);
			daemons.schedule(new CachePruneDaemon(pool.getConnectionSource(),
					siteUrl, CACHE_PRUNE_MAX_AGE_SEC),
					CACHE_PRUNE_STARTUP_DELAY, CACHE_PRUNE_INTERVAL);
		} catch (Exception e) {
			logger.warn("Failed to start {}", CachePruneDaemon.class, e);
		}
	}

	/**
	 * Load the mySql settings
	 */
	public Properties loadMySqlConfig(String filepath){
		try {
			Properties sqlProps = new DefaultMySQLconnection(); 
			InputStream is = new FileInputStream(filepath);
			if(is != null)
				sqlProps.load(is);
			return sqlProps;
		} catch (IOException ioe) {
			logger.warn("Error accessing file "+ ioe.getMessage());
			return new DefaultMySQLconnection();
		}
	}
	
	/**
	 * Loads the Application configuration from file.
	 * If an error occurs whilst loading the configuration, a default config
	 * is returned.
	 * @return Property Object containing the configuration.
	 */
	public Properties loadAppConfig(String filepath){
		try {
			Properties appSetting = new DefaultAppSettings();
			InputStream is = new FileInputStream(filepath);
			
			if(is != null) {
				appSetting.load(is);
			}

			return appSetting;
		} catch (IOException ioe) {
			logger.warn("Error accessing file "+ ioe.getMessage());
			return new DefaultAppSettings();
		}
	}
	
	public void actionPerformed(ActionEvent e){
		if("Clear ImageQueue".equals(e.getActionCommand())){
			imageLoader.clearQueue();
		}

		if("Prune cache".equals(e.getActionCommand())){
			filter.pruneCache();
		}

		if("Filter".equals(e.getActionCommand())){
			blockList.setVisible(true);
		}

		if("Filterlist".equals(e.getActionCommand())){
			filterlist.setVisible(true);
		}

		if ("Clear stats".equals(e.getActionCommand())){
			fileWriter.clearStats();
			Log.add("Stats cleared");
		}
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener){
		change.addPropertyChangeListener(listener);
	}

	/**
	 * Thread for the Shutdown hook.
	 * This Thread will be called when the Program is shut down.
	 */
	class Shutdown extends Thread{
		public Shutdown() {
			super("main shutdown hook");
		}
		
		@Override
		public void run(){
			logger.info("Shutting down...");

			// stop boards
			logger.info("Stopping all boards...");
			for(Object o : boards.toArray()){
				((Board)o).stop();
			}

			// shutdown file downloading
			if(imageLoader != null){
				imageLoader.shutdown();
			}

			// shutdown writing to disk
			try {
				if(fileWriter != null){
					fileWriter.shutdown();
					fileWriter.join();
				}
			} catch (InterruptedException e) {
				logger.debug("FileWriter was interrupted");
			}

			// close all DB connections
			if(connPool != null){
				connPool.stopPool();
			}

			// save the thread filter
			if(filter != null){
				logger.info("Saving Filter...");
				filter.saveFilter(new File(PWD,FILTER_DATA_FILENAME));
			}

			// update window position
			appSettings.put("xpos", String.valueOf(aid.getX()));
			appSettings.put("ypos", String.valueOf(aid.getY()));
			
			// save program settings
			try {
				logger.info("Saving application settings to {}", APP_CFG_FILENAME);
				appSettings.store(new FileOutputStream(APP_CFG_FILENAME), "General application settings");
			} catch (IOException e) {
				logger.warn("Unable to save application settings: {}", e.getMessage());
			}

			// save mysql settings
			try {
				logger.info("Saving database settings to {}", MYSQL_CFG_FILENAME);
				sqlProps.store(new FileOutputStream(MYSQL_CFG_FILENAME), "MySQL connection settings");
			} catch (IOException e) {
				logger.warn("Unable to save Database settings: {}", e.getMessage());
			}

			logger.info("Shutdown complete.");
		}
	}	
}
