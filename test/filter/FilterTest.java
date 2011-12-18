package filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.*;
import gui.BlockListDataModel;

import io.ConnectionPoolaid;
import io.ThumbnailLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import board.Post;

public class FilterTest {
	ConnectionPoolaid mockConnectionPoolaid = mock(ConnectionPoolaid.class);
	ThumbnailLoader mockThumbnailLoader = mock(ThumbnailLoader.class);
	
	Filter filter;
	TemporaryFolder tempFolder = new TemporaryFolder();
	final String TEST_FILE_NAME = "test_file";
	File testFile;
	
	String testName[] = {"foo","bar"};
	String testContent[] = {"oof","rab"};
	
	URL testURL;
			
	@Before
	public void setUp() throws Exception {
		filter = new Filter(mockConnectionPoolaid, new BlockListDataModel(),mockThumbnailLoader);
		testFile = tempFolder.newFile(TEST_FILE_NAME);
		testURL = new URL("http://foo.bar/test/12345");
	}

	@Test
	public void testSaveFilter() throws IOException {
		assertTrue(filter.saveFilter(testFile.toString()));
		assertFalse(filter.saveFilter(""));
	}

	@Test
	public void testLoadFilter() {
		// add test data
		for(String s : testName)
			filter.addFileNameFilterItem(s);
		for(String s : testContent)
			filter.addPostContentFilterItem(s);
		
		// save test data
		filter.saveFilter(testFile);
		
		// new Filter item to clear data
		filter = new Filter(mockConnectionPoolaid,new BlockListDataModel(),mockThumbnailLoader);
		
		//should be empty now
		assertThat(new ArrayList<String>(), is(filter.getFileNameFilterItem()));
		assertThat(new ArrayList<String>(), is(filter.getPostContentFilterItem()));
		
		
		assertFalse(filter.loadFilter("")); // should not work
		assertTrue(filter.loadFilter(testFile)); // reload data
		
		// check if data is still the same
		assertThat(filter.getPostContentFilterItem(),hasItems(testContent));
		assertThat(filter.getFileNameFilterItem(),hasItems(testName));
	}

	@Test
	public void testCheckPost() throws IOException {
		Post mockPost = mock(Post.class);
		when(mockPost.getImageName()).thenReturn("test.png");
		when(mockPost.getImageUrl()).thenReturn(new URL("http://foo.bar/yeti/1234"));
		when(mockPost.getComment()).thenReturn("just testing, foo bar");
		when(mockPost.hasComment()).thenReturn(true);
		when(mockPost.hasImage()).thenReturn(true);
		
		assertNull(filter.checkPost(mockPost));
		
		filter.addFileNameFilterItem("test");
		assertNotNull(filter.checkPost(mockPost));
		assertThat(filter.checkPost(mockPost), is("file name, test"));
		filter.removeFileNameFilterItem("test");
		
		filter.addPostContentFilterItem("foo");
		assertNotNull(filter.checkPost(mockPost));
		assertThat(filter.checkPost(mockPost), is("post content, foo"));
	}
}