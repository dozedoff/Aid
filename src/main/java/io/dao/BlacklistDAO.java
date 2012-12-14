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

import io.tables.BlacklistRecord;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;

public class BlacklistDAO extends BaseDaoImpl<BlacklistRecord,String> {
	final String BLACKLIST_STMT = "SELECT a.id FROM (select fileindex.id FROM block join fileindex on block.id = fileindex.id) AS a";
	
	public BlacklistDAO(ConnectionSource cSource) throws SQLException{
		super(cSource, BlacklistRecord.class);
	}
	
	public LinkedList<String> getBlacklisted() throws SQLException {
		LinkedList<String> ids = new LinkedList<>();
		GenericRawResults<String[]> rawResults = queryRaw(BLACKLIST_STMT);
		List<String[]> data = rawResults.getResults();
		
		for(String[] row : data) {
			ids.add(row[0]);
		}
		
		return ids;
	}
}
