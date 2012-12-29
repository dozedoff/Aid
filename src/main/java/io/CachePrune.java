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

import java.net.URL;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dozedoff.commonj.gui.Log;
import com.github.dozedoff.commonj.net.GetHtml;

public class CachePrune {
	GetHtml getHtml = new GetHtml();
	URL testAliveUrl;
	int refreshInterSec = 1;
	int startupDelaySec = 1;
	int maximumAgeSec = 1;
	Timer cachePruneTimer;
	AidDAO sql;

	static final Logger logger = LoggerFactory.getLogger(CachePrune.class.getName());

	public CachePrune(AidDAO sql, URL testAliveUrl, int refreshInterSec, int startupDelaySec, int maximumAgeSec) {
		this.testAliveUrl = testAliveUrl;
		this.sql = sql;
		this.refreshInterSec = refreshInterSec * 1000;
		this.startupDelaySec = startupDelaySec * 1000;
		this.maximumAgeSec = maximumAgeSec;
	}

	public boolean start(){
		if(cachePruneTimer != null)
			return false;

		cachePruneTimer = new Timer("CachePrune Timer");
		cachePruneTimer.scheduleAtFixedRate(new CachePruneWorker() , startupDelaySec, refreshInterSec);

		return true;
	}

	public void stop(){
		if(cachePruneTimer == null)
			return;
		cachePruneTimer.cancel();
		cachePruneTimer = null;
	}

	class CachePruneWorker extends TimerTask{

		@Override
		public void run() {
			int response = -1;
			try{
				response = getHtml.getResponse(testAliveUrl);
			}catch (Exception e){
				String message = "Failed to contact URL: "+e.getMessage()+"\n"
								+"Response code was: "+response;
				logger.warn(message);
				Log.add(message);
			}
				
				if(response != 200){
					String message = "Could not verify that client is online, skipping cache prune.";
					logger.warn(message);
					Log.add(message);
					return;
				}

				sql.pruneCache(maxAge(maximumAgeSec)); // delete keys that are older than maximumAgeMin
				Stats.setCacheSize(sql.size(AidTables.Cache)); // update GUI
		}

	}

	private long maxAge(int timeInSec){
		//TODO replace this with SQL
		Calendar exp = Calendar.getInstance();
		exp.add(Calendar.SECOND, (-timeInSec));

		return exp.getTimeInMillis();
	}
}
