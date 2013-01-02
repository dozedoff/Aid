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

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;

public class LastModified {
	@DatabaseField(id=true, canBeNull=false)
	private String id;
	@DatabaseField(canBeNull=false)
	private Date lastmod;
	@DatabaseField(canBeNull=false)
	private Date lastvisit;
	
	public LastModified() {}

	public LastModified(String id, Date lastmod, Date lastvisit) {
		this.id = id;
		this.lastmod = lastmod;
		this.lastvisit = lastvisit;
	}

	public Date getLastmod() {
		return lastmod;
	}

	public void setLastmod(Date lastmod) {
		this.lastmod = lastmod;
	}

	public Date getLastvisit() {
		return lastvisit;
	}

	public void setLastvisit(Date lastvisit) {
		this.lastvisit = lastvisit;
	}

	public String getId() {
		return id;
	}
}
