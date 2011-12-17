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
public abstract class ImageLoader {
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
	
	/**
	 * Run before a file is added to the list.
	 * @param url URL that was added
	 * @param fileName relative path to working directory
	 */
	protected void beforeImageAdd(URL url,String fileName){} // code to run before adding a file to the list
	
	/**
	 * Run after a file was added to the list.
	 * @param url URL that was added
	 * @param fileName relative path to working directory
	 */
	protected void afterImageAdd(URL url,String fileName){} // code to run after adding a file to the list

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
	
	/**
	 * Called after the queue has been cleared.
	 */
	protected void afterClearImageQueue(){}

	/**
	 * Download an image and pass it to the buffer for saving.
	 * 
	 * @param url URL to save
	 * @param savePath relative save path
	 */
	private void loadImage(URL url, File savePath){
		File fullPath = new File(workingDir, savePath.toString());

		try{Thread.sleep(SLEEP_VALUE_IMAGE);}catch(InterruptedException ie){}

		byte[] data = null;
		try{
			data = getBinary.getViaHttp(url);
			onFileDownload(data,fullPath,url);
		}catch(PageLoadException ple){
			onPageLoadException(ple);
		}catch(IOException ioe){
			onIOException(ioe);
		}
	}
	
	/**
	 * Called when a page could be contacted, but an error code was received from the server.
	 * @param ple the PageLoadException that was thrown
	 */
	protected void onPageLoadException(PageLoadException ple){
		logger.warning("Unable to load " + ple.getUrl() + " , response is " + ple.getResponseCode());
	}
	
	/**
	 * Called when a page could not be loaded due to an IO error.
	 * @param ioe the IOException that was thrown
	 */
	protected void onIOException(IOException ioe){
		logger.warning("Unable to load page " + ioe.getLocalizedMessage());
	}
	
	/**
	 * Called when the file was successfully downloaded.
	 * @param data the downloaded file
	 * @param fullpath the absolute filepath
	 * @param url the url of the file
	 */
	abstract protected void onFileDownload(byte[] data, File fullpath, URL url);
	
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
	
	/**
	 * Called after a worker has processed an item from the list.
	 * @param ii the imageitem that was processed
	 */
	protected void afterProcessItem(ImageItem ii){}

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

					loadImage(ii.getImageUrl(), new File(ii.getImageName()));
					afterProcessItem(ii);
					
				}catch(InterruptedException ie){interrupt();} //otherwise it will reset it's own interrupt flag
			}
		}
	}
}
