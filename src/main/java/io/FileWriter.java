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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.activity.InvalidActivityException;

import com.github.dozedoff.commonj.file.BinaryFileReader;
import com.github.dozedoff.commonj.gui.Log;
import com.github.dozedoff.commonj.hash.HashMaker;

import filter.Filter;
import gui.Stats;

/**
 * Class for checking and buffering files before they are written to disk.<br/><br/>
 * Before a file is written to disk it will be hashed and compared to the database.
 * New files are saved, ones already present will be discarded.<br/>
 * The reason for buffering the whole files is, at least under windows, writing lots of
 * small fragments causes massive fragmentation.
 * The buffer is currently flushes at a set interval.
 */
public class FileWriter extends Thread{
	final LinkedBlockingQueue<FileItem> fileBuffer = new LinkedBlockingQueue<>();

	boolean writeBlocked = false;
	HashMaker hashMaker = new HashMaker(); // used to generate SHA-2 Hash
	//TODO use this to output current buffer size to GUI
	AtomicLong bufferSize = new AtomicLong(0); // amount of data in the buffer 
	volatile boolean stop = false; // stop the FileWrite and do a clean Shutdown
	//TODO will FileWriter shut down correctly without volatile?
	private Filter filter;
	private static Logger logger = Logger.getLogger(FileWriter.class.getName());

	long bytesSaved = 0;		// bytes written to disk
	long bytesDiscarded = 0;  // bytes discarded (Hash found in mySQL Database)
	
	private final int FLUSH_INTERVAL = 1000; // time between buffer flushes, in ms.
	private final String[] ILLEGAL_FILENAME_CHARS = {"/", "\\", ":", "?", "\"", "<", ">", "|"};	
	
	public FileWriter(Filter filter){
		super("FileWriter");
		this.filter = filter;
		this.start();
	}

	public boolean isWriteBlocked() {
		return writeBlocked;
	}

	public void setWriteBlocked(boolean writeBlocked) {
		this.writeBlocked = writeBlocked;
	}

	/**
	 * Returns the number of files in the file buffer.
	 * 
	 *  @return Number of files in the buffer.
	 */
	public int getPendingWrites(){
		return fileBuffer.size();
	}

	/**
	 * Returns the number of bytes saved to the Disk.
	 * 
	 * @return Number of bytes saved.
	 */
	public long getBytesSaved() {
		return bytesSaved;
	}
	
	/**
	 * Resets BytesSaved and BytesDiscarded to 0.
	 */
	public void clearStats(){
		bytesSaved = 0;
		bytesDiscarded = 0;
		Stats.resetStats();
	}
	
	/**
	 * Returns the number of bytes that were discarded because the Hash was found
	 * in the Database.
	 * 
	 * @return Number of bytes discarded.
	 */
	public long getBytesDiscarded() {
		return bytesDiscarded;
	}
	
	/**
	 * Adds a new File to the buffer.
	 * Note that the File will not be written to disk instantly. The file buffer will be written
	 * to disk at a set interval.
	 * 
	 * @param path Filesystem path to save the data to.
	 * @param data Binary data of the file.
	 * @throws InvalidActivityException Thrown if files are added during shutdown.
	 */
	public void add(File path, byte[] data) throws InvalidActivityException{
		if(stop)
			throw new InvalidActivityException("FileWriter is shutting down");
		fileBuffer.add(new FileItem(path, data));
		bufferSize.addAndGet(data.length);
	}
	
	/**
	 * Writes all files in the file buffer to disk.
	 * In case a file already exist, _{time in long format} will be apended to
	 * the filename.
	 * 
	 * @param data Data of the file
	 * @param path Filepath of the file.
	 * @param hash Hash value of the file data.
	 */
	private void writeToDisk(byte[] data, String path, String hash){
		File directory = new File(path).getParentFile();
		File fullPath = new File(path);

		directory.mkdirs();

		try{
			if(!fullPath.createNewFile()){
				if(fullPath.exists()){
					//file exits, compare hash values
					String newFileHash =  hashMaker.hash(data);
					String existingFileHash = hashMaker.hash(new BinaryFileReader().get(fullPath));
					
					if(newFileHash.equals(existingFileHash)){
						//files are identical, normally this should not happen
						bytesDiscarded += data.length;
						try {
							filter.addIndex(existingFileHash, fullPath.toString(), data.length);
						} catch (SQLException e) {
							logger.warning("Could not add Hash to database: "+e.getMessage());
						}
						return;
					}else{
						// same name, different data
						fullPath = newFileName(fullPath, true); // change name and re-add to queue
						add(fullPath, data);
						return;
					}
				}
			}

		}catch(IOException e){
			logger.warning("Unable to create File: "+e.getMessage());
		}

		try{
			if(!hasValidFilename(fullPath)){
				fullPath = newFileName(fullPath, false);
			}
			
			BufferedOutputStream buffOut = new BufferedOutputStream(new FileOutputStream(fullPath),1024);

			buffOut.write(data);
			buffOut.close();
			
			filter.addIndex(hash, path, data.length);
			bytesSaved += data.length; // in bytes
			Stats.saveBytes(data.length);
		}catch(SQLException se){
			if(se.getLocalizedMessage().contains("Incorrect string value")){ //TODO instead of writing the file here, add the data back to the buffer
				logger.warning("Unable to add hash to database due to :"+se.getMessage());
				fullPath.delete();
				fullPath = newFileName(fullPath, false);
				try {
					add(fullPath,data);
				} catch (InvalidActivityException e) {
					logger.warning("failed to write file: "+e.getMessage());
				}
			}else{
				logger.severe("add hash failed: "+se.getMessage());
			}
		}catch(Exception e){
			logger.severe("File Buffer write failed: "+e.getLocalizedMessage());
			if(e != null && e.getLocalizedMessage().contains("space")){
				logger.severe("Not enough free disk space, will now exit...");
				System.exit(1);
			}
		}
	}
	
	private boolean hasValidFilename(File fullpath) {
		String filename = fullpath.getName();

		if (filename.isEmpty()) {
			return false;
		}

		for (String illegalChar : ILLEGAL_FILENAME_CHARS) {
			if (filename.contains(illegalChar)) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Write the contents of the file buffer to disk.<br/>
	 * Before writing to disk, the file is hashed (SHA-2) and checked against the Database.<br/>
	 * If the file is in the blocklist, it is tagged.<br/>
	 * If it's already present the file will be discarded.<br/>
	 * If the file does not exist, it is saved and added to the database.
	 */
	private void flushBuffer(){
		byte[] data;
		String path, hash;
		Path dir;
		LinkedList<FileItem> flushBuffer = new LinkedList<>();
		fileBuffer.drainTo(flushBuffer);

		while(! flushBuffer.isEmpty()){
			FileItem fi = flushBuffer.poll();

			path = fi.getPath().toString();
			data = fi.getData();
			
			if(data.length == 0){
				Log.add("Zero size file ignored: "+path);
				continue;
			}

			hash = hashMaker.hash(data);
			if (filter.isBlacklisted(hash)){ // files will be renamed to WARNING-{hash value}-{filename}{file extension}
				Path realPath = Paths.get(path);
				dir = realPath.getParent();
				String name = realPath.getFileName().toString();
				
				// should blocked files be written to disk, or only create a placeholder?
				if(writeBlocked){ 
					path = dir.resolve("WARNING-"+hash+"-"+name).toString(); //add tag to unwanted file

					writeToDisk(data, path, hash);
				}else{
					path=dir.resolve("WARNING-"+hash+"-"+name+".txt").toString(); 
					dir.toFile().mkdirs();
					File file = new File(path);

					try {
						file.createNewFile();
					} catch (IOException e) {
						logger.warning("failed to create warning Tag for "+dir);
					}
				}

				logger.warning("WARNING! "+ path + " is blacklisted");
				Log.add("WARNING! "+ path + " is blacklisted");
				continue;
			}

			if (filter.exists(hash)){
				bytesDiscarded += data.length; // in bytes
				Stats.discardBytes(data.length);
				continue;
			}
			writeToDisk(data, path, hash);
		}
		bufferSize.set(0);	// reset counter
	}
	
	/**
	 * Renames the provided file. If the name should be preserved, output will be {name}_{timestamp}.{extension}
	 * else renamed_{timestamp}.{extension}
	 * @param filepath the original filename
	 * @param keepOriginalName keeps name if true, otherwise replaces it with "renamed_"
	 * @return the new filepath
	 */
	private File newFileName(File filepath, boolean keepOriginalName){
		File newPath;
		
		String fullname = filepath.getName();
		String extension = fullname.substring(fullname.lastIndexOf("."));
		String name = fullname.substring(0,fullname.lastIndexOf("."));
		
		File directory = filepath.getParentFile();
		
		if(keepOriginalName){
			newPath = new File(directory,name + "_" + Calendar.getInstance().getTimeInMillis() + extension);
		}else{
			newPath = new File(directory,"renamed_" + Calendar.getInstance().getTimeInMillis() + extension);
		}
		logger.info("Renamed "+filepath.toString()+" to "+newPath.toString());
		return newPath;
	}

	/**
	 * Filewriter will no longer accept new files, and will
	 * begin to flush the buffer to disk.
	 */
	public void shutdown(){
		logger.info("Shutting down FileWriter...");
		
		this.stop = true;
		//	interrupt();
		try {
			this.interrupt();
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("FileWriter shutdown complete");
	}

	// run until stopped
	@Override
	public void run(){
		setPriority(7);

		while(!stop){
			try{Thread.sleep(FLUSH_INTERVAL);}catch(InterruptedException ignore){interrupt();}
			flushBuffer();
		}
		flushBuffer(); // write buffer to Disk when the Thread is stopped
	}
}