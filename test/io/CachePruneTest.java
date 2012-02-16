/*  Copyright (C) 2012  Nicholas Wright

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
package io;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CachePruneTest {
	CachePrune cachePrune;
	MySQLaid sql;
	@Before
	public void setUp() throws Exception {
		sql = mock(MySQLaid.class);
		cachePrune = new CachePrune(sql, new URL("http://www.google.com"), 1, 0, 1);
	}

	@After
	public void tearDown() throws Exception {
		cachePrune.stop();
	}

	@Test
	public void test() throws InterruptedException {
		when(sql.size(MySQLtables.Cache)).thenReturn(4);
		assertThat(cachePrune.start(), is(true));
		Thread.sleep(1000*3);
		
		verify(sql,times(1)).pruneCache(anyLong());
	}

}
