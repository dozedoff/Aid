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

import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import io.tables.Cache;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;

public class CacheDAO extends BaseDaoImpl<Cache, String> {
	PreparedDelete<Cache> pruneCacheQuery;
	PreparedQuery<Cache> allDownloadedQuery;
	SelectArg timestamp, lastModIdPrep;

	public CacheDAO(ConnectionSource cSource) throws SQLException {
		super(cSource, Cache.class);
		createDownloadQueryStmt();
	}

	private void createDownloadQueryStmt() throws SQLException {
		lastModIdPrep = new SelectArg();
		QueryBuilder<Cache, String> qb = queryBuilder();
		allDownloadedQuery = qb.setCountOf(true).where().eq("last_mod_id", lastModIdPrep).and().eq("downloaded", false).prepare();
	}
	
	public boolean isDownloaded(URL url) throws SQLException{
		String cacheId = url.toString();
		Cache cache = new Cache(cacheId, true);
		List<Cache> queryResult = queryForMatching(cache);
		boolean downloaded = !queryResult.isEmpty();
		return downloaded;
	}
	
	public boolean areAllDownloaded(int lastModId) throws SQLException{
		lastModIdPrep.setValue(lastModId);
		long notDlCount = countOf(allDownloadedQuery);
		
		if(notDlCount > 0){
			return false;
		}else{
			return true;
		}
	}
}
