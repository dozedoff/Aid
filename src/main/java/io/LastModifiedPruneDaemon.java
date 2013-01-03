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
package io;

import gui.Stats;

import io.dao.CacheDAO;
import io.dao.LastModifiedDAO;
import io.tables.Cache;
import io.tables.LastModified;

import java.net.URL;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dozedoff.commonj.gui.Log;
import com.github.dozedoff.commonj.net.GetHtml;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class LastModifiedPruneDaemon extends TimerTask {
	GetHtml getHtml = new GetHtml();
	URL testAliveUrl;
	int maximumAgeSec = 1;
	Timer cachePruneTimer;
	LastModifiedDAO lastModifiedDao;
	
	static final Logger logger = LoggerFactory.getLogger(LastModifiedPruneDaemon.class);

	public LastModifiedPruneDaemon(ConnectionSource connection, URL testAliveUrl, int maximumAgeSec) throws Exception {
		this.testAliveUrl = testAliveUrl;
		this.maximumAgeSec = maximumAgeSec;
		
		lastModifiedDao = DaoManager.createDao(connection, LastModified.class);
	}

	@Override
	public void run() {
		try {
			int response = -1;
			response = getHtml.getResponse(testAliveUrl);

			if (response != 200) {
				String message = "Could not verify that client is online, skipping last modified prune.";
				logger.warn(message);
				Log.add(message);
				return;
			}

			int pruned = lastModifiedDao.pruneLastMod(maxAge(maximumAgeSec));
			logger.info("Pruned {} last modified entries", pruned);
		} catch (SQLException se) {
			logger.warn("Cache prune failed", se);
		} catch (Exception e) {
			logger.warn("Alive test failed to connect to {}", testAliveUrl, e);
		}
	}

	private long maxAge(int timeInSec){
		Calendar exp = Calendar.getInstance();
		exp.add(Calendar.SECOND, (-timeInSec));

		return exp.getTimeInMillis();
	}
}
