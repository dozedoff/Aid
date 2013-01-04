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

import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import board.Post;

import gui.Stats;
import io.dao.CacheDAO;
import io.tables.Cache;

public class CacheCheck {
	private static final Logger logger = LoggerFactory.getLogger(CacheCheck.class);
	private CacheDAO cacheDao;
	private final int CACHE_PRUNE_THRESHOLD_HRS = 4;

	public CacheCheck(CacheDAO cacheDao) {
		this.cacheDao = cacheDao;
	}
	
	public boolean isCached(URL url) {
		boolean isCached = false;
		String cacheId = url.toString();
		
		try {
			isCached = cacheDao.idExists(cacheId);
		} catch (SQLException e) {
			logger.warn("Failed to check if {} is cached", cacheId, e);
		}
		return isCached;
	}
	
	public boolean isDownloaded(URL url) {
		boolean isDownloaded = false;
		try {
			isDownloaded = cacheDao.isDownloaded(url);
		} catch (SQLException e) {
			logger.warn("Failed to check if {} is downloaded", url, e);
		}
		return isDownloaded;
	}
	
	public boolean areAllCached(List<Post> posts){
		boolean allCached = false;
		
		for(Post p : posts){
			if(!isCached(p.getImageUrl())){
				allCached = false;
				break;
			}
		}
		
		return allCached;
	}
	
	public boolean areAllDownloaded(List<Post> posts) {
		boolean allDownloaded = false;

		try {
			for (Post p : posts) {
				if (!cacheDao.isDownloaded(p.getImageUrl())) {
					allDownloaded = false;
					break;
				}
			}
		} catch (SQLException e) {
			logger.warn("Failed to check if all posts are downloaded", e);
		}

		return allDownloaded;
	}
	
	public void addCache(URL url){
		addCache(url, true);
	}
	
	public void addCache(URL url, boolean downloaded) {
		String cacheId = url.toString();
		Cache cache = new Cache(cacheId, downloaded);
		cache.setTimestamp(Calendar.getInstance().getTime());

		try {
			if(!cacheDao.idExists(cacheId)){
				cacheDao.create(cache);
			}else{
				logger.info("Cache entry for {} already exists", cacheId);
			}
			int cacheSize = getCacheSize();
			Stats.setCacheSize(cacheSize);
		} catch (SQLException e) {
			logger.warn("Failed to create/update cache entry for {} (downloaded: {})", cacheId, downloaded);
		}
	}
	
	public void setDownloaded(URL imageUrl, boolean downloaded){
		String cacheId = imageUrl.toString();
		
		try {
			Cache cache = cacheDao.queryForId(cacheId);
			if(cache == null){
				logger.info("Could not find cache entry {} to set download state", cacheId);
			}
			cache.setDownloaded(downloaded);
			cacheDao.update(cache);
		} catch (SQLException e) {
			logger.warn("Could not set download state for {}", cacheId, e);
		}
	}
	
	public int getCacheSize() {
		int cacheSize = -1;
		try {
			cacheSize = (int)cacheDao.countOf();
		} catch (SQLException e) {
			logger.warn("Failed to get cache size", e);
		}
		return cacheSize;
	}
	
	public void pruneCache() {
		Calendar exp = Calendar.getInstance();
		exp.add(Calendar.HOUR, -CACHE_PRUNE_THRESHOLD_HRS);

		try {
			cacheDao.pruneCache(exp.getTimeInMillis());
			int cacheSize = getCacheSize();
			Stats.setCacheSize(cacheSize);
		} catch (SQLException e) {
			logger.warn("Failed to prune cache entries older than {} hours", CACHE_PRUNE_THRESHOLD_HRS);
			logger.warn("Error was", e);
		}
	}
}
