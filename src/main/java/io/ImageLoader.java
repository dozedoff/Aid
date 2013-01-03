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
package io;

import java.io.File;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activity.InvalidActivityException;

import com.github.dozedoff.commonj.net.DownloadItem;
import com.github.dozedoff.commonj.net.FileLoader;
import com.github.dozedoff.commonj.net.PageLoadException;

import filter.CacheCheck;
import filter.Filter;
import gui.Stats;

public class ImageLoader extends FileLoader {
private static final Logger logger = LoggerFactory.getLogger(ImageLoader.class);

private FileWriter fileWriter;
private CacheCheck cacheCheck;

private final int TIME_GRAPH_FACTOR = 1; // factor used for scaling DataGraph output

	public ImageLoader(FileWriter fileWriter, CacheCheck cacheCheck, File workingDir, int imageQueueWorkers) {
		super(workingDir, imageQueueWorkers);
		this.fileWriter = fileWriter;
		this.cacheCheck = cacheCheck;
		
		logger.info("ImageLoader started");
	}

	@Override
	protected boolean beforeFileAdd(URL url, String fileName) {
		if(cacheCheck.isDownloaded(url)){	// has the file been downloaded recently?
			return false;
		}
		return true;
	}
	
	@Override
	protected void afterFileAdd(URL url, String fileName) {
		updateFileQueueState();
	}
	
	private void updateFileQueueState(){
		Stats.setFileQueueState("FileQueue: "+downloadList.size()+" - "+"? / "+fileQueueWorkers);
		// queue size  - active workers / pool size
	}
	
	@Override
	protected void afterClearQueue() {
		updateFileQueueState();
	}
	
	@Override
	protected void afterProcessItem(DownloadItem ii) {
		updateFileQueueState();
	}
	
	@Override
	protected void afterFileDownload(byte[] data, File fullpath, URL url) {
		if(data != null){
			try {
				logger.debug("Adding file {} to FileWriter, caching URL {}", fullpath, url);
				fileWriter.add(fullpath, data.clone());
				cacheCheck.setDownloaded(url, true);
				Stats.addTimeGraphValue((int)((data.length/1024)*TIME_GRAPH_FACTOR)); // add data to the download graph
			} catch (InvalidActivityException e) {
				logger.warn("Failed adding file {} to FileWriter ({})", fullpath, url);
				logger.warn("Failed with {}", e);
			}
			
		}else{
			logger.warn("Downloaded data for {} ({}) was null", url, fullpath);
		}
	}
	
	@Override
	protected void onPageLoadException(PageLoadException ple) {
		int responseCode = Integer.parseInt(ple.getMessage());

		if(responseCode == 404 || responseCode == 500){
			logger.warn("Could not load file, invalid response ({}) for {}", responseCode, ple.getUrl());
		}

		if(responseCode == 503){
			logger.warn("Got a 503 response for {} This cloud indicate server porblems or a possible IP ban.", ple.getUrl());
		}else{
			logger.info("GetBinary(size) http code "+ple.getMessage());
		}
	}
}
