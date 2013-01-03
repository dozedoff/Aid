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
package board;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;

import io.ImageLoader;

import java.net.URL;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import filter.CacheCheck;
import filter.Filter;
import filter.LastModCheck;

public class BoardTest {
	Board board;
	
	SiteStrategy strategy;
	Filter filter;
	CacheCheck cacheCheck;
	ImageLoader imageLoader;
	LastModCheck lmc;
	
	@Before
	public void setup() throws Exception {
		strategy = mock(SiteStrategy.class);
		filter = mock(Filter.class);
		imageLoader = mock(ImageLoader.class);
		lmc = mock(LastModCheck.class);
		cacheCheck = mock(CacheCheck.class);
		board = new Board(new URL("http://foo.bar/"), "t", strategy, filter, imageLoader, lmc, cacheCheck);
	}
	
	@Test
	public void testToString() {
		assertThat(board.toString(), is("/t/"));
	}

	@Test
	public void testGetStatus() {
		assertThat(board.getStatus(), is("/t/  idle"));
		board.start();
		assertThat(board.getStatus(), containsString("running"));
		board.stop();
		assertThat(board.getStatus(), is("/t/  idle"));
	}
}
