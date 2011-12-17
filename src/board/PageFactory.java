/*  Copyright (C) 2011  Nicholas Wright
	
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

import java.util.logging.*;

import net.FileLoader;

import filter.Filter;

/**
 * Class for generating the pages for a board.
 */
public class PageFactory {
	private static Logger logger = Logger.getLogger(PageFactory.class.getName());
	private PageFactory(){}

	public static ArrayList<Page> makePages(String boardUrl, int numOfPages, Filter filter, FileLoader imageLoader){
		ArrayList<Page>pages = new ArrayList<>(numOfPages);

		try {
			// the first page does not have a number, all  that are n > 0 have "/n/" added
			pages.add(new Page(new URL(boardUrl),0, filter, imageLoader));

			for(int i=2; i<numOfPages+1; i++)
				pages.add(new Page(new URL(boardUrl),i, filter, imageLoader));
		} catch (MalformedURLException e) {
			logger.warning("invalid URL, page creation aborted.\n"+e.getMessage());
		}
		return pages;
	}
}
