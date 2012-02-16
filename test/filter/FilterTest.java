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
package filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gui.BlockListDataModel;
import io.BoneConnectionPool;
import io.MySQLaid;
import io.ThumbnailLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

import javax.swing.DefaultListModel;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import board.Post;

public class FilterTest {
	MySQLaid mockMySqlAid = mock(MySQLaid.class);
	ThumbnailLoader mockThumbnailLoader = mock(ThumbnailLoader.class);
	
	DefaultListModel<String> fileNameModel;
	DefaultListModel<String> postContentModel;
	
	Filter filter;
	static File tempFolder;
	final static String TEST_FILE_NAME = "test_file";
	static File testFile;
	
	String testName[] = {"foo","bar"};
	String testContent[] = {"oof","rab"};
	
	URL testURL;
	
	@BeforeClass
	public static void createTestFile() throws IOException{
		tempFolder = Files.createTempDirectory("FilterTest").toFile();
		testFile = new File(tempFolder,TEST_FILE_NAME);
		testFile.createNewFile();
	}
			
	@Before
	public void setUp() throws Exception {
		fileNameModel = new DefaultListModel<>();
		postContentModel = new DefaultListModel<>();
		filter = new Filter(mockMySqlAid, new BlockListDataModel(),fileNameModel, postContentModel, mockThumbnailLoader);

		testURL = new URL("http://foo.bar/test/12345");
	}

	@Test
	public void testSaveFilter() throws IOException {
		assertTrue(filter.saveFilter(testFile.toString()));
		assertFalse(filter.saveFilter(""));
	}

	@Test
	public void testLoadFilter() throws Exception {
		// add test data
		for(String s : testName)
			filter.addFileNameFilterItem(s);
		for(String s : testContent)
			filter.addPostContentFilterItem(s);
		
		// save test data
		filter.saveFilter(testFile);
		
		// new Filter & model to clear data
		setUp();
		
		//should be empty now
		assertThat(fileNameModel.size(), is(0));
		assertThat(postContentModel.size(), is(0));
		
		
		assertFalse(filter.loadFilter("")); // should not work
		assertTrue(filter.loadFilter(testFile)); // reload data
		
		// check if data is still the same
		assertThat(postContentModel.size(),is(2));
		assertThat(fileNameModel.size(),is(2));
		
		assertThat(postContentModel.contains("oof"), is(true));
		assertThat(postContentModel.contains("rab"), is(true));
		assertThat(postContentModel.contains("foo"), is(false));
		assertThat(postContentModel.contains("bar"), is(false));
		
		assertThat(fileNameModel.contains("foo"), is(true));
		assertThat(fileNameModel.contains("bar"), is(true));
		assertThat(fileNameModel.contains("oof"), is(false));
		assertThat(fileNameModel.contains("rab"), is(false));
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