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
package io;

import static org.junit.Assert.*;

import java.net.URL;

import net.DownloadItem;

import org.junit.Before;
import org.junit.Test;

public class ImageItemTest {
	DownloadItem imageItem;
	@Before
	public void setUp() throws Exception{
		imageItem = new DownloadItem(new URL("http://foo.bar/test/"), "testImg.jpg");
	}
	
	@Test
	public void testEqualsObject() throws Exception{
		assertFalse(imageItem.equals(null));
		
		DownloadItem testItem = new DownloadItem(new URL("http://foo.bar/test/"), "testImg.jpg");
		assertTrue(imageItem.equals(testItem));
		
		testItem = new DownloadItem(new URL("http://foo.bar/test/"), "Img.jpg");
		assertFalse(imageItem.equals(testItem));
		
		testItem = new DownloadItem(new URL("http://foo.bar/"), "testImg.jpg");
		assertFalse(imageItem.equals(testItem));
	}
}
