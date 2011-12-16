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
package io;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import board.Post;

/**
 * Class for downloading and storing thumbnails.
 */
public class ThumbnailLoader {
	private static Logger logger = Logger.getLogger(ThumbnailLoader.class.getName());
	private final int NUM_OF_THUMBS = 17;
	private final int SQL_MAX_WAITTIME = 5000;
	private ConnectionPoolaid connPool;
	public ThumbnailLoader(ConnectionPoolaid connPool){
		this.connPool = connPool;
	}
	/**
	 * Download thumbnails and store them in the database.
	 * @param url URL of the thread from which the thumbnails are loaded
	 * @param postList Posts from which thumbnails should be loaded.
	 */
	public void downloadThumbs(String url,ArrayList<Post> postList){
		//TODO add code to re-fetch thumbs?
		GetBinary gb = new GetBinary(2097152);  // 2 mb
		int counter = 0;
		logger.info("fetching thumbs for " + url);
		for(Post p : postList){

			if (! p.hasImage())
				continue;

			// Image / thumbnail addresses follow a distinct pattern
			String thumbUrl = p.getImageUrl().toString();
			thumbUrl = thumbUrl.replace("images", "thumbs");
			thumbUrl = thumbUrl.replace("src", "thumb");
			thumbUrl = thumbUrl.replace(".jpg", "s.jpg");

			MySQLaid sql = null;
			try {
				byte data[] = gb.get(thumbUrl); // get thumbnail

				int split = thumbUrl.lastIndexOf("/")+1;
				String filename = thumbUrl.substring(split); // get the filename (used for sorting)

				logger.info("adding thumbnail  " + thumbUrl +", "+filename+ "  datasize: "+data.length);
				sql = aquireSql();
				sql.addThumb(url,filename, data); // add data to DB
			} catch (IOException e) {
				logger.info("could not load thumbnail: "+e.getMessage());		
			}finally{
				counter++;
				releaseSql(sql);
			}
			// only the first few thumbs are needed for a preview
			if (counter > (NUM_OF_THUMBS-1))
				break;
		}
	}

	/**
	 * Fetch thumbnail data from database.
	 * @param id URL of the page thumbs to load
	 * @return Array of Binary data
	 */
	public ArrayList<Image> getThumbs(String id){
		MySQLaid mySql = aquireSql();
		
		ArrayList<Image> images = new ArrayList<>(mySql.getThumb(id));
		releaseSql(mySql);
		
		return images;
	}

	private MySQLaid aquireSql(){
		try {
			return connPool.getResource(SQL_MAX_WAITTIME);
		} catch (InterruptedException e) {
		} catch (ResourceCreationException e) {
			logger.severe(e.getMessage());}
		return null;
	}
	
	private void releaseSql(MySQLaid mySql){
		connPool.returnResource(mySql);
	}
}
