package board;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import io.ImageLoader;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import filter.Filter;

public class PageTest {
	Page page;
	
	Filter mockFilter = mock(Filter.class);
	ImageLoader mockImageLoader = mock(ImageLoader.class);
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetPageUrl() throws MalformedURLException {
		page = new Page(new URL("http://foo.bar/"), 1, mockFilter, mockImageLoader);
		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/1")));
		
		page = new Page(new URL("http://foo.bar/"), 2, mockFilter, mockImageLoader);
		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/2")));
		
		page = new Page(new URL("http://foo.bar/"), 3, mockFilter, mockImageLoader);
		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/3")));
		
		page = new Page(new URL("http://foo.bar/"), 0, mockFilter, mockImageLoader);
		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/")));
		
		page = new Page(new URL("http://foo.bar"), 0, mockFilter, mockImageLoader);
		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/")));
	}

	@Test
	@Ignore
	public void testProcessPage() {
		fail("Not yet implemented");
	}
	
	@Test
	public void testEquals() throws MalformedURLException {
		Page page0 = new Page(new URL("http://foo.bar/"), 0, mockFilter, mockImageLoader);
		Page page1 = new Page(new URL("http://foo.bar/"), 1, mockFilter, mockImageLoader);
		Page page2 = new Page(new URL("http://foo.bar/"), 2, mockFilter, mockImageLoader);
		Page page_1 = new Page(new URL("http://foo.bar/"), 1, mockFilter, mockImageLoader);
		Page page_2 = new Page(new URL("http://foo.bar/"), 2, mockFilter, mockImageLoader);
		
		assertThat(page0.equals(page1), is(false));
		assertThat(page1.equals(page0), is(false));
		
		assertThat(page1.equals(page_1), is(true));
		assertThat(page_1.equals(page1), is(true));
		assertThat(page_2.equals(page1), is(false));
		
		assertThat(page2.equals(page_2), is(true));
	}
}
