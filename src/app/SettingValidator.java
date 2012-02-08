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

public class SettingValidator {
	private static final Logger logger = Logger.getLogger(SettingValidator.class.getName());

	public static boolean validateAppSettings(Properties appSettings){
		// validate loaded parameters
		String errorMsg;
		boolean valid = true;

		String page = appSettings.getProperty("page_threads");
		errorMsg = invalidPropertyMessage("page_threads");

		try{
			if(page == null || Integer.parseInt(page) < 1){
				logger.warning(errorMsg);
				valid = false;
			}
		}catch(NumberFormatException nfe){
			logger.warning(errorMsg);
			valid = false;
		}

		String image = appSettings.getProperty("image_threads");
		errorMsg = invalidPropertyMessage("image_threads");
		try{
			if(image == null || Integer.parseInt(image) < 1){
				logger.warning(errorMsg);
				valid = false;
			}
		}catch(NumberFormatException nfe){
			logger.warning(errorMsg);
			valid = false;
		}

		String writeBlocked = appSettings.getProperty("write_Blocked");
		if(writeBlocked == null || !(writeBlocked.equals("false") || writeBlocked.equals("true"))){
			errorMsg = invalidPropertyMessage("write_Blocked");
			logger.warning(errorMsg);
			valid = false;
		}

		String baseUrl = appSettings.getProperty("base_url");
		errorMsg = invalidPropertyMessage("base_url");
		if(baseUrl == null || (! baseUrl.matches("(?m)http://([0-9a-zA-Z.-]+\\.)+([a-zA-Z])*/$"))){
			logger.warning(errorMsg);
			valid = false;
		}

		String subPages = appSettings.getProperty("sub_pages");
		if(subPages == null || (! subPages.matches("([a-zA-Z]+;[0-9]+,)*+[a-zA-Z]+;[0-9]+$"))){
			errorMsg = invalidPropertyMessage("sub_pages");
			logger.warning(errorMsg);
			valid = false;
		}
		return valid;
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
