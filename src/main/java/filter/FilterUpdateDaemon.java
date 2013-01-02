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

import io.dao.FilterDAO;

import java.net.URL;
import java.sql.SQLException;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dozedoff.commonj.net.GetHtml;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public 	class FilterUpdateDaemon extends TimerTask{
	Logger logger = LoggerFactory.getLogger(FilterUpdateDaemon.class);
	FilterDAO filterDao;
	
	public FilterUpdateDaemon(ConnectionSource connection) throws Exception {
		filterDao = DaoManager.createDao(connection, FilterItem.class);
	}
	
	@Override
	public void run() {
		FilterItem oldestFilter;
		try {
			oldestFilter = filterDao.getOldestFilter();
			if (oldestFilter == null) {
				logger.info("Aborting filter update, nothing to do");
				return;
			}
			refreshFilterItem(oldestFilter);
		} catch (SQLException e) {
			logger.warn("Failed to get the oldest filter from DB", e);
		}
	}
	
	private boolean refreshFilterItem(FilterItem filterItem){
		URL threadUrl = filterItem.getUrl();

		try {
			if (new GetHtml().getResponse(threadUrl) == 404){
				filterDao.delete(filterItem);
				logger.info("Deleted filter ({}) entry for {}", filterItem.getReason(), filterItem.getUrl());
				return false;
			}else{
				logger.info("Updated filter ({}) entry for {}", filterItem.getReason(), filterItem.getUrl());
				filterDao.updateFilterTimestamp(filterItem);
				return true;
			}
		} catch (SQLException se){
			logger.warn("Failed to refresh filter item", se);
		}
		return false;
	}
}
