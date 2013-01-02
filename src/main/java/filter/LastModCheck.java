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

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.tables.LastModified;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class LastModCheck {
	Dao<LastModified, String> lastModifiedDao;
	final static Logger logger = LoggerFactory.getLogger(LastModCheck.class);
	
	public LastModCheck(ConnectionSource source) throws SQLException {
		lastModifiedDao = DaoManager.createDao(source, LastModified.class);
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
			LastModified lastModiefied = lastModifiedDao.queryForId(threadUrl);
			long dbTimestamp = lastModiefied.getLastmod().getTime();
			Object[] logData = {threadUrl, modTimestamp, dbTimestamp};
			
			if(modTimestamp <= dbTimestamp) {
				logger.info("Looking up modified state for {} with mod time {} - found timestamp {} - no change", logData);
				visit = false;
			}else{
				logger.info("Looking up modified state for {} with mod time {} - found timestamp {} - CHANGE", logData);
			}
			
			lastModiefied.setLastvisit(now());
			lastModifiedDao.update(lastModiefied);
		} catch (SQLException e) {
			logger.warn("Failed to get data for lastModified", e);
		}
		
		return visit;
	}
	
	public boolean addLastModified(String threadUrl, long lastModTime) {
		boolean allOk = true;
		
		LastModified dbLastMod = new LastModified(threadUrl, new Date(lastModTime), now());
		try {
			lastModifiedDao.createOrUpdate(dbLastMod);
			logger.info("Added last modified entry for {} at {}", threadUrl, lastModTime);
		} catch (SQLException e) {
			logger.warn("Failed to add last modiefied entry for {} with time {}", threadUrl, lastModTime);
			logger.warn("Error was ", e);
			allOk = false;
		}
		
		return allOk;
	}
	
	private Date now() {
		return Calendar.getInstance().getTime();
	}
}
