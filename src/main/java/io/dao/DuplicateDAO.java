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

import io.tables.DuplicateRecord;
import io.tables.FileRecord;
import io.tables.IndexRecord;
import io.tables.LocationRecord;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import com.github.dozedoff.commonj.file.FileInfo;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

public class DuplicateDAO extends BaseDaoImpl<DuplicateRecord, String> {
	final String DUPLICATE_STMT = "SELECT dv.id, dv.dupeloc AS loc, dv.dupePath AS path FROM dupeview AS dv";
	final String ORIGINAL_STMT = "SELECT dv.id, dv.origloc AS loc, dv.origPath AS path  FROM dupeview AS dv";
	
	RawRowMapper<IndexRecord> indexMapper;
	RawRowMapper<DuplicateRecord> duplicateMapper;
	
	public DuplicateDAO(ConnectionSource cs) throws SQLException {
		super(cs, DuplicateRecord.class);
		createMappers();
	}
	
	private void createMappers() {
		indexMapper = new RawRowMapper<IndexRecord>() {

			@Override
			public IndexRecord mapRow(String[] columnNames,	String[] resultColumns) throws SQLException {
				FileInfo info = new FileInfo(Paths.get(resultColumns[2]));
				LocationRecord location= new LocationRecord(resultColumns[1]);
				
				IndexRecord index = new IndexRecord(info, location);
				index.setId(resultColumns[0]);
				
				return index;
			}
		};
		
		duplicateMapper = new RawRowMapper<DuplicateRecord>() {

			@Override
			public DuplicateRecord mapRow(String[] columnNames,	String[] resultColumns) throws SQLException {
				FileInfo info = new FileInfo(Paths.get(resultColumns[2]));
				LocationRecord location= new LocationRecord(resultColumns[1]);
				
				DuplicateRecord duplicate = new DuplicateRecord(info, location);
				duplicate.setId(resultColumns[0]);
				
				return duplicate;
			}
		};
	}
	
	public LinkedList<FileRecord> getDuplicatesAndOriginals() {
		LinkedList<FileRecord> records = new LinkedList<>();
		
		try {
			GenericRawResults<IndexRecord> rawIndex = queryRaw(ORIGINAL_STMT, indexMapper);
			GenericRawResults<DuplicateRecord> rawDuplicate = queryRaw(DUPLICATE_STMT, duplicateMapper);
		
			
			for(IndexRecord index : rawIndex){
				records.add(index);
			}
			
			for(DuplicateRecord duplicate : rawDuplicate){
				records.add(duplicate);
			}
			
			rawIndex.close();
			rawDuplicate.close();
		} catch (SQLException e) {
			records = new LinkedList<>();
			e.printStackTrace();
		}
		
		return records;
	}
	
	public boolean moveDuplicateToIndex(final String id) throws SQLException{
		final String SQL_COPY_INDEX_STATEMENT = "INSERT INTO fileindex SELECT * FROM fileduplicate WHERE id = ? LIMIT 1" ;
		final String SQL_DELETE_DUPLICATE_STATEMENT = "DELETE fd FROM fileduplicate AS fd JOIN fileindex AS fi ON fi.id=fd.id AND fi.dir=fd.dir AND fi.filename=fd.filename";
		
		return TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				updateRaw(SQL_COPY_INDEX_STATEMENT, id);
				updateRaw(SQL_DELETE_DUPLICATE_STATEMENT);
				return true;
			}
		});
	}
}
