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
import static org.junit.matchers.JUnitMatchers.hasItem;
import io.tables.FilePathRecord;
import io.tables.IndexRecord;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.spring.DaoFactory;

public class ORMliteForeignLearningTest {
	JdbcPooledConnectionSource pool;
	DaoManager manager;
	
	@Before
	public void setUp() throws Exception {
			createConnectionPool();
			
			manager = new DaoManager();
	}
	
	private void createConnectionPool() throws Exception{
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
		Dao<IndexRecord, String> indexDAO = DaoFactory.createDao(pool, IndexRecord.class);
		Dao<FilePathRecord, Integer> fileNameDAO = DaoFactory.createDao(pool, FilePathRecord.class);
		IndexRecord record = new IndexRecord();
		
		record.setId("100");
		record.setLocation("TEST");
		record.setSize(9001);
		record.setRelativePath(Paths.get("/non-existent/path/", "invisible.file"));
		
		indexDAO.createIfNotExists(record);
		
		FilePathRecord recordToFind = new FilePathRecord();
		recordToFind.setFilename("invisible.file");
		
		List<FilePathRecord> filepath = fileNameDAO.queryForMatching(recordToFind);
		
		assertThat(filepath, hasItem(recordToFind));
	}

}
