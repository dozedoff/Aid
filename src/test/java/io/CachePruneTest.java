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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CachePruneTest {
	CachePrune cachePrune;
	AidDAO sql;
	static Server server;
	private static int SERVER_PORT = 5980;
	
	@BeforeClass
	public static void startServer() throws Exception{
		server  = new Server(SERVER_PORT);
		server.setHandler(new TestHandler());
		server.start();
	}
	
	@Before
	public void setUp() throws Exception {
		sql = mock(AidDAO.class);
		cachePrune = new CachePrune(sql, new URL("http://localhost:" + SERVER_PORT + "/"), 2, 0, 1);
	}

	@After
	public void tearDown() throws Exception {
		cachePrune.stop();
	}
	
	@AfterClass
	public static void stopServer() throws Exception{
		server.stop();
	}

	@Test
	public void testCachePrune() throws InterruptedException {
		when(sql.size(AidTables.Cache)).thenReturn(4);
		assertThat(cachePrune.start(), is(true));
		Thread.sleep(1100);
		
		verify(sql,times(1)).pruneCache(anyLong());
	}
	
	static class TestHandler extends AbstractHandler{
		@Override
		public void handle(String arg0, Request baseRequest, HttpServletRequest request,
				HttpServletResponse response) throws IOException, ServletException {

			response.setContentType("application/octet-stream");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
		}
	}
}
