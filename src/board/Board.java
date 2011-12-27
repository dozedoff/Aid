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
package board;

import java.text.DateFormat;
import java.util.AbstractList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import java.util.logging.*;

import thread.WorkQueue;

/**
 * Represents a whole board.
 */
public class Board {
	private AbstractList<Page> pages;
	private WorkQueue pageQueue;
	private Timer pageAdder;
	private String boardId;
	private String lastRun ="";
	private boolean stoppped = true;
	private final int WAIT_TIME = 60 * 1000 * 60; // 1 hour
	
	private static final Logger LOGGER = Logger.getLogger(Board.class.getName()); // NOPMD by Nicholas Wright on 12/27/11 10:08 AM
	
	public Board(AbstractList<Page> pages, WorkQueue pageQueue, String boardId){
		this.pages = pages;
		this.pageQueue = pageQueue;
		this.boardId = boardId;
	}

	public void stop(){
		for(Page p : pages){
			p.setStop(true);
		}

		this.stoppped = true;
		if(pageAdder != null)
			pageAdder.cancel();
		
		lastRun = ""; // looks a bit odd otherwise
		
		LOGGER.info("Board "+boardId+" is stopping...");
	}

	@Override
	public String toString() {
		return "/"+boardId+"/";
	}

	public String getStatus(){
		String status = "/"+boardId+"/";
		status += " "+lastRun+" ";

		if(stoppped){
			status += "idle";
		}else{
			status += "running";
		}
		return status;
	}

	public void start(){
		start(0);
	}

	public void start(int delay){
		pageAdder = new Timer("Board "+boardId+" job adder", true);

		for(Page p : pages){
			p.setStop(false);
		}

		this.stoppped = false;
		pageAdder.schedule(new PageAdder(delay), delay*60*1000, WAIT_TIME);
	}

	class PageAdder extends TimerTask{
		public PageAdder(int delay){
			setTime(delay);
		}

		@Override
		public void run() {
			//TODO add muli-queueing protection

			setTime(0);

			for(Page p : pages){
				pageQueue.execute(p);
			}
		}

		/**
		 * Set the time displayed in the GUI.
		 * @param delay delay in minutes.
		 */
		private void setTime(int delay){
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.MINUTE, delay);
			DateFormat df;
			df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
			lastRun = df.format(cal.getTime());
		}
	}
}
