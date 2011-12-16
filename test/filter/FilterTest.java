package filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import gui.BlockListDataModel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import testDummy.DummyConnectionPool;
import testDummy.DummyPost;
import testDummy.DummyThumbnailLoader;

public class FilterTest {
	DummyConnectionPool dc;
	Filter filter;
	TemporaryFolder tempFolder = new TemporaryFolder();
	final String TEST_FILE_NAME = "test_file";
	File testFile;
	
	String testName[] = {"foo","bar"};
	String testContent[] = {"oof","rab"};
	
	URL testURL;
			
	@Before
	public void setUp() throws Exception {
		dc = new DummyConnectionPool();
		filter = new Filter(dc, new BlockListDataModel(),new DummyThumbnailLoader());
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
		filter = new Filter(dc,new BlockListDataModel(),new DummyThumbnailLoader());
		
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
		DummyPost dp = new DummyPost("test.png", new URL("http://foo.bar/yeti/1234"), "just testing, foo bar");
		
		assertNull(filter.checkPost(dp));
		
		filter.addFileNameFilterItem("test");
		assertNotNull(filter.checkPost(dp));
		assertThat(filter.checkPost(dp), is("file name, test"));
		filter.removeFileNameFilterItem("test");
		
		filter.addPostContentFilterItem("foo");
		assertNotNull(filter.checkPost(dp));
		assertThat(filter.checkPost(dp), is("post content, foo"));
	}
}