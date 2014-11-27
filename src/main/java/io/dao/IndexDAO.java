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

import io.tables.IndexRecord;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

public class IndexDAO extends BaseDaoImpl<IndexRecord, String> {
	public IndexDAO(ConnectionSource cs) throws SQLException {
		super(cs, IndexRecord.class);
	}

	public IndexRecord queryForFirst(IndexRecord index) throws SQLException {
		List<IndexRecord> records = queryForMatchingArgs(index);
		
		if(records.isEmpty()){
			return null;
		}else{
			return records.get(0);
		}
	}
	
	public boolean moveIndexToDuplicate(final String id) throws SQLException{
		final String SQL_COPY_INDEX_STATEMENT = "INSERT INTO fileduplicate SELECT * FROM fileindex WHERE id = ?";
		
		return TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				updateRaw(SQL_COPY_INDEX_STATEMENT, id);
				deleteById(id);
				return true;
			}
		});
	}
}
