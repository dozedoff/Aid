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

import gui.Log;
import gui.Stats;

import java.net.URL;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.GetHtml;

public class CachePrune {
	GetHtml getHtml = new GetHtml();
	URL testAliveUrl;
	int refreshInterMin = 1;
	int startupDelayMin = 1;
	int maximumAgeMin = 1;
	Timer cachePruneTimer;
	MySQLaid sql;

	static final Logger logger = Logger.getLogger(CachePrune.class.getName());

	public CachePrune(MySQLaid sql, URL testAliveUrl, int refreshInterMin, int startupDelayMin, int maximumAgeMin) {
		this.testAliveUrl = testAliveUrl;
		this.sql = sql;
		this.refreshInterMin = refreshInterMin * 60 * 1000;
		this.startupDelayMin = startupDelayMin * 60 * 1000;
		this.maximumAgeMin = maximumAgeMin;
	}

	public boolean start(){
		if(cachePruneTimer != null)
			return false;

		cachePruneTimer = new Timer("CachePrune Timer");
		cachePruneTimer.scheduleAtFixedRate(new CachePruneWorker() , startupDelayMin, refreshInterMin);

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
				logger.warning(message);
				Log.add(message);
			}
				
				if(response != 200){
					String message = "Could not verify that client is online, skipping cache prune.";
					logger.warning(message);
					Log.add(message);
					return;
				}

				sql.pruneCache(maxAge(maximumAgeMin)); // delete keys that are older than maximumAgeMin
				Stats.setCacheSize(sql.size(MySQLtables.Cache)); // update GUI
		}

	}

	private long maxAge(int timeInMin){
		//TODO replace this with SQL
		Calendar exp = Calendar.getInstance();
		exp.add(Calendar.MINUTE, (-timeInMin));

		return exp.getTimeInMillis();
	}
}
