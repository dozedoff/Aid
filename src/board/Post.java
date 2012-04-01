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
	protected String comment = "";
	protected String imageName;

	protected URL imageUrl;

	private static final Logger LOGGER = Logger.getLogger(Post.class.getName());

	public void processHtml(String html){
		//TODO make tag finding scalable
		Scanner scanner;

		if(html.equals("")){	// empty post
			comment = null;
			imageName = null;
			imageUrl = null;
			return;
		}

		// find the image URL
		scanner = new Scanner(html);
		scanner.useDelimiter("</span><br><a href=\"|\" target=_blank><img");

		if(scanner.hasNext()) // skip the first block
			scanner.next();

		if(scanner.hasNext()){
			StringBuilder sb = new StringBuilder();
			try {
				sb.append(scanner.next());
				// this fixes "http:" going missing.
				// Cloudflare related?
				if(sb.toString().startsWith("//")){
					sb.insert(0, "http:");
				}
				imageUrl = new URL(sb.toString());
			} catch (MalformedURLException e) {
				LOGGER.warning("Invalid url: "+e.getMessage());
				imageUrl = null;
			}
		}else{
			imageUrl = null;
		}

		// no point in looking for a name if there is no URL
		if(imageUrl != null){
			// find the image name
			scanner = new Scanner(html);
			scanner.useDelimiter(", <span title=\"|</span>\\)</span>");

			if(scanner.hasNext()) // skip the first block
				scanner.next();

			if(scanner.hasNext()){
				imageName = scanner.next().split("\">")[0];
			}else{
				imageName = null;
			}
		}

		// find the comment
		scanner = new Scanner(html);
		scanner.useDelimiter("<blockquote>|</blockquote>");

		if(scanner.hasNext()) // skip the first block
			scanner.next();

		if(scanner.hasNext()){
			comment = scanner.next();
			
			if(comment.equals("")){
				comment = null;
			}
		}else{
			comment = null;
		}
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