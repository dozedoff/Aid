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
package filter;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

public class FilterItemTest {
	FilterItem filterItem;
	@Before
	public void setUp() throws Exception {
		URL url = new URL("http://foo.bar/test/12345");
		filterItem = new FilterItem(url,"foo", "bar",  FilterState.PENDING);
	}

	@Test
	public void testToString() {
		String format = "%1$-5s %2$-10s %3$-30s";
		assertEquals(String.format(format, "foo", "12345", "bar"), filterItem.toString());
	}

}
