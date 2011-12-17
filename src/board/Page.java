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

import io.FileLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Logger;

import net.GetHtml;
import net.PageLoadException;
import filter.Filter;
import filter.FilterItem;
import filter.FilterState;

/**
 * This class represents a Page on the Board.
 */
public class Page implements Runnable{
	private LinkedList<PageThread> pageThreads = new LinkedList<PageThread>();
	private static Logger logger = Logger.getLogger(Page.class.getName());
	private URL pageUrl, boardUrl;
	private Filter filter;
	private FileLoader imageLoader;
	private int pageNumber;

	private boolean stop = false;

	public Page(URL boardUrl,int pageNumber, Filter filter,FileLoader imageLoader){
		this.boardUrl = boardUrl;
		this.filter = filter;
		this.imageLoader = imageLoader;
		this.pageNumber = pageNumber;
		makePageUrl();
	}

	private void makePageUrl(){
		try {
			String boardurl;
			if(boardUrl.toString().endsWith("/"))
				boardurl = this.boardUrl.toString();
			else
				boardurl = this.boardUrl.toString()+"/";
			
			
			if(pageNumber == 0)
				this.pageUrl = new URL(boardurl);
			else{
				this.pageUrl = new URL(boardurl+pageNumber);
			}
		} catch (MalformedURLException e) {
			logger.severe("Could not generate page URL for\n"+boardUrl+pageNumber);
		}
	}

	public void setStop(boolean stop){
		this.stop = stop;
	}

	public URL getPageUrl() {
		return pageUrl;
	}

	public ArrayList<URL> processPage(String html){
		int pageStart,pageEnd;
		ArrayList<URL> threadUrls = new ArrayList<>();

		// only the text between the first 2 <center> tags is relevant
		try{
			pageStart = html.indexOf("</noscript>", 0)+11;
			pageEnd = html.indexOf("</noscript>", pageStart);
			html = html.substring(pageStart, pageEnd);
		}catch(StringIndexOutOfBoundsException e){
			logger.severe(
					e.getMessage()+" for\n"+pageUrl+"\n"+
							"could probably not find the end of the Page"
					);
			return threadUrls;
		}
		//cut up tokens to get thread url's
		Scanner pageScanner = new Scanner(html).useDelimiter("<a href=\"");
		String temp;

		while(pageScanner.hasNext()){
			if(stop){break;}

			temp = pageScanner.next();
			if(!temp.startsWith("res"))	// not a thread
				continue;

			int endMark = temp.indexOf("\""); // end of the address
			temp = temp.substring(0, endMark);
			if(temp.contains("#"))	// in thread post reference
				continue;

			try {
				threadUrls.add(new URL(boardUrl.toString()+"/"+temp));
			} catch (MalformedURLException e) {
				logger.warning("unable to process thread URL.\n "+temp+"\n"+e.getMessage());
			}
		}
		return threadUrls;
	}

	@Override
	public void run(){
		if(stop){return;}
			
		pageThreads.clear();

		ArrayList<URL> threadUrls;

		threadUrls = processPage(loadUrl(pageUrl));

		boolean blocked = false;

		for(URL url : threadUrls){
			if(stop){break;}
			blocked = false;
			FilterState threadFilterState = filter.getFilterState(url);

			if(threadFilterState == FilterState.DENY | threadFilterState == FilterState.PENDING){
				continue;
			}

			PageThread pt = new PageThread(url);
			pt.processThread(loadUrl(pt.getThreadUrl()));
			String response;

			for(Post p : pt.getPosts()){
				response = filter.checkPost(p);
				
				if(response != null && threadFilterState != FilterState.ALLOW){
					filter.reviewThread(new FilterItem(pt.getBoardDesignation(), response, pt.getThreadUrl(), FilterState.PENDING));
					filter.downloadThumbs(pt.getThreadUrl().toString(), pt.getPosts());
					blocked = true;
					break;
				}
			}

			if(! blocked)
				pageThreads.add(pt);
		}

		for(PageThread pt : pageThreads){
			if(stop){break;}

			for(Post p : pt.getPosts()){
				if(stop){break;}

				if(p.hasImage()){
						imageLoader.add(p.getImageUrl(), pt.getBoardDesignation()+"\\"+pt.getThreadNumber()+"\\"+p.getImageName());
				}
			}
		}
		
		if(! stop)
			try{Thread.sleep(10*1000);}catch(InterruptedException ie){}
	}

	private String loadUrl(URL url){
		String html ="";

		try {
			html = new GetHtml().get(url);
		} catch (IOException io) {
			logger.warning("unable to load "+pageUrl.toString()+" , ResponseCode: "+io.getMessage());

		} catch (PageLoadException ple) {
			if(ple.getResponseCode() == 404 || ple.getResponseCode() == 500)
				logger.warning("unable to load "+pageUrl.toString()+" , ResponseCode: "+ple.getResponseCode());//TODO do something else?
			else
				logger.warning("unable to load "+pageUrl.toString()+" , ResponseCode: "+ple.getResponseCode());
		}
		return html;
	}
}