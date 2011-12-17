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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.activity.InvalidActivityException;

import net.GetBinary;
import net.PageLoadException;
import filter.Filter;
import gui.Stats;

/**
 * Class for downloading images from the Internet.
 */
public class ImageLoader {
	private static Logger logger = Logger.getLogger(ImageLoader.class.getName());

	private FileWriter fileWriter;

	protected LinkedBlockingQueue<ImageItem> imageUrlList = new LinkedBlockingQueue<ImageItem>();
	private LinkedList<Thread> workers = new LinkedList<>();

	private boolean skipLogEnabled = false;

	private final double TIME_GRAPH_FACTOR = 0.03255;
	private final int SLEEP_VALUE_IMAGE = 1000;

	private GetBinary getBinary = new GetBinary();

	private File workingDir;

	protected int imageQueueWorkers;
	private Filter filter;

	public ImageLoader(FileWriter fileWriter,Filter filter, File workingDir, int imageQueueWorkers) {
		this.fileWriter = fileWriter;
		this.workingDir = workingDir;
		this.imageQueueWorkers = imageQueueWorkers;
		this.filter = filter;

		setUp(imageQueueWorkers);
	}

	public boolean isSkipLogEnabled() {
		return skipLogEnabled;
	}

	public void setSkipLogEnabled(boolean skipLogEnabled) {
		this.skipLogEnabled = skipLogEnabled;
	}
	
	protected void beforeImageAdd(URL url,String fileName){} // code to run before adding a file to the list
	protected void afterImageAdd(URL url,String fileName){} // ocde to run after adding a file to the list

	public void addImage(URL url,String fileName){
		beforeImageAdd(url, fileName);
		
		if(imageUrlList.contains(url)) // is the file already queued? 
			return;
		
		imageUrlList.add(new ImageItem(url, fileName));
		
		afterImageAdd(url, fileName);
	}

	public void clearImageQueue(){
		imageUrlList.clear();
	}
	
	protected void afterClearImageQueue(){}

	/**
	 * Download an image and pass it to the buffer for saving.
	 * 
	 * @param url
	 * @param savePath
	 */
	private void loadImage(URL url, File savePath){
		File fullPath = new File(workingDir, savePath.toString());

		try{Thread.sleep(SLEEP_VALUE_IMAGE);}catch(InterruptedException ie){}

		byte[] file = null;
		try{
			file = getBinary.getViaHttp(url);

			if(file != null){
				fileWriter.add(fullPath, file.clone());
				filter.cache(url);;	//add URL to cache
				Stats.addTimeGraphValue((int)((file.length/1024)*TIME_GRAPH_FACTOR)); // add data to the download graph
			}
		}catch(InvalidActivityException iae){
			logger.warning(iae.getMessage());
		}catch(PageLoadException ple){
			int responseCode = Integer.parseInt(ple.getMessage());

			// the file was unavailable
			if(responseCode == 404 || responseCode == 500){
				logger.warning("got a 404 or 500 response for "+url.toString());
				filter.cache(url); // to prevent future attempts to load the file
				return;
			}

			if(responseCode == 503){
				logger.severe("IP was banned for too many connections"); // :_(  thats what you get if you use too short intervals
				System.exit(3);
			}else{
				logger.info("GetBinary(size) http code "+ple.getMessage());
			}
		}catch(IOException io){
			logger.warning("GetBinary Error: "+io.getLocalizedMessage());
		}
	}

	private void setUp(int image){
		for(int i=0; i <image; i++){
			workers.add(new ImageWorker());
		}

		for(Thread t : workers){
			t.start();
		}
	}

	public void shutdown(){
		logger.info("ImageLoader shutting down...");
		
		clearImageQueue();

		for(Thread t : workers){
			t.interrupt();
		}

		for(Thread t : workers){
			try {t.join();} catch (InterruptedException e) {}
		}
		
		logger.info("ImageLoader shutdown complete");
	}

	class ImageWorker extends Thread{
		public ImageWorker() {
			super("ImageWorker");

			Thread.currentThread().setPriority(2);
		}

		@Override
		public void run() {
			while(! isInterrupted()){
				try{
					ImageItem ii;
					ii = imageUrlList.take(); // grab some work
					if(ii == null) // check if the item is valid
						continue;

					updateFileQueueState();
					
					loadImage(ii.getImageUrl(), new File(ii.getImageName()));
				}catch(InterruptedException ie){interrupt();} //otherwise it will reset it's own interrupt flag
			}
		}
	}
}
