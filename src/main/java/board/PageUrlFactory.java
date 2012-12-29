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
package board;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for generating the pages for a board.
 */
public class PageUrlFactory {
	private static Logger logger = LoggerFactory.getLogger(PageUrlFactory.class.getName());
	private PageUrlFactory(){}

	public static ArrayList<URL> makePages(URL boardUrl, int numOfPages){
		ArrayList<URL>pages = new ArrayList<>(numOfPages);

		try {
			// the first page does not have a number, all  that are n > 0 have "/n/" added
			pages.add(makePageUrl(boardUrl, 0));

			for(int i=2; i<numOfPages+1; i++){
				pages.add(makePageUrl(boardUrl, i));
			}
		} catch (MalformedURLException e) {
			logger.warn("invalid URL, page creation aborted.\n"+e.getMessage());
		}
		return pages;
	}
	
	private static URL makePageUrl(URL boardUrl, int pageNumber)	throws MalformedURLException {
		URL pageUrl;
		String boardurl;
		
		if (boardUrl.toString().endsWith("/"))
			boardurl = boardUrl.toString();
		else
			boardurl = boardUrl.toString() + "/";

		if (pageNumber == 0)
			pageUrl = new URL(boardurl);
		else {
			pageUrl = new URL(boardurl + pageNumber);
		}

		return pageUrl;
	}
}
