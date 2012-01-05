package app;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class MainTest {
	Main main;
	Properties mockAppSettings;
	
	@Before
	public void setup() throws Exception{
		main = new Main();
		
		mockAppSettings = new Properties();
		
		mockAppSettings.setProperty("page_threads","1");
		mockAppSettings.setProperty("image_threads","1");
		mockAppSettings.setProperty("write_Blocked","false");
		mockAppSettings.setProperty("base_url","http://foo.bar/");
		mockAppSettings.setProperty("sub_pages","a;15,w;15,wg;15");
		
		mockAppSettings = spy(mockAppSettings);
//		when(mockAppSettings.getProperty("page_threads",anyString())).thenReturn("1");
//		when(mockAppSettings.getProperty("image_threads",anyString())).thenReturn("1");
//		when(mockAppSettings.getProperty("write_Blocked",anyString())).thenReturn("false");
//		when(mockAppSettings.getProperty("base_url",anyString())).thenReturn("http://foo.bar/");
//		when(mockAppSettings.getProperty("sub_pages",anyString())).thenReturn("a;15,w;15,wg;15");
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
		main.validateAppSettings(mockAppSettings);
		verify(mockAppSettings,times(0)).setProperty(anyString(), anyString());
	}

}
