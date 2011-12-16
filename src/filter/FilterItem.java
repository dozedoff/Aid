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
package filter;

import java.io.Serializable;
import java.net.URL;

/**
 * This class stores the information about a item in the filter.
 * Information include: location, state and reason.
 */
public class FilterItem implements Serializable{
	
	private static final long serialVersionUID = -9148296011482486975L;

	private String board;
	private String reason;
	private URL url;
	private FilterState state;
	private String displayString;
	
	private void updateDiplayString(){
		String urlStr = url.toString();
		String format = "%1$-5s %2$-10s %3$-30s";
		String pageNr = urlStr.substring(urlStr.lastIndexOf("/")+1,urlStr.length());
		displayString = String.format(format, board,pageNr,reason);
	}
	
	public FilterItem(String board, String reason, URL url, FilterState state){
		this.board = board;
		this.reason = reason;
		this.url = url;
		this.state = state;
		updateDiplayString();
	}
	
	public URL getUrl(){
		return url;
	}
	
	public void setState(FilterState state){
		this.state = state;
	}
	
	public FilterState getState(){
		return this.state;
	}
	
	public String getReason(){
		return reason;
	}
	
	public String getBoard(){
		return board;
	}
	
	@Override
	public String toString(){
		return displayString;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof FilterItem) )
			return false;

		FilterItem fi = (FilterItem)obj;
		
		if(! fi.getUrl().equals(this.url))
			return false;
		
		if(fi.getState() != this.state)
			return false;
		
		if(! fi.getReason().equals(this.reason))
			return false;
		
		if(! fi.getBoard().equals(this.board))
			return false;
		
		return true;
	}
}
