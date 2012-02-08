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
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class that represents a thread on the page.
 */
public class PageThread {
	private ArrayList<Post> postList = new ArrayList<Post>();
	private URL threadUrl;
	
	public PageThread(URL threadUrl){
		this.threadUrl = threadUrl;
	}
	
	public URL getThreadUrl(){
		return threadUrl;
	}

	public void processThread(String html){
		//split up the page into segments, so the whole thing does not get processed every time
		Scanner postScanner = new Scanner(html).useDelimiter("<span class=\"commentpostername\">");
		ArrayList<String> tokenList = new ArrayList<String>();

		//postScanner.next("<form name=\"delform\"");
		postScanner.findInLine("<form name=\"delform\"");
		
		while(postScanner.hasNext()){
			tokenList.add(postScanner.next());
		}

		if(tokenList.isEmpty())
			return;

		int cutHere=0;

		cutHere = tokenList.get(0).indexOf("<center>", 0); // start of the first post in thread
		if(cutHere != -1){
			String temp = tokenList.get(0);
			tokenList.remove(0);	// remove the old entry
			tokenList.add(0,temp.substring(cutHere, temp.length()));	// cut and re-insert
		}

		cutHere = 0;
		cutHere = tokenList.get(tokenList.size()-1).indexOf("<center>", 0); // end of the last post in thread

		if(cutHere != -1){
			String temp = tokenList.get(tokenList.size()-1);
			tokenList.remove(tokenList.size()-1);	// remove the old entry
			tokenList.add(temp.substring(0, cutHere));	// cut and re-insert
		}

		postList.clear();
		// create Post objects
		for(String htmlSnippet : tokenList){
			Post p = new Post();
			p.processHtml(htmlSnippet);
			postList.add(p);
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