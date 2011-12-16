/*  Copyright (C) 2011  Nicholas Wright
	
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
package filter;

import gui.BlockListDataModel;
import gui.Stats;
import io.ConnectionPoolaid;
import io.MySQLaid;
import io.ResourceCreationException;
import io.ThumbnailLoader;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.logging.Logger;

import board.Post;

/**
 * Class used to filter thread contents based on Post content and Filenames,
 * as well as checking File Hashes, performing cache look up's and checking
 * the threads status.
 */
public class Filter implements FilterModifiable{
	private static Logger logger = Logger.getLogger(Filter.class.getName());

	private int filterNr = 0;	// filter item counter

	private ArrayList<String> postContentKeywords = new ArrayList<String>();
	private ArrayList<String> imageNameKeywords = new ArrayList<String>();

	private BlockListDataModel blocklistModel;
	private ThumbnailLoader thumbLoader;

	private ConnectionPoolaid connPool; //TODO there is a MySql.ConnectionPool - check it out
	
	public Filter(ConnectionPoolaid connPool, BlockListDataModel blockListModel, ThumbnailLoader thumbLoader){
		this.connPool = connPool;
		this.blocklistModel = blockListModel;
		this.thumbLoader = thumbLoader;
	}
	
	public boolean loadFilter(String path){
		return loadFilter(new File(path));
	}
	
	public boolean loadFilter(File file){
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			return loadFilter(fis);
		} catch (FileNotFoundException e) {
			logger.severe("Unable to load "+file.toString());
		}
		return false;
		
	}
	
	/**
	 * Load filter data from a File.
	 */
	@SuppressWarnings("unchecked")
	public boolean loadFilter(InputStream is){
		try{
			if(is == null)
				return false;
			ObjectInputStream o = new ObjectInputStream(is);
			postContentKeywords.addAll((ArrayList<String>)o.readObject());
			imageNameKeywords.addAll((ArrayList<String>)o.readObject());
			o.close();
		}catch (IOException io) { 
			logger.warning("Error when loading file: "+io.getMessage());
			return false;		} catch (ClassNotFoundException e) {
			logger.warning("Could not locate class: "+e.getMessage());
			return false;
		}
		return true;
	}

	public boolean saveFilter(File file){
		return saveFilter(file.toString());
	}
	
	/**
	 * Save all filter items to disk.
	 */
	public boolean saveFilter(String path){
		try{
			FileOutputStream file = new FileOutputStream(path);
			ObjectOutputStream o = new ObjectOutputStream( file );  
			o.writeObject(postContentKeywords);
			o.writeObject(imageNameKeywords);
			o.close();
			logger.info("Saved filter to "+path);
			return true;
		}catch ( IOException e ) { 
			logger.severe("Error when saving file: "+e.getMessage()); 
			return false;}
	}

	public void addFileNameFilterItem(String item){
		if(! imageNameKeywords.contains(item))
			imageNameKeywords.add(item);
	}

	public void addPostContentFilterItem(String item){
		if(! postContentKeywords.contains(item))
			postContentKeywords.add(item);
	}

	public void removeFileNameFilterItem(String item){
		imageNameKeywords.remove(item);
	}

	public void removePostContentFilterItem(String item){
		postContentKeywords.remove(item);
	}

	public ArrayList<String> getFileNameFilterItem(){
		ArrayList<String> tmp = new ArrayList<String>(imageNameKeywords.size());
		for(String s : imageNameKeywords)
			tmp.add(s);

		return tmp;
	}

	public ArrayList<String> getPostContentFilterItem(){
		ArrayList<String> tmp = new ArrayList<String>(postContentKeywords.size());
		
		for(String s : postContentKeywords)
			tmp.add(s);

		return tmp;
	}

	/**
	 * Returns the filter state of the Item.
	 * If the item is not in the Filter, unknown is returned.
	 * 
	 * @param urlToTest The URL to check against the database.
	 */
	public FilterState getFilterState(URL urlToTest){
		MySQLaid mySql = getSql();

		FilterState state = FilterState.UNKNOWN;

		state = mySql.getFilterState(urlToTest.toString());
		releaseSql(mySql);
		return state;
	}

	/**
	 * Returns the number of items in the Filter.
	 * 
	 * @return Number of items in the filter.
	 */
	public int getSize(){
		MySQLaid mySql = getSql();
		int size = mySql.size("filter");
		releaseSql(mySql);
		return size;
	}

	/**
	 * Returns the number of items with the status "pending".
	 * 
	 * @return Number of "Pending" filter items.
	 */
	public int getPending(){
		MySQLaid mySql = getSql();

		int pending = mySql.getPending();

		releaseSql(mySql);

		return pending;
	}

	/**
	 * Adds a new item to the filter list.
	 * @param filteritem FilterItem to add.
	 */
	public void reviewThread(FilterItem filterItem){
		MySQLaid mySql = getSql();

		mySql.addFilter(filterItem.getUrl().toString(),  filterItem.getBoard(), filterItem.getReason(), filterItem.getState());
		blocklistModel.addElement(filterItem);
		filterNr++;
		Stats.setFilterSize(filterNr);
		releaseSql(mySql);
	}

	/**
	 * Set the filter item to "allow".
	 * Files in this thread will be processed.
	 * 
	 * @param url URL to allow.
	 */
	public void setAllow(URL url){
		MySQLaid mySql = getSql();
		mySql.updateState(url.toString(), FilterState.ALLOW);
		releaseSql(mySql);
		filterNr--;
		Stats.setFilterSize(filterNr);
	}

	/**
	 * Set the filter item to "deny".
	 * Files in this thread will not be processed.
	 */
	public void setDeny(URL url){
		MySQLaid mySql = getSql();
		mySql.updateState(url.toString(), FilterState.DENY);
		releaseSql(mySql);
		filterNr--;
		Stats.setFilterSize(filterNr);
	}
	
	/**
	 * Check all pending items if they still exist (that the thread they
	 * reference has not 404'd).<br/>
	 * Non existing items will be removed from the database and the GUI-list.
	 */
	public void refreshList(){
		Thread t = new RefreshList();
		t.start();
	}

	/**
	 * Will check a post to see if it contains blocked content / names.
	 * 
	 * @param p Post to check
	 * @return Reason if blocked, otherwise null
	 */
	public String checkPost(Post p){
		// filter out unwanted content (File Name Check)
		if(p.hasImage()){
			for (String detail : imageNameKeywords){
				if (p.getImageName().toLowerCase().contains(detail)){
					return "file name, "+detail;
				}
			}
		}

		// filter out unwanted content (Post content check)
		if(p.hasComment()){
			for (String detail : postContentKeywords){
				if (p.getComment().toLowerCase().contains(detail))
					return "post content, "+detail;
			}
		}
		return null;
	}
	
	/**
	 * Check to see if the URL is in the cache.
	 * 
	 * @param url URL to check.
	 * @return true if found, else false.
	 */
	public boolean isCached(URL url){
		MySQLaid mySql = getSql();

		boolean known = mySql.isCached(url);

		releaseSql(mySql);
		return known;
	}
	/**
	 * Adds the URL to the cache or updates the existing timestamp.
	 * @param url URL to add.
	 */
	public void cache(URL url){
		MySQLaid mySql = getSql();
		mySql.addCache(url);
		Stats.setCacheSize(mySql.size("cache"));
		releaseSql(mySql);
	}
	
	/**
	 * Remove all cache entries with timestamps older than 3 hours.<br/>
	 */
	public void pruneCache(){
		MySQLaid sql = getSql();
		Calendar exp = Calendar.getInstance();
		exp.add(Calendar.HOUR, -3);

		sql.pruneCache(exp.getTimeInMillis()); //keys that are older than 3 Hour
		Stats.setCacheSize(sql.size("cache"));
		releaseSql(sql);
	}
	
	/**
	 * Checks if the hash has been recorded.
	 * @param hash Hash to check
	 * @return true if found else false.<br/>
	 * Returns true on error.
	 */
	public boolean exists(String hash){
		MySQLaid sql = getSql();
		boolean exists = sql.isArchived(hash)||sql.isDnw(hash)||sql.isHashed(hash);
		releaseSql(sql);
		return exists;
	}
	
	public void addHash(String hash, String path, int size) throws SQLException{
		MySQLaid sql = getSql();

			try {
				sql.addHash(hash, path, size);
			} catch (SQLException e) {
				releaseSql(sql);
				throw e;
			}
		releaseSql(sql);
	}
	
	/**
	 * Check if the hash is blacklisted.
	 * @param hash Hash to check
	 * @return true if found.
	 * Returns false on error.
	 */
	public boolean isBlacklisted(String hash){
		MySQLaid sql = getSql();
		boolean blocked = sql.isBlacklisted(hash);
		if(blocked){
			//remove that hash from other tables
			sql.delete("hash", hash);
			sql.delete("archive", hash);
			sql.delete("dnw", hash);
		}
		releaseSql(sql);
		return blocked;
	}
	
	/**
	 * Fetch thumbnail data from database.
	 * @param url URL of the page thumbs to load.
	 * @return Array of Binary data.
	 */
	public ArrayList<Image> getThumbs(String url){
		return thumbLoader.getThumbs(url);
	}
	
	public void downloadThumbs(String url, ArrayList<Post> postList){
		thumbLoader.downloadThumbs(url, postList);
	}

	private MySQLaid getSql(){
		MySQLaid mySql = null;
		try {mySql = (MySQLaid) connPool.getResource(5000);
		} catch (InterruptedException e1) {
		} catch (ResourceCreationException e1) {
			logger.severe(e1.getMessage());}
		return mySql;
	}

	private void releaseSql(MySQLaid mySql){
		connPool.returnResource(mySql);
	}

	/**
	 * Attempts to connect to the URL.
	 * If it exists, update the FilterItem timestamp, else delete it.
	 * 
	 * @param mySql An active mySql connection
	 * @param url The URL to be checked
	 * @return true if valid, else false<br/>
	 * Returns false on error.
	 */
	private boolean refreshFilterItem(URL url){
		String currString = url.toString();
		MySQLaid mySql = getSql();

		try {
			if (new io.GetHtml().getResponse(currString) == 404){
				mySql.delete("filter", currString);
				releaseSql(mySql);
				return false;
			}else{
				releaseSql(mySql);
				return true;
			}
		} catch (MalformedURLException e2) {
			logger.warning("Refresh invalid URL: "+currString);
		} catch (Exception e) {
			logger.warning("Refresh failed,  Reason: "+e.getMessage());
		}
		releaseSql(mySql);
		return false;
	}
	
	/**
	 * Thread for updating the pending item list.
	 * 
	 * @author https://github.com/dozedoff
	 */
	class RefreshList extends Thread{
		@Override
		public void run() {
			MySQLaid mySql = getSql();
			LinkedList<FilterItem> filterList = new LinkedList<>();
			filterNr = 0;
			filterList.addAll(mySql.getPendingFilters());
			blocklistModel.clear();
			for(FilterItem fi : filterList){
				if(refreshFilterItem(fi.getUrl())){
					blocklistModel.addElement(fi);
					filterNr++;
				}
			}
			releaseSql(mySql);
			Stats.setFilterSize(filterNr);
		}
	}
}
