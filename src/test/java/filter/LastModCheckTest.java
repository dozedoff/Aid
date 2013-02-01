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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.dao.CacheDAO;
import io.dao.LastModifiedDAO;
import io.tables.LastModified;

import java.net.URL;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;


public class LastModCheckTest {
	private static LastModifiedDAO lmdao;
	private static CacheDAO cacheDao;
	private LastModCheck lmc = null;
	private LastModified lastModEntry;
	
	private final String BASE_URL = "http://foo.bar/";
	private final String existingUrl = BASE_URL + 1;
	private final String newUrl = BASE_URL + 2;
	
	private final long INITIAL_TIMESTAMP = 1000;
	
	@Before
	public void setUp() throws Exception {
		lmdao = mock(LastModifiedDAO.class);
		cacheDao = mock(CacheDAO.class);

		when(lmdao.idExists(eq(existingUrl))).thenReturn(true);
		
		lastModEntry = new LastModified(existingUrl, new Date(5000L), new Date(6000L));
		when(lmdao.queryForId(eq(existingUrl))).thenReturn(lastModEntry);
		
		lmc = new LastModCheck(cacheDao, lmdao);
	}

	@Test
	public void testContains() {
		boolean contains = lmc.contains(existingUrl);
		assertThat(contains, is(true));
	}

	@Test
	public void testContainsNot() {
		boolean contains = lmc.contains(newUrl);
		assertThat(contains, is(false));
	}
	
	@Test
	public void testIsVisitNeededNewURL() {
		boolean response = lmc.isVisitNeeded(newUrl, 1000);
		assertThat(response, is(true));
	}
	
	@Test
	public void testIsVisitNeededExistingUrlNoChange() {
		boolean response = lmc.isVisitNeeded(existingUrl, 5000L);
		assertThat(response, is(false));
	}
	
	@Test
	public void testIsVisitNeededExistingUrlChangedBeforeLastVisit() {
		boolean response = lmc.isVisitNeeded(existingUrl, 5500L);
		assertThat(response, is(true));
	}
	
	@Test
	public void testIsVisitNeededExistingUrlChangedAfterLastVisit() {
		boolean response = lmc.isVisitNeeded(existingUrl, 7000L);
		assertThat(response, is(true));
	}
	
	@Test
	public void testIsVisitNeededExistingUrlNewURL() {
		lastModEntry.setLastmod(new Date(INITIAL_TIMESTAMP));
		boolean response = lmc.isVisitNeeded(existingUrl, 7000L);
		assertThat(response, is(true));
	}

	@Test
	public void testAddOrUpdateLastModified() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateLastVisit() throws Exception {
		lmc.updateLastVisit(new URL(existingUrl));
		verify(lmdao).update(any(LastModified.class));
	}
	
	@Test
	public void testUpdateLastVisitNewUrl() throws Exception {
		lmc.updateLastVisit(new URL(newUrl));
		verify(lmdao, never()).update(any(LastModified.class));
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
