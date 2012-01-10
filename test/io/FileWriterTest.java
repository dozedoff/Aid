package io;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.activity.InvalidActivityException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import filter.Filter;
import gui.BlockListDataModel;
import hash.HashMaker;


public class FileWriterTest {
	ConnectionPoolaid mockConnectionPoolaid;
	ThumbnailLoader mockThumbnailLoader;
	Filter mockFilter;
	
	FileWriter fileWriter;
	File testDir;
	byte[] testData = {12,45,6,12,99};	// SHA-256: 95F6A79D2199FC2CFA8F73C315AA16B33BF3544C407B4F9B29889333CA0DB815
	byte[] testData2 = {99,21,6,45,12}; // SHA-256: 20FC038E00E13585E68E7EBE50D79CBE7D476A74D8FDE71872627DA6CD8FC8BB
	File[] testFilesRelative = {new File("a\\test1.txt"),new File("a\\test2.txt"),new File("b\\test1.txt"),new File("c\\test1.txt"),new File("c\\test2.txt")};
	ArrayList<File> testFiles;
	BlockListDataModel bldm;

	/**
	 * Create a new Filewriter for the test.
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		mockConnectionPoolaid = mock(ConnectionPoolaid.class);
		mockThumbnailLoader = mock(ThumbnailLoader.class);
		mockFilter = mock(Filter.class);
		
		fileWriter = new FileWriter(mockFilter);
		testDir = Files.createTempDirectory("fileWriterTest").toFile();
		testFiles = new ArrayList<>();
		
		for(File file : testFilesRelative){
			testFiles.add(new File(testDir,file.toString()));
		}
		
		testDir.deleteOnExit();
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

		new File(testDir,"\\a").delete();
		new File(testDir,"\\b").delete();
		new File(testDir,"\\c").delete();

		testDir.delete();
	}
	/**
	 * Check that files are written to disk, and also check that buffer flushing works.
	 * @throws InvalidActivityException
	 * @throws InterruptedException
	 * @throws SQLException 
	 */
	@Test
	public void testAdd() throws InvalidActivityException, InterruptedException, SQLException {
		for(File f : testFiles)
			fileWriter.add(f, testData);


		Thread.sleep(6000);// wait for buffer to clear

		for(File f : testFiles)
			assertTrue("File "+f.toString()+" not found",f.exists());
		
		verify(mockFilter,times(5)).addHash(eq("95F6A79D2199FC2CFA8F73C315AA16B33BF3544C407B4F9B29889333CA0DB815"), anyString(), eq(5));
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
			assertTrue("File "+f.toString()+" not found",f.exists());
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
		fileWriter.add(new File(testDir,"ooops+%ç!<>.txt"), testData);
		Thread.sleep(15000);
		
		ArrayList<String> filenames = new ArrayList<>();
		
		for(File file : testDir.listFiles()){
			filenames.add(file.getName());
		}
		
		assertThat(filenames,hasItem(both(containsString("renamed_")).and(containsString(".txt"))));
	}
	
	@Test
	public void testFileExistsDifferentData() throws InvalidActivityException, InterruptedException{
		fileWriter.add(new File(testDir,"foo.txt"), testData);
		fileWriter.add(new File(testDir,"foo.txt"), testData2);
		Thread.sleep(15000);
		ArrayList<String> filenames = new ArrayList<>();
		
		for(File file : testDir.listFiles()){
			filenames.add(file.getName());
		}
		
		assertThat(filenames,hasItem("foo.txt"));
		assertThat(filenames,hasItem(both(containsString("foo_")).and(containsString(".txt"))));
	}
	
	@Test
	public void testFileExistsSameData() throws InvalidActivityException, InterruptedException{
		fileWriter.add(new File(testDir,"foo.txt"), testData);
		fileWriter.add(new File(testDir,"foo.txt"), testData);
		Thread.sleep(15000);
		ArrayList<String> filenames = new ArrayList<>();
		
		for(File file : testDir.listFiles()){
			filenames.add(file.getName());
		}
		
		assertThat(filenames,hasItem("foo.txt"));
		assertThat(filenames.size(),is(1)); //TODO write custom matcher for "list does not contain" see: http://stackoverflow.com/q/6520546/891292
	}
	
	@Test
	public void testClearStats() throws InvalidActivityException, InterruptedException{
		when(mockFilter.exists("20FC038E00E13585E68E7EBE50D79CBE7D476A74D8FDE71872627DA6CD8FC8BB")).thenReturn(true);
		fileWriter.add(new File(testDir,"foo.txt"), testData);
		fileWriter.add(new File(testDir,"bar.txt"),testData2);
		Thread.sleep(6000);
		assertThat(fileWriter.getBytesSaved(), is(5L));
		assertThat(fileWriter.getBytesDiscarded(), is(5L));
		
		fileWriter.clearStats();
		
		assertThat(fileWriter.getBytesSaved(), is(0L));
		assertThat(fileWriter.getBytesDiscarded(), is(0L));
	}
	
	
	@Test
	public void testBlacklistedNoWrite() throws InvalidActivityException, InterruptedException{
		when(mockFilter.isBlacklisted("95F6A79D2199FC2CFA8F73C315AA16B33BF3544C407B4F9B29889333CA0DB815")).thenReturn(true);
		fileWriter.add(new File(testDir,"foo.bar"), testData);
		fileWriter.shutdown();
		
		Thread.sleep(1000);
		
		ArrayList<String> filenames = new ArrayList<>();
		
		for(File file : testDir.listFiles()){
			filenames.add(file.getName());
		}
		
		assertThat(filenames, hasItem("WARNING-95F6A79D2199FC2CFA8F73C315AA16B33BF3544C407B4F9B29889333CA0DB815-foo.bar.txt"));
	}
	
	@Test
	public void testBlacklistedWrite() throws InvalidActivityException, InterruptedException{
		when(mockFilter.isBlacklisted("95F6A79D2199FC2CFA8F73C315AA16B33BF3544C407B4F9B29889333CA0DB815")).thenReturn(true);
		fileWriter.setWriteBlocked(true);
		fileWriter.add(new File(testDir,"foo.bar"), testData);
		fileWriter.shutdown();
		
		Thread.sleep(1000);
		
		ArrayList<String> filenames = new ArrayList<>();
		
		for(File file : testDir.listFiles()){
			filenames.add(file.getName());
		}
		
		assertThat(filenames, hasItem("WARNING-95F6A79D2199FC2CFA8F73C315AA16B33BF3544C407B4F9B29889333CA0DB815-foo.bar"));
	}
}