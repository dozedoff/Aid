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

import java.net.URL;
import java.util.List;
import java.util.Map;

public interface SiteStrategy {
	public boolean validSiteStrategy(URL siteUrl);
	
	public Map<String, URL> findBoards(URL siteUrl);
	public int getBoardPageCount(URL boardUrl);
	
	public List<PageThread> parsePage(URL pageUrl);
	public List<Post> parseThread(PageThread pageThread);
}
