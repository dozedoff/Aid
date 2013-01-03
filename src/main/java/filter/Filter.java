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
package filter;

import gui.BlockListDataModel;
import gui.Stats;
import io.AidDAO;
import io.AidTables;
import io.ThumbnailLoader;
import io.dao.FilterDAO;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.DefaultListModel;

import board.Post;

/**
 * Class used to filter thread contents based on Post content and Filenames,
 * as well as checking File Hashes, performing cache look up's and checking
 * the threads status.
 */
public class Filter implements FilterModifiable{
	private static Logger logger = LoggerFactory.getLogger(Filter.class);
	private final String LOCATION_TAG = "DL_CLIENT";
	private int filterNr = 0;	// filter item counter

	private BlockListDataModel blocklistModel;
	private DefaultListModel<String> fileNameModel;
	private DefaultListModel<String> postContentModel;
	private ThumbnailLoader thumbLoader;

	private AidDAO sql;
	private FilterDAO filterDao;
	private CacheCheck cacheCheck;
	
	public Filter(AidDAO sql,FilterDAO filterDao, CacheCheck cacheCheck, BlockListDataModel blockListModel,DefaultListModel<String> fileNameModel, DefaultListModel<String> postContentModel, ThumbnailLoader thumbLoader){
		this.sql = sql;
		this.filterDao = filterDao;
		
		this.blocklistModel = blockListModel;
		this.fileNameModel = fileNameModel;
		this.postContentModel = postContentModel;
		this.thumbLoader = thumbLoader;
		this.cacheCheck = cacheCheck;
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
			logger.error("Unable to load "+file.toString());
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
			DefaultListModel<String> tmpPostContentModel = (DefaultListModel<String>)o.readObject();
			DefaultListModel<String> tmpFileNameModel = (DefaultListModel<String>)o.readObject();
			o.close();
			
			for(Object obj : tmpFileNameModel.toArray()){
				fileNameModel.addElement((String)obj);
			}
			
			for(Object obj : tmpPostContentModel.toArray()){
				postContentModel.addElement((String)obj);
			}
		}catch (IOException io) { 
			logger.warn("Error when loading file: "+io.getMessage());
			return false;
		} catch (ClassNotFoundException e) {
			logger.warn("Could not locate class: "+e.getMessage());
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
			o.writeObject(postContentModel);
			o.writeObject(fileNameModel);
			o.close();
			logger.info("Saved filter to "+path);
			return true;
		}catch ( IOException e ) { 
			logger.error("Error when saving file: "+e.getMessage()); 
			return false;}
	}

	public void addFileNameFilterItem(String item){
		if(! fileNameModel.contains(item))
			fileNameModel.addElement(item);
	}

	public void addPostContentFilterItem(String item){
		if(! postContentModel.contains(item))
			postContentModel.addElement(item);
	}

	public void removeFileNameFilterItem(String item){
		fileNameModel.removeElement(item);
	}

	public void removePostContentFilterItem(String item){
		postContentModel.removeElement(item);
	}

	/**
	 * Returns the filter state of the Item.
	 * If the item is not in the Filter, unknown is returned.
	 * 
	 * @param urlToTest The URL to check against the database.
	 */
	public FilterState getFilterState(URL urlToTest){

		FilterState state = FilterState.UNKNOWN;

		state = sql.getFilterState(urlToTest.toString());
		return state;
	}

	/**
	 * Returns the number of items in the Filter.
	 * 
	 * @return Number of items in the filter.
	 */
	public int getSize(){
		int size = -1;
		try {
			size = (int)filterDao.countOf();
		} catch (SQLException e) {
			logger.warn("Failed to get filter table size", e);
		}
		return size;
	}

	/**
	 * Returns the number of items with the status "pending".
	 * 
	 * @return Number of "Pending" filter items.
	 */
	public int getPending(){
		int pending = -1;
		try {
			pending = filterDao.getPendingFilterCount();
		} catch (SQLException e) {
			logger.warn("Failed to get filter pending size", e);
		}
		return pending;
	}

	/**
	 * Adds a new item to the filter list. 
	 * @param filteritem FilterItem to add.
	 */
	public void reviewThread(FilterItem filterItem){
		try {
			filterDao.create(filterItem);
			blocklistModel.addElement(filterItem);
			filterNr++;
			Stats.setFilterSize(filterNr);
		} catch (SQLException e) {
			logger.warn("Failed to add filter for {} with reason {}",filterItem.getUrl(), filterItem.getReason());
			logger.warn("Error was", e);
		}
	}

	/**
	 * Set the filter item to "allow".
	 * Files in this thread will be processed.
	 * 
	 * @param url URL to allow.
	 */
	public void setAllow(URL url){
		try {
			setFilterState(url, FilterState.ALLOW);
		} catch (SQLException e) {
			logger.warn("Failed to set filter ALLOW state for {}", url);
			logger.warn("Error was", e);
		}
	}

	/**
	 * Set the filter item to "deny".
	 * Files in this thread will not be processed.
	 */
	public void setDeny(URL url){
		try {
			setFilterState(url, FilterState.DENY);
		} catch (SQLException e) {
			logger.warn("Failed to set filter DENY state for {}", url);
			logger.warn("Error was", e);
		}
	}
	
	private void setFilterState(URL url, FilterState state) throws SQLException{
		String filterId = url.toString();
		FilterItem filterItem = filterDao.queryForId(filterId);
		filterItem.setState(state);
		filterDao.update(filterItem);
		filterNr--;
		Stats.setFilterSize(filterNr);
	}
	
	/**
	 * Check all pending items if they still exist (that the thread they
	 * reference has not 404'd).<br/>
	 * Non existing items will be removed from the database and the GUI-list.
	 */
	public void refreshList(){
		logger.error("Functionality removed");
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
			for (Object detail : fileNameModel.toArray()){
				if (p.getImageName().toLowerCase().contains((String)detail)){
					return "file name, "+(String)detail;
				}
			}
		}

		// filter out unwanted content (Post content check)
		if(p.hasComment()){
			for (Object detail : postContentModel.toArray()){
				if (p.getComment().toLowerCase().contains((String)detail))
					return "post content, "+(String)detail;
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
		boolean known = cacheCheck.isCached(url);
		return known;
	}
	/**
	 * Adds the URL to the cache or updates the existing timestamp.
	 * @param url URL to add.
	 */
	public void cache(URL url){
		cacheCheck.addCache(url);
		Stats.setCacheSize(cacheCheck.getCacheSize());
	}
	
	/**
	 * Remove all cache entries with timestamps older than 3 hours.<br/>
	 */
	public void pruneCache(){
		cacheCheck.pruneCache();
		Stats.setCacheSize(cacheCheck.getCacheSize());
	}
	
	/**
	 * Checks if the hash has been recorded.
	 * @param hash Hash to check
	 * @return true if found else false.<br/>
	 * Returns true on error.
	 */
	public boolean exists(String hash){
		boolean exists = sql.isDnw(hash)||sql.isHashed(hash);
		return exists;
	}
	
	public void addIndex(String hash, String path, int size) throws SQLException{
				sql.addIndex(hash, path, size, LOCATION_TAG);
	}
	
	/**
	 * Check if the hash is blacklisted.
	 * @param hash Hash to check
	 * @return true if found.
	 * Returns false on error.
	 */
	public boolean isBlacklisted(String hash){
		boolean blocked = sql.isBlacklisted(hash);
		if(blocked){
			//remove that hash from other tables
			sql.delete(AidTables.Fileindex, hash);
			sql.delete(AidTables.Dnw, hash);
		}
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
	
	public void downloadThumbs(String url, List<Post> postList){
		thumbLoader.downloadThumbs(url, postList);
	}
}
