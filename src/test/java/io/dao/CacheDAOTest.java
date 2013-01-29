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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.tables.Cache;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.dozedoff.commonj.io.BoneConnectionPool;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;

import config.DefaultMySQLconnection;

public class CacheDAOTest {
	private static CacheDAO dao = null;
	private static BoneConnectionPool bcp = null;
	
	private final int SAMPLE_NO = 4;
	private final String BASE_URL = "http://foo.bar/";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bcp = new BoneConnectionPool(new DefaultMySQLconnection("127.0.0.1", 3306, "test", "test", "test"), 10);
		bcp.startPool();
		dao = DaoManager.createDao(bcp.getConnectionSource(), Cache.class);
		TableUtils.clearTable(bcp.getConnectionSource(), Cache.class);
	}
	
	@Before
	public void setUp() throws SQLException {
		TableUtils.clearTable(bcp.getConnectionSource(), Cache.class);
		
		for (int i=1; i < SAMPLE_NO+1; i++){
			Cache cache = new Cache(BASE_URL + i, false);
			cache.setLast_mod_id(1);
			dao.create(cache);
		}
		
		// Create one entry that has already been downloaded
		dao.create(new Cache(String.valueOf(BASE_URL + (SAMPLE_NO + 1))));
	}

	@Test
	public void testIsDownloaded() throws MalformedURLException, SQLException {
		URL one = new URL(BASE_URL + 1);
		assertThat("URL: " + one,dao.isDownloaded(one), is(false));
	}
	
	@Test
	public void testIsNotDownloaded() throws MalformedURLException, SQLException {
		URL two = new URL(BASE_URL + (SAMPLE_NO + 1));
		assertThat("URL: " + two,dao.isDownloaded(two), is(true));
	}

	@Test
	public void testAreAllDownloaded() throws SQLException {
		assertThat(dao.areAllDownloaded(1), is(false)); // guard condition
		
		setTableDownloadStatus(true);
		assertThat(dao.areAllDownloaded(1), is(true));
	}
	
	private void setTableDownloadStatus(boolean downloaded) throws SQLException {
		List<Cache> cacheTable = dao.queryForAll();

		for (Cache cache : cacheTable) {
			cache.setDownloaded(downloaded);
			dao.update(cache);
		}
	}
	
	@Test
	public void testAreAllDownloadedNonExistantModId() throws SQLException {
		assertThat(dao.areAllDownloaded(2), is(true));
	}
}
