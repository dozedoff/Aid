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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Class that represents a thread on the page.
 */
public class PageThread {
	private ArrayList<Post> postList = new ArrayList<Post>();
	private URL threadUrl;
	private static final Logger logger = LoggerFactory.getLogger(PageThread.class);
	
	public PageThread(URL threadUrl){
		this.threadUrl = threadUrl;
	}
	
	public URL getThreadUrl(){
		return threadUrl;
	}

	public void processThread(String html){
		Document pageDocument = Jsoup.parse(html);
		
		Element thread = pageDocument.select("#delform > div.board > div.thread").first();
		
		Elements posts = thread.getElementsByClass("post");
		
		postList.clear();
		
		for(Element post : posts){
			Post postObject = new Post();
			
			Elements fileElements = post.getElementsByClass("file");
			
			for(Element file : fileElements){
				String imageUrl = "?";
				try{
					Element imageInfo = file.select("div.fileInfo > span.fileText").first();
					if(imageInfo == null){
						// the image was deleted
						continue;
					}
					
					postObject.setImageName(imageInfo.select("span").attr("title"));
					imageUrl = imageInfo.select("a").attr("href");
					
					postObject.setImageUrl(new URL("https:" + imageUrl));
				}catch(MalformedURLException mue){
					logger.warn("Invalid image URL (" + imageUrl+ ") in thread " + threadUrl);
					postObject.setImageName(null);
					postObject.setImageUrl(null);
				}
			}
			
			postObject.setComment(post.getElementsByClass("postMessage").first().ownText());
			
			postList.add(postObject);
		}
	}

	public ArrayList<Post> getPosts(){
		return postList;
	}
	// example: /a/res/43587987
	public int getThreadNumber(){
			String urlFragments[] = threadUrl.toString().split("/");
			return Integer.parseInt(urlFragments[urlFragments.length-1]);
	}
	
	public String getBoardDesignation(){
		String urlFragments[] = threadUrl.toString().split("/");
		return urlFragments[urlFragments.length-3];
	}
}
