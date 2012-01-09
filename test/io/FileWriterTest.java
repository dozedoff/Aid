package io;

import static org.junit.Assert.*;

import java.io.File;

import javax.activity.InvalidActivityException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import filter.Filter;
import gui.BlockListDataModel;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Mockito.*;

//TODO re-write tests to use temp. folders
@SuppressWarnings("unused")
public class FileWriterTest {
	ConnectionPoolaid mockConnectionPoolaid = mock(ConnectionPoolaid.class);
	ThumbnailLoader mockThumbnailLoader = mock(ThumbnailLoader.class);
	Filter mockFilter = mock(Filter.class);
	
	FileWriter fileWriter;
	File testPath;
	byte[] testData = {12,45,6,12,99};
	File[] testFiles = {new File(testPath,"a\\test1.txt"),new File(testPath,"a\\test2.txt"),new File(testPath,"b\\test1.txt"),new File(testPath,"c\\test1.txt"),new File(testPath,"c\\test2.txt")};
	BlockListDataModel bldm;

	/**
	 * Create a new Filewriter for the test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		fileWriter = new FileWriter(mockFilter);
		testPath = File.createTempFile("test", null);	// create a temporary file to get the temp location
		testPath.delete();	// delete the temp file
		
		testPath = new File(testPath.toString()+"dir");	// create a temp directory based on the obtained path
		testPath.mkdirs();
		testPath.deleteOnExit();
	}
	/**
	 * Shutdown the Filewriter and delete files created during the test.
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		fileWriter.shutdown();

		for(File f : testFiles)
			f.delete();

		new File(testPath,"\\a").delete();
		new File(testPath,"\\b").delete();
		new File(testPath,"\\c").delete();

		testPath.delete();
	}
	/**
	 * Check that files are written to disk, and also check that buffer flushing works.
	 * @throws InvalidActivityException
	 * @throws InterruptedException
	 */
	@Test
	public void testAdd() throws InvalidActivityException, InterruptedException {
		for(File f : testFiles)
			fileWriter.add(f, testData);


		try{Thread.sleep(6000);}catch(InterruptedException ie){} // wait for buffer to clear

		for(File f : testFiles)
			assertTrue(f.exists());
	}
	
	/**
	 * Check that shutdown triggers a buffer flush.
	 * @throws InvalidActivityException
	 */
	@Test
	public void testShutdown() throws InvalidActivityException{
		for(File f : testFiles)
			fileWriter.add(f, testData);

		fileWriter.shutdown();

		for(File f : testFiles)
			assertTrue(f.exists());
	}

	@Test
	public void testGetPendingWrites() throws InvalidActivityException {
		for(File f : testFiles)
			fileWriter.add(f, testData);
		
		assertThat(fileWriter.getPendingWrites(), is(5));
		fileWriter.shutdown();
		assertThat(fileWriter.getPendingWrites(), is(0));
	}

	@Test
	public void testGetBytesSaved() throws InvalidActivityException {
		for(File f : testFiles)
			fileWriter.add(f, testData);
		
		fileWriter.shutdown();
		
		assertThat(fileWriter.getBytesSaved(), is(25L));
	}

	@Test
	public void testGetBytesDiscarded() throws InvalidActivityException {
		fileWriter = new FileWriter(mockFilter);
		when(mockFilter.exists(anyString())).thenReturn(true);
		
		for(File f : testFiles)
			fileWriter.add(f, testData);
		
		fileWriter.shutdown();
		
		assertThat(fileWriter.getBytesDiscarded(), is(25L));
	}
	
	@Test
	public void testInvalidFileName() throws Exception{
		fileWriter.add(new File(testPath,"ooops+%ç!<>.txt"), testData);
		Thread.sleep(6000);
	}
}