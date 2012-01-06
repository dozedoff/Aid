package app;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import config.DefaultAppSettings;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

@RunWith(Parameterized.class)
public class MainTest {
	Main main;
	Properties appSettings;
	
	String pageThreads, imageThreads, writeBlocked, BaseUrl, subpages;
	
	@Parameters 
	public static Collection<Object[]> params() {
		
	return Arrays.asList(new Object[][] {
			 {"1", 	"1",	"false",	"http://foo.bar",	"a;15,b;14"},
			 {"0", 	"1",	"false",	"http://foo.bar",	"a;15,b;14"},
			 {"-1", "1",	"false",	"http://foo.bar",	"a;15,b;14"},
			 {"1", 	"0",	"false",	"http://foo.bar",	"a;15,b;14"},
			 {"1", 	"-1",	"false",	"http://foo.bar",	"a;15,b;14"},
			 {"1", 	"1",	"true",		"http://foo.bar",	"a;15,b;14"},
			 {"1", 	"1",	"fdfg",		"http://foo.bar",	"a;15,b;14"},
			 {"1", 	"1",	"3455",		"http://foo.bar",	"a;15,b;14"},
			 {"1", 	"1",	"false",	"http:/foo.bar",	"a;15,b;14"},
			 {"1", 	"1",	"false",	"foo.bar",			"a;15,b;14"},
			 {"1", 	"1",	"false",	"http://foobar",	"a;15,b;14"},
			 {"1", 	"1",	"false",	"http://foo.bar",	"a;15b;14"},
			 {"1", 	"1",	"false",	"http://foo.bar",	"a;15"},
			 {"1", 	"1",	"false",	"http://foo.bar",	"a,15,b,14"},
			 {"1", 	"1",	"false",	"http://foo.bar",	"a;15;b;14"}
		});
	}
	
	public MainTest(String pageThreads, String imageThreads, String writeBlocked, String baseUrl, String subpages) {
		this.pageThreads = pageThreads;
		this.imageThreads = imageThreads;
		this.writeBlocked = writeBlocked;
		BaseUrl = baseUrl;
		this.subpages = subpages;
	}



	@Before
	public void setup() throws Exception{
		main = new Main();
	}
	
	@Test
	@Ignore
	public void testLoadLoggerConfig() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testLoadMySqlConfig() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public void testLoadAppConfig() {
		fail("Not yet implemented");
	}

	@Test
	public void testValidateAppSettings() {
		appSettings = spy(makeAppSettings(pageThreads, imageThreads, writeBlocked, BaseUrl, subpages));
		main.validateAppSettings(appSettings);
		assertThat(appSettings, is((Properties)new DefaultAppSettings()));
	}
	
	/**
	 * Construct a new property Object from parameters-
	 * @param pageThreads
	 * @param imageThreads
	 * @param writeBlocked
	 * @param BaseUrl
	 * @param subpages
	 * @return
	 */
	private Properties makeAppSettings(String pageThreads, String imageThreads, String writeBlocked, String BaseUrl, String subpages){
		Properties setting = new Properties();
		
		setting.setProperty("page_threads","1");
		setting.setProperty("image_threads","1");
		setting.setProperty("write_Blocked","false");
		setting.setProperty("base_url","http://boards.4chan.org/");
		setting.setProperty("sub_pages","a;15,w;15,wg;15");
		
		return setting;
	}
}
