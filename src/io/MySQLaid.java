package io;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;

import filter.FilterItem;
import filter.FilterState;

public class MySQLaid extends MySQL {
	public MySQLaid(Properties mySqlProps) {
		super(mySqlProps);
		// TODO Auto-generated constructor stub
	}
	
	public void init(){
		super.init();
		
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
		reconnect();
		try {
			PreparedStatement addFilter = getPrepStmt("addFilter");
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
			logger.warning("MySql Filter add failed: "+e.getMessage());
		}
	
		return false;
	}
	
	public void updateState(String id, FilterState state){
		reconnect();
		PreparedStatement updateFilter = getPrepStmt("updateFilter");
		try {
			updateFilter.setShort(1, (short)state.ordinal());
			updateFilter.setString(2, id);
			updateFilter.executeUpdate();
		} catch (SQLException e) {
			logger.warning("MySql filter update failed: "+e.getMessage());
		}
	}

	public FilterState getFilterState(String id){
		reconnect();
		ResultSet rs;

		try {
			getPrepStmt("filterState").setString(1, id);
			rs = prepStmtQuery("filterState");
			if(!rs.first()){
				rs.close();
				return FilterState.UNKNOWN;
			}
			else{
				FilterState fs = FilterState.values()[(int)rs.getShort(1)];
				rs.close();
				return fs; 
			}
		} catch (SQLException e) {
			logger.warning("MySql filterstate get failed: "+e.getMessage());
		}
		return FilterState.UNKNOWN;
	}

	/**
	 * Returns all items in the filter with state set to pending (1).
	 * @return a list of all pending filter items
	 */
	public LinkedList<FilterItem> getPendingFilters(){
		reconnect();
		PreparedStatement pendingFilter = getPrepStmt("pendingFilter");
		ResultSet rs;

		try {
			rs = pendingFilter.executeQuery();
			LinkedList<FilterItem> result = new LinkedList<FilterItem>();
			while(rs.next()){
				URL url;
				url = new URL(rs.getString("id"));

				result.add(new FilterItem(rs.getString("board"), rs.getString("reason"), url, FilterState.PENDING));
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			logger.warning("MySql PendingFilter lookup failed: "+e.getMessage());
		} catch (MalformedURLException e) {
			logger.warning("Unable to create URL "+e.getMessage());
		}
		return new LinkedList<FilterItem>();
	}

}
