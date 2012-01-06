package app;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import config.DefaultAppSettings;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

public class MainTest {
	Main main;
	Properties appSettings;
	
	String pageThreads, imageThreads, writeBlocked, BaseUrl, subpages;
	int interacions;
	
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

	@Test //TODO run this test with datapoints / parameters, also add verification number
	public void testValidateAppSettings() {
		appSettings = spy(makeAppSettings(pageThreads, imageThreads, writeBlocked, BaseUrl, subpages));
		main.validateAppSettings(appSettings);
		verify(appSettings,times(interacions)).setProperty(anyString(), anyString()); // potential problem, as values aren't checked...
		//TODO add corrected DefaultAppSetting to verify settings
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
