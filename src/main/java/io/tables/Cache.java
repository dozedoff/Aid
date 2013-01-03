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
package io.tables;

import io.dao.CacheDAO;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(daoClass=CacheDAO.class)
public class Cache {
	@DatabaseField(id=true, canBeNull=false)
	private String id;
	@DatabaseField
	private Date timestamp;
	@DatabaseField(canBeNull=false)
	private int last_mod_id;
	@DatabaseField(canBeNull=false)
	boolean downloaded = true;
	
	public Cache() {}
	
	/**
	 * Create a new cache entry.<br>
	 * <strong>Note:</strong> For compatibility the download flag will be <strong>set to true</strong>.
	 * @param id
	 */
	public Cache(String id) {
		this.id = id;
	}
	
	public Cache(String id, boolean downloaded) {
		this.id = id;
		this.downloaded = downloaded;
	}

	public String getId() {
		return id;
	}

	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public int getLast_mod_id() {
		return last_mod_id;
	}

	public void setLast_mod_id(int last_mod_id) {
		this.last_mod_id = last_mod_id;
	}

	public boolean isDownloaded() {
		return downloaded;
	}

	public void setDownloaded(boolean downloaded) {
		this.downloaded = downloaded;
	}
}
