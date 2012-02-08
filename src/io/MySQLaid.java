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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Properties;

import filter.FilterItem;
import filter.FilterState;

public class MySQLaid extends MySQL {
	public MySQLaid(ConnectionPool mySqlProps) {
		super(mySqlProps);
	}
	
	static{
		init();
	}
	
	private static void init(){
		addPrepStmt("addFilter"			, "INSERT IGNORE INTO filter (id, board, reason, status) VALUES (?,?,?,?)");
		addPrepStmt("updateFilter"		, "UPDATE filter SET status = ? WHERE id = ?");
		addPrepStmt("filterState"		, "SELECT status FROM filter WHERE  id = ?");
		addPrepStmt("pendingFilter"		, "SELECT board, reason, id FROM filter WHERE status = 1 ORDER BY board, reason ASC");
		addPrepStmt("filterTime"		, "UPDATE filter SET timestamp = ? WHERE id = ?");
		addPrepStmt("oldestFilter"		, "SELECT id FROM filter ORDER BY timestamp ASC LIMIT 1");
	}
	
	public boolean addFilter(FilterItem fi){
		return addFilter(fi.getUrl().toString(), fi.getBoard(), fi.getReason(), fi.getState());
	}
	
	/**
	 * Adds a filter item to the database.
	 * @param id id of the item
	 * @param board board alias
	 * @param reason reason for adding the filter
	 * @param state initial state of the filter
	 * @return true if the filter was added, else false
	 */
	public boolean addFilter(String id, String board, String reason, FilterState state){
		PreparedStatement addFilter =getPrepStmt("addFilter");
		try {
			addFilter.setString(1, id);
			addFilter.setString(2, board);
			addFilter.setString(3, reason);
			addFilter.setShort(4, (short) state.ordinal());
			int res = addFilter.executeUpdate();
	
			if(res == 0){
				logger.warning("filter already exists!");
				return false;
			}else{
				return true;
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} finally {
			closeAll(addFilter);
		}
	
		return false;
	}
	
	public void updateState(String id, FilterState state){
		PreparedStatement updateFilter = getPrepStmt("updateFilter");
		try {
			updateFilter.setShort(1, (short)state.ordinal());
			updateFilter.setString(2, id);
			updateFilter.executeUpdate();
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} finally {
			closeAll(updateFilter);
		}
	}

	public FilterState getFilterState(String id){
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = getPrepStmt("filterState");
			ps.setString(1, id);
			rs = ps.executeQuery();
			if(rs.next()){
				FilterState fs = FilterState.values()[(int)rs.getShort(1)];
				return fs; 
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		}finally{
			closeAll(ps);
		}
		return FilterState.UNKNOWN;
	}

	/**
	 * Returns all items in the filter with state set to pending (1).
	 * @return a list of all pending filter items
	 */
	public LinkedList<FilterItem> getPendingFilters(){
		PreparedStatement pendingFilter = getPrepStmt("pendingFilter");
		ResultSet rs = null;

		try {
			rs = pendingFilter.executeQuery();
			LinkedList<FilterItem> result = new LinkedList<FilterItem>();
			while(rs.next()){
				URL url;
				url = new URL(rs.getString("id"));

				result.add(new FilterItem(url, rs.getString("board"), rs.getString("reason"),  FilterState.PENDING));
			}
			
			return result;
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} catch (MalformedURLException e) {
			logger.warning("Unable to create URL "+e.getMessage());
		}finally{
			closeAll(pendingFilter);
		}
		return new LinkedList<FilterItem>();
	}
	
	public void updateFilterTimestamp(String id){
		PreparedStatement updateTimestamp = getPrepStmt("filterTime");
		try {
			updateTimestamp.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			updateTimestamp.setString(2, id);
			updateTimestamp.executeUpdate();
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} finally {
			closeAll(updateTimestamp);
		}
	}
	
	public String getOldestFilter(){
		ResultSet rs = null;
		PreparedStatement getOldest = getPrepStmt("oldestFilter");

		try {
			rs = getOldest.executeQuery();
			if(rs.next()){
				String s = rs.getString(1);
				return s;
			}else {
				return null;
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getLocalizedMessage());
		}finally{
			closeAll(getOldest);
		}
		return null;
	}
}