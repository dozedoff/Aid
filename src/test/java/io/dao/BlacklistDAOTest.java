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
import static org.junit.matchers.JUnitMatchers.hasItems;
import io.tables.BlacklistRecord;

import java.sql.SQLException;
import java.util.LinkedList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.dozedoff.commonj.io.BoneConnectionPool;
import com.j256.ormlite.dao.DaoManager;

import config.DefaultMySQLconnection;

public class BlacklistDAOTest {
	static BlacklistDAO dao = null;
	static BoneConnectionPool bcp = null;
	static String[] expectedBlocked = {"1", "2", "3", "4"};
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		bcp = new BoneConnectionPool(new DefaultMySQLconnection("127.0.0.1", 3306, "test", "test", "test"), 10);
		bcp.startPool();
		dao = DaoManager.createDao(bcp.getConnectionSource(), BlacklistRecord.class);
	}

	@Test
	public void testGetBlacklisted() throws SQLException {
		LinkedList<String> result = dao.getBlacklisted();
		assertThat(result, hasItems(expectedBlocked));
		assertThat(result.size(), is(4));
	}
}
