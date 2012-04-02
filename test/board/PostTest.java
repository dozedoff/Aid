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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import io.TextFileReader;

import java.io.IOException;

import org.junit.Test;

public class PostTest {
	Post post = new Post();

	@Test
	public void testOPcommentAndImage() throws IOException {
		String testData = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData\\postTestData1")); // load test data
		post.parseHtml(testData);
		
		assertTrue(post.hasImage());
		assertTrue(post.hasComment());
		
		assertThat(post.getComment(), is("You must spend 1 year alone in a small log cabin with the bare basics in the middle of no where. <br /><br />Hard mode:"));
		assertThat(post.getImageName(), is("1301298882993.jpg"));
		assertThat(post.getImageUrl().toString(),is("http://images.4chan.org/a/src/1322860719840.jpg"));
	}
	
	@Test
	public void testReplyCommentNoImage() throws IOException {
		String testData = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData\\postTestData2")); // load test data
		post.parseHtml(testData);
		
		assertFalse(post.hasImage());
		assertTrue(post.hasComment());
		
		assertThat(post.getComment(), is("bear grylls"));
		assertNull(post.getImageName());
		assertNull(post.getImageUrl());
	}
	
	@Test
	public void testReplyQuoteCommentImageTripcode() throws IOException {
		String testData = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData\\postTestData3")); // load test data
		post.parseHtml(testData);
		
		assertTrue(post.hasImage());
		assertTrue(post.hasComment());
		
		assertThat(post.getComment(), is("<font class=\"unkfunc\"><a href=\"57866763#57866763\" class=\"quotelink\">&gt;&gt;57866763</a></font><br /><font class=\"unkfunc\">&gt;You must spend 1 year alone in a small log cabin with the bare basics in the middle of no where.</font><br /><font class=\"unkfunc\">&gt;</font><br />Well that's easy the-<br /><font class=\"unkfunc\">&gt;</font><br />That's just not fair."));
		assertThat(post.getImageName(),is("1299649678890.jpg"));
		assertThat(post.getImageUrl().toString(),is("http://images.4chan.org/a/src/1322861012803.jpg"));
	}
	
	@Test
	public void testReplyImageOnly() throws IOException{
		String testData = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData\\postTestData4")); // load test data
		post.parseHtml(testData);
		
		assertTrue(post.hasImage());
		assertFalse(post.hasComment());
		
		assertNull(post.getComment());
		assertThat(post.getImageName(), is("Nausica� 5.jpg"));
		assertThat(post.getImageUrl().toString(),is("http://images.4chan.org/a/src/1322861629050.jpg"));
	}
	
	@Test
	public void testReplyCloudfire() throws IOException{
		String testData = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData\\postTestData4")); // load test data
		post.parseHtml(testData);
		
		assertTrue(post.hasImage());
		assertFalse(post.hasComment());
		
		assertNull(post.getComment());
		assertThat(post.getImageName(), is("Nausica� 5.jpg"));
		assertThat(post.getImageUrl().toString(),is("http://images.4chan.org/a/src/1322861629050.jpg"));
	}
}
