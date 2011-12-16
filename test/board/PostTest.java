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
		post.processHtml(testData);
		
		assertTrue(post.hasImage());
		assertTrue(post.hasComment());
		
		assertThat(post.getComment(), is("You must spend 1 year alone in a small log cabin with the bare basics in the middle of no where with a character of your choice. Your not allowed to have any sexual relations of any kind with said character. You and your companion must gather your own food which is not too hard to find. Who do you choose?<br /><br />Hard mode: not your waifu"));
		assertThat(post.getImageName(), is("1301298882993.jpg"));
		assertThat(post.getImageUrl().toString(),is("http://images.4chan.org/a/src/1322860719840.jpg"));
	}
	
	@Test
	public void testReplyCommentNoImage() throws IOException {
		String testData = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData\\postTestData2")); // load test data
		post.processHtml(testData);
		
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
		post.processHtml(testData);
		
		assertTrue(post.hasImage());
		assertTrue(post.hasComment());
		
		assertThat(post.getComment(), is("<font class=\"unkfunc\"><a href=\"57866763#57866763\" class=\"quotelink\">&gt;&gt;57866763</a></font><br /><font class=\"unkfunc\">&gt;You must spend 1 year alone in a small log cabin with the bare basics in the middle of no where with a character of your choice.</font><br /><font class=\"unkfunc\">&gt;not your waifu</font><br />Well that's easy the-<br /><font class=\"unkfunc\">&gt;Your not allowed to have any sexual relations of any kind with said character</font><br />That's just not fair."));
		assertThat(post.getImageName(),is("1299649678890.jpg"));
		assertThat(post.getImageUrl().toString(),is("http://images.4chan.org/a/src/1322861012803.jpg"));
	}
	
	@Test
	public void testReplyImageOnly() throws IOException{
		String testData = new TextFileReader().read(this.getClass().getClassLoader().
				getResourceAsStream("HtmlData\\postTestData4")); // load test data
		post.processHtml(testData);
		
		assertTrue(post.hasImage());
		assertFalse(post.hasComment());
		
		assertNull(post.getComment());
		assertThat(post.getImageName(), is("Nausicaä 5.jpg"));
		assertThat(post.getImageUrl().toString(),is("http://images.4chan.org/a/src/1322861629050.jpg"));
	}
}
