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
import static file.FileUtil.convertDirPathToString;
import static file.FileUtil.removeDriveLetter;
import file.FileInfo;
import filter.FilterItem;
import filter.FilterState;
import io.dao.DuplicateDAO;
import io.dao.IndexDAO;
import io.dao.LocationDAO;
import io.tables.Cache;
import io.tables.DirectoryPathRecord;
import io.tables.DuplicateRecord;
import io.tables.FilePathRecord;
import io.tables.FileRecord;
import io.tables.IndexRecord;
import io.tables.LocationRecord;
import io.tables.Thumbnail;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
/**
 * Class for database communication.
 */
public class AidDAO{
	private static final HashMap<String, String> prepStmts = new HashMap<String, String>();
	protected static Logger logger = Logger.getLogger(AidDAO.class.getName());
	protected final String RS_CLOSE_ERR = "Could not close ResultSet: ";
	protected final String SQL_OP_ERR = "MySQL operation failed: ";
	private final String DEFAULT_LOCATION = "UNKNOWN";
	protected final ConnectionPool connPool;
	
	private Dao<Cache, String> cacheDAO = null;
	private Dao<Thumbnail, Integer> ThumbnailDAO = null;
	private LocationDAO locationDao = null;
	private IndexDAO indexDao;
	private Dao<DirectoryPathRecord, Integer> directoryDAO;
	private Dao<FilePathRecord, Integer> fileDAO;
	private DuplicateDAO duplicateDAO;
	

	public AidDAO(ConnectionPool connPool){
		this.connPool = connPool;
		createDaos();
	}
	
	private void createDaos() {
		try{
			ConnectionSource cSource = connPool.getConnectionSource();
			
			cacheDAO = DaoManager.createDao(cSource, Cache.class);
			ThumbnailDAO = DaoManager.createDao(cSource, Thumbnail.class);
			indexDao = new IndexDAO(cSource);
			DaoManager.registerDao(cSource, indexDao);
			locationDao = new LocationDAO(cSource);
			DaoManager.registerDao(cSource, locationDao);
			directoryDAO = DaoManager.createDao(cSource, DirectoryPathRecord.class);
			fileDAO = DaoManager.createDao(cSource, FilePathRecord.class);
			duplicateDAO = new DuplicateDAO(cSource);
			DaoManager.registerDao(cSource, duplicateDAO);
		}catch(SQLException e){
			logger.severe("Unable to create DAO: " + e.getMessage());
		}
	}
	
	static{
		init();
	}

	/**
	 * Initialize the class, preparing the statements needed for the methods.
	 */
	private static void init(){
		generateStatements();
		
		addPrepStmt("pending"			, "SELECT count(*) FROM filter WHERE status = 1");
		addPrepStmt("isDnw"				, "SELECT * FROM `dnw` WHERE `id` = ?");
		addPrepStmt("prune"				, "DELETE FROM `cache` WHERE `timestamp` < ?");
		addPrepStmt("isIndexedPath"		, "SELECT i.dir, i.filename FROM `fileindex` AS i JOIN dirlist ON dirlist.id = i.dir JOIN filelist ON filelist.id = i.filename JOIN location_tags ON i.location = location_tags.tag_id WHERE location_tags.location = ? AND dirlist.dirpath = ? AND filelist.filename = ?");
		addPrepStmt("isBlacklisted"		, "SELECT * FROM `block` WHERE `id` = ?");
		addPrepStmt("getSetting"		, "SELECT param	FROM settings WHERE name = ?");
		addPrepStmt("getPath"			, "SELECT  CONCAT(dirlist.dirpath,filelist.filename) FROM `fileindex` as a JOIN filelist ON a.filename = filelist.id JOIN dirlist ON a.dir = dirlist.id WHERE  a.id = ?");
		addPrepStmt("hlUpdateBlock"		, "INSERT IGNORE INTO block (id) VALUES (?)");
		addPrepStmt("hlUpdateDnw"		, "INSERT IGNORE INTO dnw (id) VALUES (?)");
		addPrepStmt("addFilter"			, "INSERT IGNORE INTO filter (id, board, reason, status) VALUES (?,?,?,?)");
		addPrepStmt("updateFilter"		, "UPDATE filter SET status = ? WHERE id = ?");
		addPrepStmt("filterState"		, "SELECT status FROM filter WHERE  id = ?");
		addPrepStmt("pendingFilter"		, "SELECT board, reason, id FROM filter WHERE status = 1 ORDER BY board, reason ASC");
		addPrepStmt("filterTime"		, "UPDATE filter SET timestamp = ? WHERE id = ?");
		addPrepStmt("oldestFilter"		, "SELECT id FROM filter ORDER BY timestamp ASC LIMIT 1");
		addPrepStmt("compareBlacklisted", "SELECT a.id, CONCAT(dirlist.dirpath,filelist.filename) FROM (select fileindex.id,dir, filename FROM block join fileindex on block.id = fileindex.id) AS a JOIN filelist ON a.filename=filelist.id Join dirlist ON a.dir=dirlist.id");
		addPrepStmt("deleteIndexViaPath", "DELETE fi FROM fileindex AS fi JOIN dirlist AS dl ON fi.dir=dl.id JOIN filelist AS fl ON fi.filename=fl.id  WHERE dl.dirpath = ? AND fl.filename = ?");
		addPrepStmt("deleteDuplicateViaPath", "DELETE fi FROM fileduplicate AS fi JOIN dirlist AS dl ON fi.dir=dl.id JOIN filelist AS fl ON fi.filename=fl.id  WHERE dl.dirpath = ? AND fl.filename = ?");
		addPrepStmt("getDuplicates"		, "SELECT dv.id, dv.dupeloc, dv.dupePath  FROM dupeview AS dv UNION SELECT dv.id, dv.origloc, dv.origPath  FROM dupeview AS dv");
	}
	
	private static void generateStatements(){
		for(AidTables table : AidTables.values()){
			addPrepStmt("size"+table.toString(), "SELECT count(*) FROM "+table.toString());
			addPrepStmt("delete"+table.toString(), "DELETE FROM " +table.toString() + " WHERE id = ?");
		}
	}

	protected Connection getConnection(){
		try {
			return connPool.getConnection();
		} catch (SQLException e) {
			logger.warning("Failed to get database connection");
		}
		
		return null;
	}

	protected static void addPrepStmt(String id, String stmt){
		try {
			if(prepStmts.containsKey(id))
				throw new IllegalArgumentException("Key "+"'"+id+"'"+" is already present");
			prepStmts.put(id, stmt);
		} catch (NullPointerException npe){
			logger.severe("Prepared Statement could not be created, invalid connection");
		} catch (IllegalArgumentException iae){
			logger.severe("Prepared Statement could not be created, "+iae.getMessage());
		}
	}

	public boolean batchExecute(String[] statements){
		Connection cn = getConnection();
		Statement req = null;
		
		try {
			for(String sql : statements){
				req = cn.createStatement();
				req.execute(sql);
				req.close();
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
			return false;
		} finally {
			silentClose(cn, null, null);
		}
		
		return true;
	}
	
	public LinkedList<String> getBlacklistedFiles(){
		LinkedList<String> images = new LinkedList<>();
		String command = "compareBlacklisted";
		
		ResultSet rs = null;
		PreparedStatement ps = getPrepStmt(command);

		try {
			rs = ps.executeQuery();

			while(rs.next()){
				images.add(rs.getString(1));
			}

			return images;

		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		}finally{
			closeAll(ps);
			silentClose(null, ps, rs);
		}
		return null;
	}
	/**
	 * Use {@link AidDAO#getDuplicatesAndOriginal()} instead.
	 */
	@Deprecated
	public LinkedList<String[]> getDuplicates(){
		LinkedList<String[]> paths = new LinkedList<>();
		String command = "getDuplicates";
		final int NUM_OF_COLS = 3;
		
		ResultSet rs = null;
		PreparedStatement ps = getPrepStmt(command);

		try {
			rs = ps.executeQuery();

			while(rs.next()){
				String[] dupe = new String[NUM_OF_COLS];
				
				for(int i=0; i < NUM_OF_COLS; i++){
					dupe[i] = rs.getString(i+1);
				}
				
				paths.add(dupe);
			}

		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		}finally{
			closeAll(ps);
			silentClose(null, ps, rs);
		}
		return paths;
	}
	
	public LinkedList<String[]> getDuplicatesAndOriginal() {
		return getDuplicates();
	}

	protected PreparedStatement getPrepStmt(String command){
		if(prepStmts.containsKey(command)){
			Connection cn = getConnection();
			
			PreparedStatement prepStmt = null;
			try {
				prepStmt = cn.prepareStatement(prepStmts.get(command));
			} catch (SQLException e) {
				logger.warning("Failed to create prepared statement for command \""+command+"\"");
			}
			return prepStmt;
		}else{
			logger.warning("Prepared statment command \""+command+"\" not found.\nHas this object been initialized?");
			return null;
		}
	}
	
	protected void silentClose(Connection cn, PreparedStatement ps, ResultSet rs){
		if(rs != null)
			try{rs.close();}catch(SQLException e){}
		if(ps != null)
			try{ps.close();}catch(SQLException e){}
		if(cn != null)
			try{cn.close();}catch(SQLException e){}
	}
	
	protected void closeAll(PreparedStatement ps){
		Connection cn = null;
		ResultSet rs = null;
		
		if(ps == null)
			return;
		
		try{cn = ps.getConnection();}catch(SQLException e){}
		try{rs = ps.getResultSet();}catch(SQLException e){}
		
		if(rs != null)
			try{rs.close();}catch(SQLException e){}
		if(ps != null)
			try{ps.close();}catch(SQLException e){}
		if(cn != null)
			try{cn.close();}catch(SQLException e){}
	}

	/**
	 * Add the current URL to the cache, or update it's Timestamp if it
	 * already exists. Method will return true if the URL is already present,
	 * otherwise false.
	 * @param url URL to be added
	 * @return true if URL is already present else false.
	 * Retruns true on error.
	 */
	public boolean addCache(URL url) {
		try {

			String id = url.toString();
			Cache cacheRecord = new Cache(id);

			if (cacheDAO.idExists(id)) {
				return false;
			} else {
				cacheDAO.create(cacheRecord);
				return true;
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR + e.getMessage());
		}

		return false;
	}

	public void addThumb(String url,String filename, byte[] data){
		Thumbnail thumb = new Thumbnail(url, filename, data);
		
		try {
			ThumbnailDAO.create(thumb);
		} catch (SQLException e) {
			logSQLerror(e);
		}
	}
	
	public boolean addIndex(FileInfo fileInfo, String location){
		try {
			LocationRecord locationRec = locationDao.queryForLocation(location);
			IndexRecord index = new IndexRecord(fileInfo, locationRec);
			
			if(indexDao.idExists(fileInfo.getHash())) {
				return false;
			}
			
			resolvePathIDs(index);
			
			int rowsChanged = indexDao.create(index);
			
			if(rowsChanged == 1){
				return true;
			}
			
		} catch (SQLException e) {
			logSQLerror(e);
		}
		
		return false;
	}
	
	private void resolvePathIDs(FileRecord record) throws SQLException {
		DirectoryPathRecord directory = record.getDirectory();
		FilePathRecord file = record.getFile();
		
		List<DirectoryPathRecord> directories = directoryDAO.queryForMatchingArgs(directory);
		List<FilePathRecord> files = fileDAO.queryForMatchingArgs(file);
		
		if(directories.isEmpty()){
			directoryDAO.createOrUpdate(directory);
		}else{
			directory = directories.get(0);
		}
		
		if(files.isEmpty()) {
			fileDAO.createOrUpdate(file);
		}else{
			file = files.get(0);
		}
		
		record.setDirectory(directory);
		record.setFile(file);
	}
	
	public boolean addIndex(String hash, String path, long size, String location){
		FileInfo info = new FileInfo(Paths.get(path), hash);
		info.setSize(size);
		
		return addIndex(info, location);
	}
	
	public boolean addDuplicate(String hash, String path, long size, String location){
		FileInfo info = new FileInfo(Paths.get(path), hash);
		info.setSize(size);
		try {
			LocationRecord locationRec = locationDao.queryForLocation(location);
			DuplicateRecord index = new DuplicateRecord(info, locationRec);
		
			resolvePathIDs(index);
			
			int rowsChanged = duplicateDAO.create(index);
			
			if(rowsChanged == 1){
				return true;
			}
			
		} catch (SQLException e) {
			logSQLerror(e);
		}
		
		return false;
	}

	/**
	 * Get the number of pending filter items.
	 * @return Number of pending items.
	 */
	public int getPending(){
		return simpleIntQuery("pending");
	}

	public ArrayList<Image> getThumb(String url){
		LinkedList<Thumbnail> thumbs;
		ArrayList<Image> images = new ArrayList<>(1);
		
		try {
			thumbs = new LinkedList<>(ThumbnailDAO.queryForEq("url", url));
			images = new ArrayList<>(thumbs.size());
			
			for(Thumbnail thumb : thumbs){
				InputStream	is = new ByteArrayInputStream(thumb.getThumb());
				images.add(ImageIO.read(is));
				is.close();
			}
		} catch (SQLException e) {
			logSQLerror(e);
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
		
		return images;
	}

	public int size(AidTables table){
		return simpleIntQuery("size"+table.toString());
	}

	/**
	 * Check the ID against the cache.
	 * @param uniqueID ID to check
	 * @return true if the ID is present otherwise false.
	 * Returns true on errors.
	 */
	public boolean isCached(URL url){
		return isCached(url.toString());
	}

	/**
	 * Check the ID against the cache.
	 * @param uniqueID ID to check
	 * @return true if the ID is present otherwise false.
	 * Returns true on errors.
	 */
	public boolean isCached(String uniqueID){
		try {
			return cacheDAO.idExists(uniqueID);
		} catch (SQLException e) {
			logSQLerror(e);
			return true;
		}
	}
	
	private void logSQLerror(SQLException e) {
		logSQLerror(e, "");
		e.printStackTrace();
	}
	
	private void logSQLerror(SQLException e, String command) {
		logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
	}

	public boolean isDnw(String hash){
		return simpleBooleanQuery("isDnw", hash, true);
	}

	public boolean isHashed(String hash){
		try {
			return indexDao.idExists(hash);
		} catch (SQLException e) {
			logSQLerror(e);
		}
		
		return true;
	}

	public boolean isBlacklisted(String hash){
		return simpleBooleanQuery("isBlacklisted", hash, false);
	}
	
	public boolean isValidTag(String tag) {
		if (getTagId(tag) == -1) {
			return false;
		} else {
			return true;
		}
	}

	public int getTagId(String tag) {
		try {
			LocationRecord locRec = locationDao.queryForLocation(tag);

			if (locRec != null) {
				return locRec.getTag_id();
			}
		} catch (SQLException e) {
			logSQLerror(e);
		}
		return -1;
	}
	
	public void update(String id, AidTables table){
		PreparedStatement update = null;
		String command = null;
		
		if(table.equals(AidTables.Block)){
			command = "hlUpdateBlock";
		}else if (table.equals(AidTables.Dnw)){
			command = "hlUpdateDnw";
		}else{
			logger.severe("Unhandled enum Table: "+table.toString());
			return;
		}

		try{
			update = getPrepStmt(command);
			update.setString(1, id);
			update.executeUpdate();
		}catch (SQLException e){
			logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
		}finally{
			closeAll(update);
		}
	}
	
	
	//TODO add isIndexedPath(Path fullPath)
	
	public boolean isIndexedPath(Path fullpath, String locationTag){
		Path relPath = removeDriveLetter(fullpath);
		String filename = relPath.getFileName().toString().toLowerCase();
		Path parent = relPath.getParent();
		String dir;
		
		dir = convertDirPathToString(parent);
		
		if(dir == null){
			logger.warning("IndexPathLookup for null parent: " + parent + filename);
			return false;
		}
		
		ResultSet rs = null;
		final String command = "isIndexedPath";
		
		PreparedStatement ps = getPrepStmt(command);
		
		try {
			ps.setString(1, locationTag);
			ps.setString(2, dir);
			ps.setString(3, filename);
			
			rs = ps.executeQuery();
			boolean b = rs.next();
			return b;
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
		} finally{
			closeAll(ps);
		}
		
		return false;
	}
	
	public int deleteIndexByPath(String fullpath){
		return deleteIndexByPath(removeDriveLetter(Paths.get(fullpath.toLowerCase())));
	}
	
	public int deleteIndexByPath(Path path) {
		final String command = "deleteIndexViaPath";
		int affectedRows = -1;
		path = removeDriveLetter(path);
		String dir = convertDirPathToString(path.getParent());
		
		if(dir == null){
			return affectedRows;
		}
		
		String filename = path.getFileName().toString();
		
		PreparedStatement ps = getPrepStmt(command);
		try {
			ps.setString(1, dir);
			ps.setString(2, filename);
			affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			logSQLerror(e);
		}
		
		return affectedRows;
	}
	
	public int deleteDuplicateByPath(String fullpath){
		return deleteDuplicateByPath(removeDriveLetter(Paths.get(fullpath.toLowerCase())));
	}
	
	public int deleteDuplicateByPath(Path path){
		final String command = "deleteDuplicateViaPath";
		int affectedRows = -1;
		path = removeDriveLetter(path);
		String dir = convertDirPathToString(path.getParent());
		
		if(dir == null){
			return affectedRows;
		}
		
		String filename = path.getFileName().toString();
		
		PreparedStatement ps = getPrepStmt(command);
		try {
			ps.setString(1, dir);
			ps.setString(2, filename);
			affectedRows = ps.executeUpdate();
		} catch (SQLException e) {
			logger.severe(SQL_OP_ERR+command+": "+e.getMessage());
		} finally{
			closeAll(ps);
		}
		
		return affectedRows;
	}
	
	private boolean simpleBooleanQuery(String command, String key, Boolean defaultReturn){
		ResultSet rs = null;
		PreparedStatement ps = getPrepStmt(command);
		try {
			ps.setString(1, key);
			rs = ps.executeQuery();
			boolean b = rs.next();
			return b;
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
		} finally{
			closeAll(ps);
		}
		
		return defaultReturn;
	}
	
	private int simpleIntQuery(String command){
		ResultSet rs = null;
		PreparedStatement ps = getPrepStmt(command);
		int intValue = -1;
		
		if(ps == null){
			logger.warning("Could not carry out query for command \""+command+"\"");
			return -1;
		}
		
		try {
			rs = ps.executeQuery();

			if(rs.next()){
				intValue = rs.getInt(1);
				return intValue;
			}else{
				return intValue;
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
		} finally{
			closeAll(ps);
		}
		
		return intValue;
	}
	
	private int simpleIntQuery(String command, String key, int defaultReturn){
		ResultSet rs = null;
		PreparedStatement ps = getPrepStmt(command);
		
		if(ps == null){
			logger.warning("Could not carry out query for command \""+command+"\"");
			return defaultReturn;
		}
		
		try {
			ps.setString(1, key);
			rs = ps.executeQuery();

			if(rs.next()){
				int intValue = rs.getInt(1);
				return intValue;
			}else{
				return defaultReturn;
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+command+": "+e.getMessage());
		} finally{
			closeAll(ps);
		}
		
		return defaultReturn;
	}
	
	public void pruneCache(long maxAge){
		PreparedStatement ps = getPrepStmt("prune");

		try {
			ps.setTimestamp(1,new Timestamp(maxAge));
			ps.executeUpdate();
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} finally {
			closeAll(ps);
		}
	}

	public int delete(AidTables table, String id){
		PreparedStatement ps = getPrepStmt("delete"+table.toString());
		if(ps == null){
			logger.warning("Could not delete entry "+ id +" for table "+table.toString());
			return -1;
		}
		
		try {
			ps.setString(1, id);
			return ps.executeUpdate();
		} catch (Exception e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} finally {
			closeAll(ps);
		}
		
		return -1;
	}

	public void sendStatement(String sqlStatment){
		Connection cn = getConnection();
		Statement req = null;
		try {
			req = cn.createStatement();
			req.execute(sqlStatment);
			req.close();
		} catch (SQLException e) {
			logger.warning("Failed to execute statement id: "+sqlStatment+"\n"+e.getMessage());
		} finally {
			if(req != null)
				try{req.close();} catch (SQLException e){}

			silentClose(cn, null, null);
		}
	}

	public String getSetting(DBsettings settingName){
		String command = "getSetting";
		ResultSet rs = null;
		PreparedStatement ps = getPrepStmt(command);
		
		try {
			ps.setString(1, settingName.toString());
			rs = ps.executeQuery();

			rs.next();
			String string = rs.getString(1);
			return string;
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} finally{
			closeAll(ps);
			silentClose(null, ps, rs);
		}
		
		return null;
	}
	
	/**
	 * Get path associated with the given hash value.
	 * 
	 * @param hash hash value to lookup
	 * @return path as a String or null if not found
	 */
	public String getPath(String hash){
		try {
			IndexRecord index = indexDao.queryForId(hash);
			return index.getRelativePath().toString();
		} catch (SQLException e) {
			logSQLerror(e);
		}
		return null;
	}
	
	public ArrayList<Path> getLocationPathList(String locationTag) {
		ArrayList<Path> pathList = new ArrayList<>();
		
		try {
			List<LocationRecord> locRec = locationDao.queryForEq("location", locationTag);
			
			if(! locRec.isEmpty()){
				IndexRecord index = new IndexRecord();
				index.setLocation(locRec.get(0));
				List<IndexRecord> results = indexDao.queryForMatchingArgs(index);
				
				for(IndexRecord result : results){
					pathList.add(result.getRelativePath());
				}
			}
		} catch (SQLException e) {
			logSQLerror(e);
		}
		
		return pathList;
	}
	
	public ArrayList<String> getLocationFilelist(String locationTag) {
		ArrayList<Path> pathList = getLocationPathList(locationTag);
		ArrayList<String> pathStrings = new ArrayList<>(pathList.size());

		for (Path path : pathList) {
			pathStrings.add(path.toString());
		}

		return pathStrings;
	}
	
	public int getLocationIndexSize(String locationTag) {
		try {
			LocationRecord locRec = locationDao.queryForLocation(locationTag);

			if (locRec == null) {
				return -1;
			}
			SelectArg selectLoc = new SelectArg();
			selectLoc.setValue(locRec.getTag_id());
			PreparedQuery<IndexRecord> indexPrep = indexDao.queryBuilder()
					.setCountOf(true).where().eq("location", selectLoc)
					.prepare();
			return (int) indexDao.countOf(indexPrep);
		} catch (SQLException e) {
			logSQLerror(e);
		}

		return -1;
	}
	
	public String getLocationById(String id) {
		try {
			IndexRecord index = indexDao.queryForId(id);
			if (index == null) {
				return DEFAULT_LOCATION;
			} else {
				return index.getLocation();
			}
		} catch (SQLException e) {
			logSQLerror(e);
		}
		return DEFAULT_LOCATION;
	}
	
	public boolean moveIndexToDuplicate(String id){
		final String SQL_COPY_INDEX_STATEMENT = "INSERT INTO fileduplicate SELECT * FROM fileindex WHERE id = ?";
		final String SQL_DELETE_INDEX_STATEMENT = "DELETE FROM fileindex WHERE id = ?";
		
		Connection cn = null;
		PreparedStatement copyPrepStatement = null, deletePrepStatement = null;
		
		try {
			cn = getConnection();
			
			cn.setAutoCommit(false);
			
			copyPrepStatement = cn.prepareStatement(SQL_COPY_INDEX_STATEMENT);
			deletePrepStatement = cn.prepareStatement(SQL_DELETE_INDEX_STATEMENT);
			
			copyPrepStatement.setString(1, id);
			deletePrepStatement.setString(1, id);
			
			copyPrepStatement.executeUpdate();
			deletePrepStatement.executeUpdate();
			
			
			
			return true;
		} catch (SQLException e) {
			try {
				cn.rollback();
			} catch (SQLException e1) {logger.severe("Failed to perform transaction rollback");}
			
			return false;
		}finally{
			
			try {
				cn.commit();
				cn.setAutoCommit(true);
			} catch (SQLException e) {
				logger.severe("Failed to commit transaction: " + e.getMessage());
			}
			
			closeAll(copyPrepStatement);
			closeAll(deletePrepStatement);
		}
	}
	
	public boolean moveDuplicateToIndex(String id){
		final String SQL_COPY_INDEX_STATEMENT = "INSERT INTO fileindex SELECT * FROM fileduplicate WHERE id = ? LIMIT 1" ;
		final String SQL_DELETE_DUPLICATE_STATEMENT = "DELETE fd FROM fileduplicate AS fd JOIN fileindex AS fi ON fi.id=fd.id AND fi.dir=fd.dir AND fi.filename=fd.filename";
		
		Connection cn = null;
		PreparedStatement copyPrepStatement = null, deletePrepStatement = null;
		
		try {
			cn = getConnection();
			cn.setAutoCommit(false);
			
			copyPrepStatement = cn.prepareStatement(SQL_COPY_INDEX_STATEMENT);
			deletePrepStatement = cn.prepareStatement(SQL_DELETE_DUPLICATE_STATEMENT);
			
			copyPrepStatement.setString(1, id);
			
			copyPrepStatement.executeUpdate();
			deletePrepStatement.executeUpdate();
			
			return true;
		} catch (SQLException e) {
			try {
				cn.rollback();
			} catch (SQLException e1) {logger.severe("Failed to perform transaction rollback");}
			
			return false;
		}finally{
			
			try {
				cn.commit();
				cn.setAutoCommit(true);
			} catch (SQLException e) {
				logger.severe("Failed to commit transaction: " + e.getMessage());
			}
			
			closeAll(copyPrepStatement);
			closeAll(deletePrepStatement);
		}
	}
	
	public boolean addFilter(FilterItem fi) {
		return addFilter(fi.getUrl().toString(), fi.getBoard(), fi.getReason(), fi.getState());
	}

	/**
	 * Adds a filter item to the database.
	 * @param id id of the item
	 * @param board board alias
	 * @param reason reason for adding the filter
	 * @param state initial state of the filter
	 * @return true if the filter was added, else false
	 */
	public boolean addFilter(String id, String board, String reason, FilterState state) {
		PreparedStatement addFilter =getPrepStmt("addFilter");
		try {
			addFilter.setString(1, id);
			addFilter.setString(2, board);
			addFilter.setString(3, reason);
			addFilter.setShort(4, (short) state.ordinal());
			int res = addFilter.executeUpdate();
	
			if(res == 0){
				logger.warning("filter already exists!");
				return false;
			}else{
				return true;
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} finally {
			closeAll(addFilter);
		}
	
		return false;
	}

	public void updateState(String id, FilterState state) {
		PreparedStatement updateFilter = getPrepStmt("updateFilter");
		try {
			updateFilter.setShort(1, (short)state.ordinal());
			updateFilter.setString(2, id);
			updateFilter.executeUpdate();
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} finally {
			closeAll(updateFilter);
		}
	}

	public FilterState getFilterState(String id) {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = getPrepStmt("filterState");
			ps.setString(1, id);
			rs = ps.executeQuery();
			if(rs.next()){
				FilterState fs = FilterState.values()[(int)rs.getShort(1)];
				return fs; 
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		}finally{
			closeAll(ps);
		}
		return FilterState.UNKNOWN;
	}

	/**
	 * Returns all items in the filter with state set to pending (1).
	 * @return a list of all pending filter items
	 */
	public LinkedList<FilterItem> getPendingFilters() {
		PreparedStatement pendingFilter = getPrepStmt("pendingFilter");
		ResultSet rs = null;
	
		try {
			rs = pendingFilter.executeQuery();
			LinkedList<FilterItem> result = new LinkedList<FilterItem>();
			while(rs.next()){
				URL url;
				url = new URL(rs.getString("id"));
	
				result.add(new FilterItem(url, rs.getString("board"), rs.getString("reason"),  FilterState.PENDING));
			}
			
			return result;
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} catch (MalformedURLException e) {
			logger.warning("Unable to create URL "+e.getMessage());
		}finally{
			closeAll(pendingFilter);
		}
		return new LinkedList<FilterItem>();
	}

	public void updateFilterTimestamp(String id) {
		PreparedStatement updateTimestamp = getPrepStmt("filterTime");
		try {
			updateTimestamp.setTimestamp(1, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			updateTimestamp.setString(2, id);
			updateTimestamp.executeUpdate();
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getMessage());
		} finally {
			closeAll(updateTimestamp);
		}
	}

	public String getOldestFilter() {
		ResultSet rs = null;
		PreparedStatement getOldest = getPrepStmt("oldestFilter");
	
		try {
			rs = getOldest.executeQuery();
			if(rs.next()){
				String s = rs.getString(1);
				return s;
			}else {
				return null;
			}
		} catch (SQLException e) {
			logger.warning(SQL_OP_ERR+e.getLocalizedMessage());
		}finally{
			closeAll(getOldest);
		}
		return null;
	}
}
