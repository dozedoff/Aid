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
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Class that represents a post in a thread.
 */
public class Post {
	protected String comment;
	protected String imageName;

	protected URL imageUrl;

	public void setComment(String comment) {
		this.comment = comment;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public void setImageUrl(URL imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getComment() {
		return comment;
	}
	public String getImageName() {
		return imageName;
	}
	public URL getImageUrl() {
		return imageUrl;
	}
	public boolean hasImage(){
		return imageName != null ? true : false;
	}
	public boolean hasComment(){
		return comment != null ? true : false;
	}
}