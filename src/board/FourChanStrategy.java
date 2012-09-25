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
import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;

import net.GetHtml;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sun.istack.internal.logging.Logger;

public class FourChanStrategy implements SiteStrategy {
	GetHtml getHtml = new GetHtml();
	static final Logger logger = Logger.getLogger(FourChanStrategy.class);
	
	@Override
	public boolean validSiteStrategy(URL siteUrl) {
		if(siteUrl.getHost().equals("http://4chan.org")){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public AbstractList<URL> findBoards() {
		// TODO look at Html source
		return null;
	}

	@Override
	public int getBoardPageCount(URL boardUrl) {
		// TODO look at Html source
		return 0;
	}

	@Override
	public List<PageThread> parsePage(URL boardUrl, Page page) {
		LinkedList<PageThread> pageThreads = new LinkedList<>();
		
		URL pageUrl = page.getPageUrl();
		String html = "";
		
		try {
			html = getHtml.get(pageUrl);
		} catch (Exception e){
			logger.warning("Failed to process page " + page.getPageUrl().toString() + " Cause: " + e.getMessage());
			return pageThreads;
		}
		
		Document pageDocument = Jsoup.parse(html);
		
		Elements board = pageDocument.select("#delform > div.board");
		Elements threads = board.first().getElementsByClass("thread");
		
		for(Element thread : threads){
			String relativeThreadUrl = thread.getElementsByClass("replylink").first().attr("href");
			
			try {
				URL threadUrl = new URL(boardUrl.toString() + relativeThreadUrl);
				PageThread pageThread = new PageThread(threadUrl);
				pageThreads.add(pageThread);
			} catch (MalformedURLException e) {
				logger.warning("unable to process thread URL.\n " + relativeThreadUrl + "\n" + e.getMessage());
			}
		}
		
		return pageThreads;
	}

	@Override
	public AbstractList<Post> parseThread(PageThread pageThread) {
		// TODO Auto-generated method stub
		return null;
	}

}
