package board;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import testDummy.DummyFilter;
import testDummy.DummyImageLoader;

@SuppressWarnings("unused")
public class PageTest {
	Page page;
	
	@Before
	public void setUp() throws Exception {
		page = new Page(new URL("http://foo.bar"), 2, new DummyFilter(), new DummyImageLoader());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetPageUrl() throws MalformedURLException {
		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/2")));
		
		page = new Page(new URL("http://foo.bar/"), 3, new DummyFilter(), new DummyImageLoader());
		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/3")));
		
		page = new Page(new URL("http://foo.bar/"), 0, new DummyFilter(), new DummyImageLoader());
		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/")));
		
		page = new Page(new URL("http://foo.bar"), 0, new DummyFilter(), new DummyImageLoader());
		assertThat(page.getPageUrl(), is(new URL("http://foo.bar/")));
	}

	@Test
	@Ignore
	public void testProcessPage() {
		fail("Not yet implemented");
	}

}
