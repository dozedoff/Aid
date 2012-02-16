package io;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CachePruneTest {
	CachePrune cachePrune;
	MySQLaid sql;
	@Before
	public void setUp() throws Exception {
		sql = mock(MySQLaid.class);
		cachePrune = new CachePrune(sql, new URL("http://www.google.com"), 1, 0, 1);
	}

	@After
	public void tearDown() throws Exception {
		cachePrune.stop();
	}

	@Test
	public void test() throws InterruptedException {
		when(sql.size(MySQLtables.Cache)).thenReturn(4);
		assertThat(cachePrune.start(), is(true));
		Thread.sleep(1000*3);
		
		verify(sql,times(1)).pruneCache(anyLong());
	}

}
