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
package filter;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.dao.CacheDAO;
import io.dao.LastModifiedDAO;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class LastModCheckTest {
	private static LastModifiedDAO lmdao;
	private static CacheDAO cacheDao;
	private LastModCheck lmc = null;
	
	private final String BASE_URL = "http://foo.bar/";
	
	@Before
	public void setUp() throws Exception {
		lmdao = mock(LastModifiedDAO.class);
		cacheDao = mock(CacheDAO.class);

		when(lmdao.idExists(BASE_URL+1)).thenReturn(true);
		when(lmdao.idExists(Mockito.anyString())).thenReturn(false);
		
		lmc = new LastModCheck(cacheDao, lmdao);
	}

	@Test
	public void testContains() {
		lmc.contains("http://foo.bar/1");
	}

	@Test
	public void testContainsNot() {
		lmc.contains("http://foo.bar/2");
	}
	
	@Test
	public void testIsVisitNeeded() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddOrUpdateLastModified() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateLastVisit() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCacheLastModId() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddCacheLinks() {
		fail("Not yet implemented");
	}

	@Test
	public void testRecordNewThreads() {
		fail("Not yet implemented");
	}

	@Test
	public void testAreAllDownloaded() {
		fail("Not yet implemented");
	}

}
