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
package io.dao;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;

import filter.FilterItem;
import filter.FilterState;

public class FilterDAO extends BaseDaoImpl<FilterItem, String>{
	final String SQL_UPDATE_TIME = "UPDATE `filter` SET `timestamp` = NOW() WHERE `id` = ?";
	PreparedQuery<FilterItem> oldestFilterQuery, pendingFilterCountQuery;

	public FilterDAO(ConnectionSource cSource) throws SQLException {
		super(cSource, FilterItem.class);
		oldestFilterQuery = queryBuilder().orderBy("timestamp", true).limit(1L).prepare();
		pendingFilterCountQuery = queryBuilder().setCountOf(true).where().eq("status", FilterState.PENDING).prepare();
	}
	
	public void updateFilterTimestamp(FilterItem filterItem) throws SQLException{
		String id = filterItem.getId();
		updateFilterTimestamp(id);
	}
	
	public void updateFilterTimestamp(String id) throws SQLException{
		executeRaw(SQL_UPDATE_TIME, id);
	}
	
	/**
	 * Returns the oldest Filter in the list, or null if no entries are found.
	 * @return the oldest filter entry found or null
	 * @throws SQLException
	 */
	public FilterItem getOldestFilter() throws SQLException {
		List<FilterItem>filterItems = query(oldestFilterQuery);
		
		if(filterItems.isEmpty()){
			return null;
		}else{
			return filterItems.get(0);
		}
	}
	
	public int getPendingFilterCount() throws SQLException {
		return (int)countOf(pendingFilterCountQuery);
	}
}
