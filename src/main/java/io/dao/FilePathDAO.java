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

import io.tables.FilePathRecord;

import java.nio.file.Path;
import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;

public class FilePathDAO extends BaseDaoImpl<FilePathRecord, Integer> {
	String rawQuery = "INSERT INTO filelist (filename) SELECT ? FROM filelist WHERE NOT EXISTS(SELECT filename FROM filelist WHERE filename = ?) LIMIT 1";
	SelectArg fileArg = new SelectArg();
	PreparedQuery<FilePathRecord> prepQueryPath;
	
	public FilePathDAO(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, FilePathRecord.class);
		initialize();
		prepQueryPath = queryBuilder().where().eq("filename", fileArg).prepare();
	}
	
	public FilePathRecord add(Path filename) throws SQLException {
		String pathArg = filename.toString();
		queryRaw(rawQuery, pathArg, pathArg);
		fileArg.setValue(pathArg);
		FilePathRecord fileRec = queryForFirst(prepQueryPath);
		return fileRec;
	}
}
