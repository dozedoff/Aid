/*  Copyright (C) 2012  Nicholas Wright

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
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;

import org.dbunit.Assertion;
import org.dbunit.DatabaseTestCase;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import config.DefaultMySQLconnection;

import filter.FilterItem;
import filter.FilterState;


public class MySqlAidTest extends DatabaseTestCase{
	AidDAO sql;
	BoneConnectionPool bcp;
	final String[] IGNORE_CACHE_COL = {"timestamp"};
	final String[] IGNORE_THUMBS_DATA_COL = {"id"};
	final String[] IGNORE_THUMBS_TRIGGER_COL = {"id","thumb"};
	final String[] IGNORE_PATH_COL = {"id"};
	final String[] IGNORE_ADD_HASH_COL = {"dir","filename"};
	
	final byte[] TEST_BINARY = {1,2,3,4,5,6,7,8,9,0};

	@Before
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		bcp = new BoneConnectionPool(new DefaultMySQLconnection("127.0.0.1", 3306, "test", "test", "test"), 10);
		bcp.startPool();
		sql = new AidDAO(bcp);
	}

	@After
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		bcp.stopPool();
	}

	@Test
	public void testAddFilter() throws Exception {
		assertTrue(sql.addFilter("http://foo.bar/PENDING","t", "test", FilterState.PENDING));
		assertTrue(sql.addFilter("http://foo.bar/ALLOW","t", "test", FilterState.ALLOW));
		assertTrue(sql.addFilter("http://foo.bar/DENY", "t","test", FilterState.DENY));
		assertTrue(sql.addFilter("http://foo.bar/UNKNOWN","t", "test", FilterState.UNKNOWN));
		assertFalse(sql.addFilter("http://foo.bar/PENDING", "t", "test", FilterState.PENDING));
		assertFalse(sql.addFilter("http://foo.bar/1", "t", "just testing", FilterState.ALLOW));

		// Assert actual database table match expected table
		Assertion.assertEqualsIgnoreCols(getFileTable("filter", "/dbData/addExpected.xml"), getDatabaseTable("filter"),IGNORE_CACHE_COL);
	}
	
	@Test
	public void testAddFilterFilteritem() throws DataSetException, DatabaseUnitException, SQLException, Exception{
//		new FilterItem(board, reason, url, state)
		assertTrue(sql.addFilter(new FilterItem(new URL("http://foo.bar/PENDING"),"t", "test", FilterState.PENDING)));
		assertTrue(sql.addFilter(new FilterItem(new URL("http://foo.bar/ALLOW"),"t", "test", FilterState.ALLOW)));
		assertTrue(sql.addFilter(new FilterItem(new URL("http://foo.bar/DENY"), "t","test", FilterState.DENY)));
		assertTrue(sql.addFilter(new FilterItem(new URL("http://foo.bar/UNKNOWN"),"t", "test", FilterState.UNKNOWN)));
		assertFalse(sql.addFilter(new FilterItem(new URL("http://foo.bar/PENDING"), "t", "test", FilterState.PENDING)));
		assertFalse(sql.addFilter(new FilterItem(new URL("http://foo.bar/1"), "t", "just testing", FilterState.ALLOW)));

		// Assert actual database table match expected table
		Assertion.assertEqualsIgnoreCols(getFileTable("filter", "/dbData/addExpected.xml"), getDatabaseTable("filter"),IGNORE_CACHE_COL);
	}

	@Test
	public void testGetFilterState(){
		assertThat(sql.getFilterState("http://foo.bar/1"), is(FilterState.DENY));
		assertThat(sql.getFilterState("http://foo.bar/4"), is(FilterState.UNKNOWN));
	}

	@Test
	public void testPending() {
		assertThat(sql.getPending(), is(4));
	}

	@Test
	public void testUpdateState() throws Exception{
		sql.updateState("http://foo.bar/1",FilterState.PENDING);
		sql.updateState("http://foo.bar/2",FilterState.DENY);
		sql.updateState("http://foo.bar/3",FilterState.PENDING);
		sql.updateState("http://foo.bar/4",FilterState.PENDING);

		Assertion.assertEqualsIgnoreCols(getFileTable("filter", "/dbData/updateStateExpected.xml"), getDatabaseTable("filter"),IGNORE_CACHE_COL);
	}

	@Test
	public void testDelete() throws Exception{
		// filter table
		sql.delete(AidTables.Filter, "http://foo.bar/1");
		sql.delete(AidTables.Filter, "http://foo.bar/2");
		sql.delete(AidTables.Filter, "http://foo.bar/3");
		sql.delete(AidTables.Filter, "http://foo.bar/4");

		Assertion.assertEqualsIgnoreCols(getFileTable("filter", "/dbData/deleteExpected.xml"), getDatabaseTable("filter"),IGNORE_CACHE_COL);
		
		// hash table
		sql.delete(AidTables.Hash, "2");
		sql.delete(AidTables.Hash, "3");
		
		Assertion.assertEquals(getFileTable("hash", "/dbData/deleteExpected.xml"), getDatabaseTable("hash"));
		
		// dnw table
		sql.delete(AidTables.Dnw, "3");
		sql.delete(AidTables.Dnw, "4");
		
		Assertion.assertEquals(getFileTable("dnw", "/dbData/deleteExpected.xml"), getDatabaseTable("dnw"));
		
		// block table
		sql.delete(AidTables.Block, "1");
		sql.delete(AidTables.Block, "4");

		Assertion.assertEquals(getFileTable("block", "/dbData/deleteExpected.xml"), getDatabaseTable("block"));
		
		// archive table
		sql.delete(AidTables.Archive, "3");
		sql.delete(AidTables.Archive, "4");

		Assertion.assertEquals(getFileTable("archive", "/dbData/deleteExpected.xml"), getDatabaseTable("archive"));
	}

	@Test
	public void testFilterSize(){
		assertThat(sql.size(AidTables.Filter), is(8));
	}

	@Test
	public void testCacheSize(){
		assertThat(sql.size(AidTables.Cache), is(4));
	}
	
	@Test
	public void testAddCache() throws Exception{
		sql.addCache(new URL("http://foo.bar.com"));
		sql.addCache(new URL("http://squirrel.net"));

		Assertion.assertEqualsIgnoreCols(getFileTable("cache", "/dbData/addExpected.xml"), getDatabaseTable("cache"), IGNORE_CACHE_COL);
	}
	
	@Test
	public void testAddThumb() throws Exception{
		sql.addThumb("4","apple.png", "12345".getBytes());
		
		Assertion.assertEqualsIgnoreCols(getFileTable("thumbs", "/dbData/addExpected.xml"), getDatabaseTable("thumbs"), IGNORE_THUMBS_DATA_COL);
	}
	
	@Test
	public void testgetThumb(){
		// needs a better test. maybe add resource, write to db and then read back and compare?
		assertThat(sql.getThumb("http://foo.bar/3").size(), is(4));
	}
	
	@Test
	public void testTriggerThumbsUpdate() throws Exception{
		sql.updateState("http://foo.bar/1", FilterState.ALLOW);
		sql.updateState("http://foo.bar/2", FilterState.ALLOW);
		
		Assertion.assertEqualsIgnoreCols(getFileTable("thumbs", "/dbData/triggerExpected.xml"), getDatabaseTable("thumbs"), IGNORE_THUMBS_TRIGGER_COL);
	}
	
	@Test
	public void testTriggerThumbsDelete() throws Exception{
		sql.delete(AidTables.Filter, "http://foo.bar/1");
		sql.delete(AidTables.Filter, "http://foo.bar/2");
		
		Assertion.assertEqualsIgnoreCols(getFileTable("thumbs", "/dbData/triggerExpected.xml"), getDatabaseTable("thumbs"), IGNORE_THUMBS_TRIGGER_COL);
	}
	
	@Test
	public void testIsCached(){
		assertTrue(sql.isCached("1"));
		assertFalse(sql.isCached("6"));
		assertFalse(sql.isCached("http://foo.bar/"));
	}
	
	@Test
	public void testIsArchived(){
		assertTrue(sql.isArchived("1"));
		assertFalse(sql.isArchived("my spoon is too big!"));
	}
	
	@Test
	public void testIsDnw(){
		assertTrue(sql.isDnw("2"));
		assertFalse(sql.isDnw("Brussels sprouts"));
	}
	
	@Test
	public void testIsHashed(){
		assertTrue(sql.isHashed("1"));
		assertFalse(sql.isHashed("bananas!"));
	}
	
	@Test
	public void testIsBlacklisted(){
		assertTrue(sql.isBlacklisted("1"));
		assertFalse(sql.isBlacklisted("45345"));
	}
	
	@Test
	public void testAddHash() throws Exception{
		sql.addIndex("54321", "D:\\foo\\panda.png", 123455L, "DL CLIENT");
		
		Assertion.assertEqualsIgnoreCols(getFileTable("hash", "/dbData/addExpected.xml"), getDatabaseTable("hash"), IGNORE_ADD_HASH_COL);
		Assertion.assertEqualsIgnoreCols(getFileTable("dirlist", "/dbData/addExpected.xml"), getDatabaseTable("dirlist"), IGNORE_PATH_COL);
		Assertion.assertEqualsIgnoreCols(getFileTable("filelist", "/dbData/addExpected.xml"), getDatabaseTable("filelist"), IGNORE_PATH_COL);
	}
	
	@Test
	public void testGetPendingFilters() throws MalformedURLException{
		LinkedList<FilterItem> pendingItems = sql.getPendingFilters();
		
		assertThat(pendingItems.size(), is(4));
		assertThat(pendingItems, hasItem(new FilterItem(new URL("http://foo.bar/2"),"a", "foo bar", FilterState.PENDING)));
		assertThat(pendingItems, hasItem(new FilterItem(new URL("http://foo.bar/5"),"vx", "and more",  FilterState.PENDING)));
		assertThat(pendingItems, hasItem(new FilterItem(new URL("http://foo.bar/6"),"zb", "om nom nom",  FilterState.PENDING)));
		assertThat(pendingItems, hasItem(new FilterItem(new URL("http://foo.bar/7"),"sa", "rain",  FilterState.PENDING)));
	}
	
	@Test
	public void testGetOldestFilter(){
		assertThat(sql.getOldestFilter(), is("http://foo.bar/1"));
		sql.delete(AidTables.Filter, "http://foo.bar/1");
		assertThat(sql.getOldestFilter(), is("http://foo.bar/2"));
		sql.delete(AidTables.Filter, "http://foo.bar/2");
		assertThat(sql.getOldestFilter(), is("http://foo.bar/3"));
	}
	
	@Test
	public void testUpdateFilterTimestamp() throws InterruptedException{
		assertThat(sql.getOldestFilter(), is("http://foo.bar/1"));
		Thread.sleep(1100);
		sql.updateFilterTimestamp(sql.getOldestFilter());
		assertThat(sql.getOldestFilter(), is("http://foo.bar/2"));
		Thread.sleep(1100);
		sql.updateFilterTimestamp(sql.getOldestFilter());
		assertThat(sql.getOldestFilter(), is("http://foo.bar/3"));
	}
	
	@Test
	public void testCachePrune(){
		sql.pruneCache(Calendar.getInstance().getTimeInMillis());
		assertThat(sql.size(AidTables.Cache), is(1));
		assertThat(sql.isCached("1"), is(true));
	}
	
	// ---------- Database Setup related methods ---------- //

	@Override
	protected IDatabaseConnection getConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver"); 
		Connection jdbcConnection = DriverManager.getConnection( "jdbc:mysql://localhost/test","test", "test"); 

		return new DatabaseConnection(jdbcConnection);
	}

	@Override
	protected IDataSet getDataSet() throws Exception {
		IDataSet dataSet = new FlatXmlDataFileLoader().load("/dbData/MySqlTest.xml");

		return dataSet;
	}

	private ITable getDatabaseTable(String tableName) throws SQLException, Exception{
		IDataSet databaseDataSet = getConnection().createDataSet();
		return databaseDataSet.getTable(tableName);
	}

	private ITable getFileTable(String tableName, String fileName) throws MalformedURLException, DataSetException{
		IDataSet expectedDataSet = new FlatXmlDataFileLoader().load(fileName);
		return expectedDataSet.getTable(tableName);
	}
}
