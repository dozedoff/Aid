package learning;
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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;
import file.FileInfo;
import io.dao.IndexDAO;
import io.tables.DirectoryPathRecord;
import io.tables.FilePathRecord;
import io.tables.IndexRecord;
import io.tables.LocationRecord;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.spring.DaoFactory;

public class ORMliteForeignLearningTest {
	private static JdbcPooledConnectionSource pool;
	DaoManager manager;
	
	@BeforeClass
	public static void setUp() throws Exception {
			createConnectionPool();
	}
	
	private static void createConnectionPool() throws Exception{
		pool = new JdbcPooledConnectionSource("jdbc:mysql://localhost/test", "test", "test");
		pool.setTestBeforeGet(true);
		pool.initialize();
	}

	@Test
	public void testReadRecord() throws SQLException {
		Dao<IndexRecord, String> indexDAO = DaoFactory.createDao(pool, IndexRecord.class);
		IndexRecord record = indexDAO.queryForId("1");
		Path relativePath = record.getRelativePath();
		String filename = relativePath.getFileName().toString();
		
		assertNotNull(record.getRelativePath());
		assertThat(filename, is("foo.png"));
	}
	
	@Test
	public void testWriteRecord() throws SQLException {
		IndexDAO indexDAO = new IndexDAO(pool);
		
		String hash = String.valueOf(Calendar.getInstance().getTimeInMillis());
		
		FileInfo info = new FileInfo(Paths.get("\\bar\\foo\\", "apple.txt"), hash);
		LocationRecord location = new LocationRecord("LOCATION A");
		IndexRecord index = new IndexRecord(info, location);
		indexDAO.create(index);
		
		assertTrue(indexDAO.idExists(hash));
	}
	
	@Test
	public void testLookup() throws SQLException {
		Dao<DirectoryPathRecord, Integer> directoryDAO = DaoManager.createDao(pool, DirectoryPathRecord.class);
		DirectoryPathRecord dirRec = new DirectoryPathRecord();
		dirRec.setDirpath("\\foo\\bar\\");
		List<DirectoryPathRecord> result = directoryDAO.queryForMatchingArgs(dirRec);
		
		if(result.isEmpty()){
			directoryDAO.create(dirRec);
		}else{
			dirRec = result.get(0);
		}
		
		assertThat(dirRec.getId(), is(1));
	}
}
