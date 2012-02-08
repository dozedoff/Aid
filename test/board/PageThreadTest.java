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
package board;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import io.TextFileReader;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class PageThreadTest {
	PageThread pt;
	
	String[] testImageNames = {"1301298882993.jpg","1299649678890.jpg","1310239670786.jpg","giant holo.jpg","1309568970939.jpg","chobits3.jpg","kino.jpg","[GomenRider]_Garo_-_23_[C19EC6CB].mkv_snapshot_18.48_[2011.12.02_22.39.14].jpg","ge.jpg"};
	String[] testComments = {"bear grylls","Guts","best choice","Sanji from One Piece.","Kino. Easy.","Kotomine Kirei.<br />We'd just troll random hikers/campers in our forest.","Kouga"};
	@Before
	public void setUp() throws Exception{
			pt  = new PageThread(new URL("http://foo.bar/a/src/12345"));
	}
	
	@Test
	public void testGetThreadNumber() {
		String test = pt.getBoardDesignation();
		assertEquals("a", test);
	}

	@Test
	public void testGetBoardDeclaration() {
		int test = pt.getThreadNumber();
		assertEquals(12345, test);
	}
	
	@Test
	public void testProcessThread() throws IOException{
		String testData = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData\\htmlTestData")); // load test data
		testData = testData.replaceAll("\n", ""); // test file is in human readable format, this is to simulate how the program would receive the data

		int images = 0, comments = 0;
		LinkedList<String> commentStrings = new LinkedList<>();
		LinkedList<String> imageNames = new LinkedList<>();
		
		
		
		
		pt.processThread(testData);	// process test data
		
		assertThat(pt.getPosts().size(),is(18)); // check for the correct number of posts
		
		for(Post p : pt.getPosts()){	// count comments and images
			if(p.hasComment()){
				comments++;
				commentStrings.add(p.getComment());
			}
			
			if(p.hasImage()){
				images++;
				imageNames.add(p.getImageName());
			}
		}
		
		assertThat(comments, is(17)); 	// check number of comments
		assertThat(images,is(10));		// check number of images
		
		assertThat(imageNames,hasItems(testImageNames));
		assertThat(commentStrings,hasItems(testComments));
	}	
}

