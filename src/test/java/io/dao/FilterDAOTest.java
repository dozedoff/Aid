/*  Copyright (C) 2014  Nicholas Wright
	
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

import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.junit.Before;
import org.junit.Test;

import com.github.dozedoff.commonj.io.BoneConnectionPool;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.table.TableUtils;

import config.DefaultSQLiteConnection;
import filter.FilterItem;
import filter.FilterState;

public class FilterDAOTest {
	private FilterDAO sql;
	private BoneConnectionPool bcp;

	@Before
	public void setUp() throws Exception {
		String tmpDbPath = Files.createTempFile("FilterDAOtest", ".db").toString();
		bcp = new BoneConnectionPool(new DefaultSQLiteConnection(tmpDbPath), 10);
		bcp.startPool();

		DataSourceConnectionSource con = bcp.getConnectionSource();
		TableUtils.createTableIfNotExists(con, FilterItem.class);

		sql = DaoManager.createDao(bcp.getConnectionSource(), FilterItem.class);
		TableUtils.clearTable(bcp.getConnectionSource(), FilterItem.class);

		// setup data

		// <!-- filter Table 0: Deny 1: Pending 2: Allow 3: Unknown -->

		sql.create(new FilterItem(new URL("http://foo.bar/1"), "t", "just testing", FilterState.DENY));
		sql.create(new FilterItem(new URL("http://foo.bar/2"), "a", "foo bar", FilterState.PENDING));
		sql.create(new FilterItem(new URL("http://foo.bar/3"), "c", "yeti!", FilterState.ALLOW));
		sql.create(new FilterItem(new URL("http://foo.bar/4"), "m", "more test data", FilterState.UNKNOWN));
		sql.create(new FilterItem(new URL("http://foo.bar/5"), "vx", "and more", FilterState.PENDING));
		sql.create(new FilterItem(new URL("http://foo.bar/6"), "zb", "om nom nom", FilterState.PENDING));
		sql.create(new FilterItem(new URL("http://foo.bar/7"), "sa", "rain", FilterState.PENDING));
		sql.create(new FilterItem(new URL("http://foo.bar/8"), "p", "snow", FilterState.DENY));

		// sql.create(new FilterItem(new URL("http://foo.bar/ALLOW") ,"t" ,"test" ,FilterState.ALLOW));
		// sql.create(new FilterItem(new URL("http://foo.bar/DENY") ,"t" ,"test" ,FilterState.DENY));
		// sql.create(new FilterItem(new URL("http://foo.bar/PENDING") ,"t" ,"test" ,FilterState.PENDING));
		// sql.create(new FilterItem(new URL("http://foo.bar/UNKNOWN") ,"t" ,"test" ,FilterState.UNKNOWN));
	}

	@Test
	public void testUpdateFilterTimestampFilterItem() throws Exception {
		fail("not yet implemented");
	}

	@Test
	public void testUpdateFilterTimestampString() throws Exception {
		fail("not yet implemented");
	}

	@Test
	public void testGetOldestFilter() throws Exception {
		fail("not yet implemented");
	}

	@Test
	public void testGetPendingFilterCount() throws Exception {
		fail("not yet implemented");
	}

	@Test
	public void testGetPendingFilter() throws Exception {
		fail("not yet implemented");
	}

	@Test
	public void testAddFilterFilteritem() throws DataSetException, DatabaseUnitException, SQLException, Exception {
		// new FilterItem(board, reason, url, state)
		assertThat(sql.addFilter(new FilterItem(new URL("http://foo.bar/PENDING"), "t", "test", FilterState.PENDING)), is(true));
		assertThat(sql.addFilter(new FilterItem(new URL("http://foo.bar/ALLOW"), "t", "test", FilterState.ALLOW)), is(true));
		assertThat(sql.addFilter(new FilterItem(new URL("http://foo.bar/DENY"), "t", "test", FilterState.DENY)), is(true));
		assertThat(sql.addFilter(new FilterItem(new URL("http://foo.bar/UNKNOWN"), "t", "test", FilterState.UNKNOWN)), is(true));
		assertThat(sql.addFilter(new FilterItem(new URL("http://foo.bar/PENDING"), "t", "test", FilterState.PENDING)), is(false));
		assertThat(sql.addFilter(new FilterItem(new URL("http://foo.bar/1"), "t", "just testing", FilterState.ALLOW)), is(false));
	}
}
