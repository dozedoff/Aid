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
package app;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SettingValidatorTest {
	Properties appSettings;
	
	String pageThreads, imageThreads, writeBlocked, BaseUrl, subpages;
	boolean expectedResponse;
	
	@Parameters 
	public static Collection<Object[]> params() {
		
	return Arrays.asList(new Object[][] {
				// Test 0 to 2 are sanity tests
			
				// Correct
			 {true,		"1", 	"1",	"false",	"http://foo.bar/",	"a;15,b;14"}, 					// 0
			 {true,		"5", 	"6",	"true",		"http://f00.b4r.jelly.beans.net/",	"a;10,b;14"}, 	// 1
			 
			 	// Wrong
			 {false,	"-1", 	"0",	"dfg4",		"",	"fxghdfgh%54q456ç%&"}, 							// 2
			 
			 	// Page threads
			 {false,	"0", 	"1",	"false",	"http://foo.bar/",	"a;15,b;14"}, 					// 3
			 {false,	"-1", 	"1",	"false",	"http://foo.bar/",	"a;15,b;14"},					// 4
			 {false,	"a", 	"1",	"false",	"http://foo.bar/",	"a;15,b;14"}, 					// 5
			 
			 	// Image threads
			 {false,	"1", 	"0",	"false",	"http://foo.bar/",	"a;15,b;14"},					// 6
			 {false,	"1", 	"-1",	"false",	"http://foo.bar/",	"a;15,b;14"},					// 7
			 {false,	"1", 	"a",	"false",	"http://foo.bar/",	"a;15,b;14"}, 					// 8
			 
			 	// Write blocked
			 {false,	"1", 	"1",	"beans",	"http://foo.bar/",	"a;15,b;14"},					// 9
			 {false,	"1", 	"1",	"32423",	"http://foo.bar/",	"a;15,b;14"},					// 10
			 
			 	// base URL
			 {false,	"1", 	"1",	"FALSE",	"http://foo.bar",	"a;15,b;14"},					// 11
			 {false,	"1", 	"1",	"TRUE",		"http://foobar/",	"a;15,b;14"},					// 12
			 {false,	"1", 	"1",	"false",	"http:/foo.bar/",	"a;15,b;14"},					// 13
			 {false,	"1", 	"1",	"false",	"http//foo.bar/",	"a;15,b;14"},					// 14
			 {false,	"1", 	"1",	"false",	"htttp://foo.bar/",	"a;15,b;14"},					// 15
			 {true,		"1", 	"1",	"false",	"http://foo.bar.net/",	"a;15,b;14"},				// 16
			 {false,	"1", 	"1",	"false",	"http://f00.b4r/",	"a;15,b;14"},					// 17
			 {true,		"1", 	"1",	"false",	"http://f00.b4r.jelly.net/",	"a;15,b;14"},		// 18
			 {false,	"1", 	"1",	"false",	"foo.bar/",	"a;15,b;14"},							// 19
			 {false,	"1", 	"1",	"false",	"http://foo.bar1/",	"a;15,b;14"},					// 20
			 
			 	// sub pages
			 {false,	"1", 	"1",	"false",	"http://foo.bar/",	"a,15,b;14"},					// 21
			 {false,	"1", 	"1",	"false",	"http://foo.bar/",	"a;15,,b;14"},					// 22
			 {true,		"1", 	"1",	"false",	"http://foo.bar/",	"a;15"},						// 23
			 {false,	"1", 	"1",	"false",	"http://foo.bar/",	"a;;15,b;;14"},					// 24
			 {false,	"1", 	"1",	"false",	"http://foo.bar/",	"a;15;b;14"},					// 25
			 {false,	"1", 	"1",	"false",	"http://foo.bar/",	"a;15,b:14"},					// 26
			 {false,	"1", 	"1",	"false",	"http://foo.bar/",	"a15,b;14"}, 					// 27
			 {false,	"1", 	"1",	"false",	"http://foo.bar/",	"a;-15,b;14"}, 					// 28
			 {false,	"1", 	"1",	"false",	"http://foo.bar/",	"aç%;15,b;14"}, 				// 29
			 
		});
	}
	
	public SettingValidatorTest(boolean expectedResponse, String pageThreads, String imageThreads, String writeBlocked, String baseUrl, String subpages) {
		this.expectedResponse = expectedResponse;
		this.pageThreads = pageThreads;
		this.imageThreads = imageThreads;
		this.writeBlocked = writeBlocked;
		BaseUrl = baseUrl;
		this.subpages = subpages;
	}



	@Before
	public void setup() throws Exception{
		appSettings = makeAppSettings(pageThreads, imageThreads, writeBlocked, BaseUrl, subpages);
	}
	
	@Test
	public void testValidateAppSettings() {
		assertThat(SettingValidator.validateAppSettings(appSettings), is(expectedResponse));
	}
	
	@Test
	public void testEmptyPropertyFile(){
		assertThat(SettingValidator.validateAppSettings(new Properties()), is(false));
	}
	
	/**
	 * Construct a new property Object from parameters
	 * @param pageThreads
	 * @param imageThreads
	 * @param writeBlocked
	 * @param BaseUrl
	 * @param subpages
	 * @return
	 */
	private Properties makeAppSettings(String pageThreads, String imageThreads, String writeBlocked, String BaseUrl, String subpages){
		Properties setting = new Properties();
		
		setting.setProperty("page_threads",pageThreads);
		setting.setProperty("image_threads",imageThreads);
		setting.setProperty("write_Blocked",writeBlocked);
		setting.setProperty("base_url",BaseUrl);
		setting.setProperty("sub_pages",subpages);
		
		return setting;
	}
}
