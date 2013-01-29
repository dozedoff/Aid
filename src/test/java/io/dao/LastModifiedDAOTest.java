/*  Copyright (C) 2013  Nicholas Wright
	
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
import static org.junit.Assert.fail;
import io.tables.Cache;
import io.tables.LastModified;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.dozedoff.commonj.io.BoneConnectionPool;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;

import config.DefaultMySQLconnection;

public class LastModifiedDAOTest {
	private static LastModifiedDAO dao = null;
	private static CacheDAO cacheDao = null;
	private static BoneConnectionPool bcp = null;
	
	private final int SAMPLE_NO = 4;
	private final String BASE_URL = "http://foo.bar/";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bcp = new BoneConnectionPool(new DefaultMySQLconnection("127.0.0.1", 3306, "test", "test", "test"), 10);
		bcp.startPool();
		dao = DaoManager.createDao(bcp.getConnectionSource(), LastModified.class);
		cacheDao = DaoManager.createDao(bcp.getConnectionSource(), Cache.class);
	}

	@Before
	public void setUp() throws Exception {
		TableUtils.clearTable(bcp.getConnectionSource(), Cache.class);
		TableUtils.clearTable(bcp.getConnectionSource(), LastModified.class);
		
		for (int i=1; i < SAMPLE_NO+1; i++){
			Cache cache = new Cache(BASE_URL + i, false);
			cache.setLast_mod_id(1);
			cacheDao.create(cache);
		}

		LastModified lm = new LastModified("http://foo.bar/12345", new Date(), new Date(5000));
		dao.create(lm);
	}

	@Test
	public void testPruneLastMod() throws SQLException {
		assertThat(cacheDao.countOf(), is(4L)); // guard condition
		assertThat(dao.countOf(), is(1L));
		
		dao.pruneLastMod(new Date().getTime());
		
		assertThat("Is the database trigger set?", cacheDao.countOf(), is(0L));
		assertThat(dao.countOf(), is(0L));
	}
	
	@Test
	public void testPruneLastModNoPrune() throws SQLException {
		assertThat(cacheDao.countOf(), is(4L)); // guard condition
		assertThat(dao.countOf(), is(1L));
		
		dao.pruneLastMod(new Date(4000).getTime());
		
		assertThat(cacheDao.countOf(), is(4L));
		assertThat(dao.countOf(), is(1L));
	}

}
