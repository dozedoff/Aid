/*  Copyright (C) 2012  Nicholas Wright

	part of 'Aid', an imageboard downloader.

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

import java.util.Properties;
import java.util.logging.Logger;

import sun.misc.Regexp;

public class SettingValidator {
	private static final Logger logger = Logger.getLogger(SettingValidator.class.getName());
	private static final String SUBPAGE_REGEX = "([a-zA-Z]+;[0-9]+,)*+[a-zA-Z]+;[0-9]+$";
	private static final String BASEURL_REGEX = "(?m)http://([0-9a-zA-Z.-]+\\.)+([a-zA-Z])*/$";

	public static boolean validateAppSettings(Properties appSettings){
		// validate loaded parameters
		String errorMsg;
		boolean valid = true;

		// validate number of page threads
		String page = appSettings.getProperty("page_threads");
		errorMsg = invalidPropertyMessage("page_threads");
		valid = valid && testLessThan(page, errorMsg, 1);

		// validate number of image threads
		String image = appSettings.getProperty("image_threads");
		errorMsg = invalidPropertyMessage("image_threads");
		valid = valid && testLessThan(image, errorMsg, 1);

		// validate "write blocked" flag
		String writeBlocked = appSettings.getProperty("write_Blocked");
		if(writeBlocked == null || !(writeBlocked.equals("false") || writeBlocked.equals("true"))){
			errorMsg = invalidPropertyMessage("write_Blocked");
			logger.warning(errorMsg);
			valid = false;
		}

		// validate base URL
		String baseUrl = appSettings.getProperty("base_url");
		errorMsg = invalidPropertyMessage("base_url");
		valid = valid && testRegexMatch(baseUrl, errorMsg, BASEURL_REGEX);

		// validate sub-pages
		String subPages = appSettings.getProperty("sub_pages");
		errorMsg = invalidPropertyMessage("sub_pages");
		valid = valid && testRegexMatch(subPages, errorMsg, SUBPAGE_REGEX);

		// validate window position (x,y)
		String xpos = appSettings.getProperty("xpos");
		errorMsg = invalidPropertyMessage("xpos");
		valid = valid && testLessThan(xpos, errorMsg, 0);

		String ypos = appSettings.getProperty("ypos");
		errorMsg = invalidPropertyMessage("ypos");
		valid = valid && testLessThan(ypos, errorMsg, 0);

		return valid;
	}

	/**
	 * Tests if the supplied regex matches the string. Outputs a error message if not.
	 * @param toTest string to test
	 * @param errorMsg message to output if string does not match
	 * @param regex regex to use for test
	 * @return
	 */
	private static boolean testRegexMatch(String toTest, String errorMsg, String regex) {
		if(toTest == null || (! toTest.matches(regex))){
			logger.warning(errorMsg);
			return false;
		}
		return true;
	}

	/**
	 * Test if the string's int value is less than LessThan.
	 * @param toTest string to test
	 * @param errorMsg error Message to output in case toTest is equal or grater
	 * @param lessThan value to test against
	 * @return true if less, else false
	 */
	private static boolean testLessThan(String toTest, String errorMsg, int lessThan) {
		try{
			if(toTest == null || Integer.parseInt(toTest) < lessThan){
				logger.warning(errorMsg);
				return false;
			}
		}catch(NumberFormatException nfe){
			logger.warning(errorMsg);
			return false;
		}
		return true;
	}

	/**
	 * Generates a error message for properties that are invalid.
	 * @param property property name in the file
	 * @param filename the config file where the property is located
	 * @param defaultValue the default value used for the property
	 * @return
	 */
	private static String invalidPropertyMessage(String property){
		return "'"+property+"' property is invalid.";
	}
}