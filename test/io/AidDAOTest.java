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
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedList;

import org.dbunit.Assertion;
import org.dbunit.DatabaseTestCase;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import config.DefaultMySQLconnection;

import file.FileInfo;
import filter.FilterItem;
import filter.FilterState;


public class AidDAOTest extends DatabaseTestCase{
	static boolean done = false;
	static AidDAO sql;
	static BoneConnectionPool bcp = null;
	final String[] IGNORE_CACHE_COL = {"timestamp"};
	final String[] IGNORE_THUMBS_DATA_COL = {"id"};
	final String[] IGNORE_THUMBS_TRIGGER_COL = {"id","thumb"};
	final String[] IGNORE_PATH_COL = {"id"};
	final String[] IGNORE_ADD_HASH_COL = {"dir","filename"};
	
	final String[] TEST_DIR = {"", "D:\\foo\\bar\\", "D:\\test\\me\\now\\", "D:\\mutated\\custard\\is\\dangerous\\"};
	final String[] TEST_FILE = {"", "foo.png", "squirrel.jpg", "meerkat.gif"};
			
	final String AidDAOTest_PATH = "/dbData/AidDAOTest.xml";
	final String addExpected_PATH = "/dbData/addExpected.xml";
	final String deleteExpected_PATH = "/dbData/deleteExpected.xml";
	final String triggerExpected_PATH = "/dbData/triggerExpected.xml";
	final String updateStateExpected_PATH = "/dbData/updateStateExpected.xml";
	
	
	final byte[] TEST_BINARY = {1,2,3,4,5,6,7,8,9,0};
	static{
		try {
			setupPool();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setupPool() throws Exception {
		if(! done){
			bcp = new BoneConnectionPool(new DefaultMySQLconnection("127.0.0.1", 3306, "test", "test", "test"), 10);
			bcp.startPool();
			System.out.println("Pool created");
			sql = new AidDAO(bcp);
		}
	}

	@After
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
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
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Filter.toString(), addExpected_PATH), getDatabaseTable(AidTables.Filter.toString()),IGNORE_CACHE_COL);
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
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Filter.toString(), addExpected_PATH), getDatabaseTable(AidTables.Filter.toString()),IGNORE_CACHE_COL);
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

		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Filter.toString(), updateStateExpected_PATH), getDatabaseTable(AidTables.Filter.toString()),IGNORE_CACHE_COL);
	}

	@Test
	public void testDelete() throws Exception{
		// filter table
		sql.delete(AidTables.Filter, "http://foo.bar/1");
		sql.delete(AidTables.Filter, "http://foo.bar/2");
		sql.delete(AidTables.Filter, "http://foo.bar/3");
		sql.delete(AidTables.Filter, "http://foo.bar/4");

		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Filter.toString(), deleteExpected_PATH), getDatabaseTable(AidTables.Filter.toString()),IGNORE_CACHE_COL);
		
		// fileindex table
		sql.delete(AidTables.Fileindex, "2");
		sql.delete(AidTables.Fileindex, "3");
		
		Assertion.assertEquals(getFileTable(AidTables.Fileindex.toString(), deleteExpected_PATH), getDatabaseTable(AidTables.Fileindex.toString()));
		
		// dnw table
		sql.delete(AidTables.Dnw, "3");
		sql.delete(AidTables.Dnw, "4");
		
		Assertion.assertEquals(getFileTable(AidTables.Dnw.toString(), deleteExpected_PATH), getDatabaseTable(AidTables.Dnw.toString()));
		
		// block table
		sql.delete(AidTables.Block, "1");
		sql.delete(AidTables.Block, "4");

		Assertion.assertEquals(getFileTable(AidTables.Block.toString(), deleteExpected_PATH), getDatabaseTable(AidTables.Block.toString()));
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

		Assertion.assertEqualsIgnoreCols(getFileTable("cache", addExpected_PATH), getDatabaseTable("cache"), IGNORE_CACHE_COL);
	}
	
	@Test
	public void testAddThumb() throws Exception{
		sql.addThumb("4","apple.png", "12345".getBytes());
		
		Assertion.assertEqualsIgnoreCols(getFileTable("thumbs", addExpected_PATH), getDatabaseTable("thumbs"), IGNORE_THUMBS_DATA_COL);
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
		
		Assertion.assertEqualsIgnoreCols(getFileTable("thumbs", triggerExpected_PATH), getDatabaseTable("thumbs"), IGNORE_THUMBS_TRIGGER_COL);
	}
	
	@Test
	public void testTriggerThumbsDelete() throws Exception{
		sql.delete(AidTables.Filter, "http://foo.bar/1");
		sql.delete(AidTables.Filter, "http://foo.bar/2");
		
		Assertion.assertEqualsIgnoreCols(getFileTable("thumbs", triggerExpected_PATH), getDatabaseTable("thumbs"), IGNORE_THUMBS_TRIGGER_COL);
	}
	
	@Test
	public void testIsCached(){
		assertTrue(sql.isCached("1"));
		assertFalse(sql.isCached("6"));
		assertFalse(sql.isCached("http://foo.bar/"));
	}
	
	
	//FIXME test is useless as it is. Update test data
	@Test
	public void testIsCachedURL() throws MalformedURLException{
		assertFalse(sql.isCached(new URL("http://foo.bar/")));
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
	public void testAddIndex() throws Exception{
		sql.addIndex("54321", "D:\\foo\\panda.png", 123455L, "LOCATION A");
		
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Fileindex.toString(), addExpected_PATH), getDatabaseTable(AidTables.Fileindex.toString()), IGNORE_ADD_HASH_COL);
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Dirlist.toString(), addExpected_PATH), getDatabaseTable(AidTables.Dirlist.toString()), IGNORE_PATH_COL);
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Filelist.toString(), addExpected_PATH), getDatabaseTable(AidTables.Filelist.toString()), IGNORE_PATH_COL);
	}
	
	@Test
	public void testAddIndexFileInfo() throws Exception{
		Path filePath = Paths.get("D:\\foo\\panda.png");
		FileInfo info = new FileInfo(filePath, "54321");
		info.setSize(123455L);
		
		sql.addIndex(info, "LOCATION A");
		
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Fileindex.toString(), addExpected_PATH), getDatabaseTable(AidTables.Fileindex.toString()), IGNORE_ADD_HASH_COL);
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Dirlist.toString(), addExpected_PATH), getDatabaseTable(AidTables.Dirlist.toString()), IGNORE_PATH_COL);
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Filelist.toString(), addExpected_PATH), getDatabaseTable(AidTables.Filelist.toString()), IGNORE_PATH_COL);
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
	
	@Test
	public void testGetTagId(){
		assertThat(sql.getTagId("LOCATION A"), is(1));
		assertThat(sql.getTagId("LOCATION B"), is(2));
		assertThat(sql.getTagId("NOT-IN-LIST"), is(-1));
	}
	
	@Test
	public void testIsValidTag(){
		assertTrue(sql.isValidTag("UNKNOWN"));
		assertTrue(sql.isValidTag("LOCATION A"));
		assertTrue(sql.isValidTag("LOCATION B"));
		assertFalse(sql.isValidTag("NOT-IN-LIST"));
		assertFalse(sql.isValidTag(null));
	}
	
	@Test
	public void testAddDuplicate() throws Exception{
		sql.addDuplicate("545", "D:\\foo\\panda.png", 123L, "LOCATION A");
		
		
		//TODO can ignore cols be removed?
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Fileduplicate.toString(), addExpected_PATH), getDatabaseTable(AidTables.Fileduplicate.toString()), IGNORE_ADD_HASH_COL);
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Dirlist.toString(), addExpected_PATH), getDatabaseTable(AidTables.Dirlist.toString()), IGNORE_PATH_COL);
		Assertion.assertEqualsIgnoreCols(getFileTable(AidTables.Filelist.toString(), addExpected_PATH), getDatabaseTable(AidTables.Filelist.toString()), IGNORE_PATH_COL);
	}
	
	@Test
	public void testGetLocationIndexSize(){
		assertThat(sql.getLocationIndexSize("UNKNOWN"), is(1));
		assertThat(sql.getLocationIndexSize("LOCATION A"), is(1));
		assertThat(sql.getLocationIndexSize("LOCATION B"), is(2));
	}
	
	@Test
	public void testGetLocationFilelist(){
		assertThat(sql.getLocationFilelist("UNKNOWN").size(),is(1));
		assertThat(sql.getLocationFilelist("UNKNOWN"), hasItem("\\foo\\bar\\foo.png"));
		
		assertThat(sql.getLocationFilelist("LOCATION A").size(),is(1));
		assertThat(sql.getLocationFilelist("LOCATION A"), hasItem("\\test\\me\\now\\squirrel.jpg"));
		
		assertThat(sql.getLocationFilelist("LOCATION B").size(),is(2));
		assertThat(sql.getLocationFilelist("LOCATION B"), hasItem("\\mutated\\custard\\is\\dangerous\\meerkat.gif"));
		assertThat(sql.getLocationFilelist("LOCATION B"), hasItem("\\mutated\\custard\\is\\dangerous\\squirrel.jpg"));
	}
	
	@Test
	public void testDirectoryLookup() throws SQLException{
		assertThat(sql.directoryLookup("foo"), is(-1));
		assertThat(sql.directoryLookup("D:\\foo\\bar\\"), is(1));
	}
	
	@Test
	public void testFileLookup() throws SQLException{
		assertThat(sql.fileLookup("derp"), is(-1));
		assertThat(sql.fileLookup("meerkat.gif"), is(3));
	}
	
	@Test
	public void testIsIndexedPath(){
		assertTrue(sql.isIndexedPath(Paths.get(TEST_DIR[1]+TEST_FILE[1]), "UNKNOWN"));
		assertFalse(sql.isIndexedPath(Paths.get(TEST_DIR[1]+TEST_FILE[1]), "LOCATION A"));
		
		assertTrue(sql.isIndexedPath(Paths.get(TEST_DIR[3]+TEST_FILE[2]), "LOCATION B"));
		assertFalse(sql.isIndexedPath(Paths.get(TEST_DIR[3]+TEST_FILE[2]), "LOCATION A"));
	}
	
	@Test
	public void testDeleteIndexViaPath() throws Exception{
		assertThat(sql.deleteIndexByPath("D:\\test\\me\\now\\squirrel.jpg"),is(1));
		assertThat(sql.deleteIndexByPath("D:\\mutated\\custard\\is\\dangerous\\meerkat.gif"),is(1));

		Assertion.assertEquals(getFileTable(AidTables.Fileindex.toString(), deleteExpected_PATH), getDatabaseTable(AidTables.Fileindex.toString()));
	}
	
	@Test
	public void testDeleteByPathString() throws Exception {
		sql.deleteDuplicateByPath("C:\\mutated\\custard\\is\\dangerous\\squirrel.jpg");
		
		Assertion.assertEquals(getFileTable(AidTables.Fileduplicate.toString(), deleteExpected_PATH), getDatabaseTable(AidTables.Fileduplicate.toString()));
	}
	
	@Test
	public void testDeleteByPath() throws Exception {
		Path path = Paths.get("C:\\mutated\\custard\\is\\dangerous\\squirrel.jpg");
		sql.deleteDuplicateByPath(path);
		
		Assertion.assertEquals(getFileTable(AidTables.Fileduplicate.toString(), deleteExpected_PATH), getDatabaseTable(AidTables.Fileduplicate.toString()));
	}
	
	@Test
	public void testMoveIndexToDuplicate() {
		final String HASH = "5";
		final String PATH = "D:\\test\\me\\now\\squirrel.jpg";
		final String LOCATION = "LOCATION A";
		final long SIZE = 123L;
			
		sql.addIndex(HASH, PATH, SIZE, LOCATION);
		assertTrue(sql.isHashed(HASH));
		
		assertTrue(sql.moveIndexToDuplicate(HASH));
		
		assertFalse(sql.isHashed(HASH));
		assertThat(sql.size(AidTables.Fileduplicate), is(5));
	}
	
	@Test
	public void testMoveDuplicateToIndex() {
		final String HASH = "5";
		final String PATH = "D:\\test\\me\\now\\squirrel.jpg";
		final String PATH2 = "D:\\foo\\bar\\meerkat.gif";
		final String LOCATION = "LOCATION A";
		final long SIZE = 123L;
		
		assertTrue(sql.addDuplicate(HASH, PATH, SIZE, LOCATION));
		assertTrue(sql.addDuplicate(HASH, PATH2, SIZE, LOCATION));
		assertThat(sql.size(AidTables.Fileduplicate), is(6));
		
		assertTrue(sql.moveDuplicateToIndex(HASH));
		assertThat(sql.size(AidTables.Fileduplicate), is(5));
		assertTrue(sql.isHashed(HASH));
	}
	
	@Test
	public void testGetLocationById() {
		String location = sql.getLocationById("1");
		assertThat(location, is("UNKNOWN"));
	}
	
	// ---------- Database Setup related methods ---------- //
	
	@Override
	protected IDatabaseConnection getConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver"); 
		Connection jdbcConnection = DriverManager.getConnection( "jdbc:mysql://localhost/test","test", "test"); 
		
		DatabaseConnection dbConn = new DatabaseConnection(jdbcConnection);
		dbConn.getConfig().setProperty("http://www.dbunit.org/properties/datatypeFactory", new MySqlDataTypeFactory());
		
		return dbConn;
	}

	@Override
	protected IDataSet getDataSet() throws Exception {
		IDataSet dataSet = new FlatXmlDataFileLoader().load(AidDAOTest_PATH);

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
