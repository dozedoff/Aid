package io;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

public class ImageItemTest {
	ImageItem imageItem;
	@Before
	public void setUp() throws Exception{
		imageItem = new ImageItem(new URL("http://foo.bar/test/"), "testImg.jpg");
	}
	
	@Test
	public void testEqualsObject() throws Exception{
		assertFalse(imageItem.equals(null));
		
		ImageItem testItem = new ImageItem(new URL("http://foo.bar/test/"), "testImg.jpg");
		assertTrue(imageItem.equals(testItem));
		
		testItem = new ImageItem(new URL("http://foo.bar/test/"), "Img.jpg");
		assertFalse(imageItem.equals(testItem));
		
		testItem = new ImageItem(new URL("http://foo.bar/"), "testImg.jpg");
		assertFalse(imageItem.equals(testItem));
	}
}
