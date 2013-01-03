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
import java.util.Date;

import io.tables.LastModified;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;

public class LastModifiedDAO extends BaseDaoImpl<LastModified, String> {
	PreparedDelete<LastModified> pruneCacheQuery;
	SelectArg timestamp;
	
	public LastModifiedDAO(ConnectionSource cSource) throws SQLException {
		super(cSource, LastModified.class);
		timestamp = new SelectArg();
		DeleteBuilder<LastModified, String> del = deleteBuilder();
		del.where().le("timestamp", timestamp);
		pruneCacheQuery = del.prepare();
	}
	
	public int pruneLastMod(long timestampInMillis) throws SQLException {
		Date date = new Date(timestampInMillis);
		timestamp.setValue(date);
		int updated = delete(pruneCacheQuery);
		return updated;
	}
}
