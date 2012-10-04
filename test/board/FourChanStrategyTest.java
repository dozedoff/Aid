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
package board;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import io.TextFileReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.hamcrest.core.Is;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FourChanStrategyTest {
	SiteStrategy strategy;
	static Server server = new Server();
	static String mainPage, page, thread;
	
	@BeforeClass
	static public void before() throws Exception{
		server.setHandler(new TestHandler());
		server.start();
		
		TextFileReader tfr = new TextFileReader();
		
		
		mainPage = tfr.read(ClassLoader.getSystemResourceAsStream("HtmlData\\mainPage.html"));
	}

	@Before
	public void setUp() throws Exception {
		strategy = new FourChanStrategy();
	}
	
	@AfterClass
	static public void after() throws Exception{
		server.stop();
	}

	@Test
	public void testValidSiteStrategy() {
		fail("Not yet implemented");
	}

	@Test
	public void testFindBoards() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetBoardPageCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testParsePage() throws Exception {
		fail("Not yet implemented");
	}

	@Test
	public void testParseThread() {
		fail("Not yet implemented");
	}
	
	static class TestHandler extends AbstractHandler{
		@Override
		public void handle(String arg0, Request baseRequest, HttpServletRequest request,
				HttpServletResponse response) throws IOException, ServletException {

			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			
			if(request.getRequestURI().equals("/")){
			
			}else if(request.getRequestURI().equals("/a/2")){
				//TODO add page data here
			}else if(request.getRequestURI().equals("a/res/1234567")){
				//TODO add thread data here
			}else{
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		}
	}

}
