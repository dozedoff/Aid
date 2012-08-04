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

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("unused")
public class PageFactoryTest {
	private final int NUM_OF_PAGES = 5;
	private String[] testStrings = {"http:\\foo.bar",
						"http:\\foo.bar\\2",
						"http:\\foo.bar\\3",
						"http:\\foo.bar\\4",
						"http:\\foo.bar\\5"
	};

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	@Ignore
	public void testMakePages() {
		//TODO re-write test
//		ArrayList<Page> pages = PageFactory.makePages("http:\\foo.bar", NUM_OF_PAGES);
//		
//		assertEquals(NUM_OF_PAGES, pages.size());  // correct number of pages?
//		
//		ArrayList<String> urlStrings = new ArrayList<>(NUM_OF_PAGES);
//		//convert to strings for testing
//		for(Page p : pages)
//			urlStrings.add(p.getPageUrl().toString());
//			
//		assertThat(urlStrings,hasItems(testStrings));
	}

}
