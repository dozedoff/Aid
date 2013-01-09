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

import io.tables.DirectoryPathRecord;

import java.nio.file.Path;
import java.sql.SQLException;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;

public class DirectoryPathDAO extends BaseDaoImpl<DirectoryPathRecord, Integer>{
	String rawQuery = "INSERT INTO dirlist (dirpath) SELECT ? FROM dirlist WHERE NOT EXISTS(SELECT dirpath FROM dirlist WHERE dirpath = ?)LIMIT 1";
	PreparedQuery<DirectoryPathRecord> prepQueryPath;
	SelectArg dirArg = new SelectArg();
	
	public DirectoryPathDAO(ConnectionSource connectionSource) throws SQLException {
		super(connectionSource, DirectoryPathRecord.class);
		initialize();
		prepQueryPath = queryBuilder().where().eq("dirpath", dirArg).prepare();
	}
	
	private String convertToLinuxPath(String path) {
		String linuxPath = path.replaceAll("\\\\", "/");
		return linuxPath;
	}
	
	public DirectoryPathRecord add(Path relativePath) throws SQLException {
		String pathArg = convertToLinuxPath(relativePath.toString());
		queryRaw(rawQuery, pathArg, pathArg);
		dirArg.setValue(pathArg);
		DirectoryPathRecord dirRec = queryForFirst(prepQueryPath);
		return dirRec;
	}
}
