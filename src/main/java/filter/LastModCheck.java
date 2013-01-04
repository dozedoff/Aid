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

import gui.Stats;
import io.dao.CacheDAO;
import io.dao.LastModifiedDAO;
import io.tables.Cache;
import io.tables.LastModified;

import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import board.Post;

public class LastModCheck {
	LastModifiedDAO lastModifiedDao;
	CacheDAO cacheDao;
	
	final static Logger logger = LoggerFactory.getLogger(LastModCheck.class);
	private final long INITIAL_TIMESTAMP = 1000;
	
	public LastModCheck(CacheDAO cacheDao, LastModifiedDAO lastModifiedDao) {
		this.cacheDao = cacheDao;
		this.lastModifiedDao = lastModifiedDao;
	}

	public boolean contains(String threadUrl) {
		try {
			return lastModifiedDao.idExists(threadUrl);
		} catch (SQLException e) {
			logger.warn("Failed to lookup id for lastModified", e);
			return false;
		}
	}
	
	public boolean isVisitNeeded(String threadUrl, long modTimestamp) {
		boolean visit = true;
		
		if(!contains(threadUrl)){
			logger.info("Looking up modified state for {} with mod time {} - Not present in db", threadUrl, modTimestamp);
			return visit;
		}
		
		try {
			LastModified lastModified = lastModifiedDao.queryForId(threadUrl);
			long dbTimestamp = lastModified.getLastmod().getTime();
			Object[] logData = {threadUrl, modTimestamp, dbTimestamp};
			
			if(modTimestamp <= dbTimestamp) {
				logger.info("Looking up modified state for {} with mod time {} - found timestamp {} - no change", logData);
				visit = false;
			}else{
				if(dbTimestamp == INITIAL_TIMESTAMP){
					logger.info("Looking up modified state for {} with mod time {} - found new Thread", logData);
				} else {
					logger.info("Looking up modified state for {} with mod time {} - found timestamp {} - CHANGED", logData);
				}
				
			}
			
			addOrUpdateLastModified(threadUrl, modTimestamp);
		} catch (SQLException e) {
			logger.warn("Failed to get data for lastModified", e);
		}
		
		return visit;
	}
	
	public LastModified addOrUpdateLastModified(String threadUrl, long lastModTime) {
			LastModified dbLastMod = null;
		try {
			dbLastMod = lastModifiedDao.queryForId(threadUrl);
			dbLastMod.setLastmod(new Date(lastModTime));
			dbLastMod.setLastvisit(now());
			lastModifiedDao.createOrUpdate(dbLastMod);
			logger.info("Added last modified entry for {} at {}", threadUrl, lastModTime);
			lastModifiedDao.refresh(dbLastMod);
		} catch (SQLException e) {
			logger.warn("Failed to add last modiefied entry for {} with time {}", threadUrl, lastModTime);
			logger.warn("Error was ", e);
			dbLastMod = null;
		}
		
		return dbLastMod;
	}
	
	public void setCacheLastModId(LastModified lastMod, List<Post> posts){
		if(lastMod == null){
			logger.warn("Could not set last_mod_id for cache, LastModified was null");
			return;
		}
		
		logger.info("Setting last_mod_id for {} posts for thread {}", posts.size(), lastMod.getId());

		int lmID = lastMod.getLast_mod_id();
		for(Post post : posts){
			try {
				Cache cache = cacheDao.queryForId(post.getImageUrl().toString());
				cache.setLast_mod_id(lmID);
				cacheDao.update(cache);
			} catch (SQLException e) {
				logger.warn("Faild to update last_mod_id for {}", post.getImageUrl(), e);
			}
		}
	}
	
	public void updateCachedLinks(URL thread){
		String lastModId = thread.toString();
		try {
			LastModified lastMod = lastModifiedDao.queryForId(lastModId);
			if(lastMod == null){
				logger.info("Could not find lastmodified entry for thread {}. Aborting cache update.", lastModId);
				return;
			}
			
			int last_mod_id = lastMod.getLast_mod_id();
			
			List<Cache> cacheEntries = cacheDao.queryForEq("last_mod_id", last_mod_id);
			
			Date currentTime = Calendar.getInstance().getTime();
			
			for(Cache cache : cacheEntries){
				cache.setTimestamp(currentTime);
				cacheDao.update(cache);
			}
			
			Object[] logData = {cacheEntries.size(), lastModId, currentTime};
			logger.info("Updated {} cache entries for thread {} with the new timestamp {}", logData);
		} catch (SQLException e) {
			logger.warn("Could not update cache links for lastmodified thread {}", lastModId);
			logger.warn("Error was", e);
		}
	}
	
	public void addCacheLinks(URL thread, List<Post> posts){
		String threadId = thread.toString();
		try {
			LastModified lastMod = lastModifiedDao.queryForId(threadId);
			
			if(lastMod == null){
				logger.info("Cannot add cache-lastmod links, no entry found for {}", threadId);
				return;
			}
			
			int last_mod_id = lastMod.getLast_mod_id();
			
			for(Post post : posts){
				String cacheId = post.getImageUrl().toString();
				Cache cache = new Cache(cacheId, false);
				
				cache.setLast_mod_id(last_mod_id);
				cacheDao.createIfNotExists(cache);
			}
			Stats.setCacheSize((int)cacheDao.countOf());
			logger.info("Added {} cache links to thread {}", posts.size(), threadId);
		} catch (SQLException e) {
			logger.warn("Failed to add cache links for thread {}", thread, e);
		}
	}
	
	public void recordNewThreads(List<URL> threads){
		for(URL thread : threads){
			String threadId = thread.toString();
			try {
				LastModified lm = new LastModified(threadId, new Date(INITIAL_TIMESTAMP), now());
				lastModifiedDao.createIfNotExists(lm);
			} catch (SQLException e) {
				logger.warn("Failed to add thread {} last modified table", threadId, e);
			}
		}
		try {
			Stats.setCacheSize((int)cacheDao.countOf());
		} catch (SQLException e) {
			logger.warn("Failed to update cache stats", e);
		}
	}
	
	public boolean areAllDownloaded(URL thread){
		String threadId = thread.toString();
		boolean allDownloaded = false;
		try {
			LastModified lm =  lastModifiedDao.queryForId(threadId);
			
			if(lm == null){
				return allDownloaded;
			}
			
			allDownloaded = cacheDao.areAllDownloaded(lm.getLast_mod_id());
		} catch (SQLException e) {
			logger.warn("Failed to check if all files for {} were downloaded", thread, e);
		}
		
		return allDownloaded;
	}
	
	private Date now() {
		return Calendar.getInstance().getTime();
	}
}
