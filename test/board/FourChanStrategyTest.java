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
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

public class FourChanStrategyTest {
	SiteStrategy strategy;

	@Before
	public void setUp() throws Exception {
		strategy = new FourChanStrategy();
	}

	@Test
	public void testValidSiteStrategy() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindBoards() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetBoardPageCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testParsePage() throws Exception {
		fail("Not yet implemented");
	}

	@Test
	public void testParseThread() {
		fail("Not yet implemented");
	}

}
