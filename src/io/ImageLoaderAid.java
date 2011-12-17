package io;

import java.io.File;
import java.net.URL;

import filter.Filter;
import gui.Stats;

public class ImageLoaderAid extends ImageLoader {

	public ImageLoaderAid(FileWriter fileWriter, Filter filter,
			File workingDir, int imageQueueWorkers) {
		super(fileWriter, filter, workingDir, imageQueueWorkers);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void beforeImageAdd(URL url, String fileName) {
		if(filter.isCached(url)){	// has the file been downloaded recently?
			filter.cache(url);		// if it has, update cache timestamp
			return;
		}
	}
	
	@Override
	protected void afterImageAdd(URL url, String fileName) {
		updateFileQueueState();
	}
	
	private void updateFileQueueState(){
		Stats.setFileQueueState("FileQueue: "+imageUrlList.size()+" - "+"? / "+imageQueueWorkers);
		// queue size  - active workers / pool size
	}
}
