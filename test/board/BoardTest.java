package board;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import thread.WorkQueue;

public class BoardTest {
	Board board;
	
	LinkedList<Page> pages;
	WorkQueue pageQueue;
	Page page;
	
	
	@Before
	public void setup(){
		pages = new LinkedList<>();
		pageQueue = mock(WorkQueue.class);
		page = mock(Page.class);
		
		pages.add(page);
		
		board = new Board(pages, pageQueue, "t");
	}
	
	@Test
	public void testToString() {
		assertThat(board.toString(), is("/t/"));
	}

	@Test
	public void testGetStatus() {
		assertThat(board.getStatus(), is("/t/  idle"));
		board.start();
		assertThat(board.getStatus(), containsString("running"));
		board.stop();
		assertThat(board.getStatus(), is("/t/  idle"));
	}
}
