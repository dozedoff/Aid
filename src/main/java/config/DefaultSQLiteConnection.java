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
package config;

import java.util.Properties;

public class DefaultSQLiteConnection extends Properties {
	private static final long serialVersionUID = 1L;

	/**
	 * Parameters for a connection to the local database (aid).
	 */
	public DefaultSQLiteConnection() {
		init();
	}

	/**
	 * Setup connection properties for a database.
	 * 
	 * @param ip
	 *            IP of the database
	 * @param port
	 *            Port to use
	 * @param database
	 *            Name of the database
	 */
	public DefaultSQLiteConnection(String database) {
		init();

		this.setProperty("url", "jdbc:sqlite:" + database);
	}

	/**
	 * Set up the default values.
	 */
	private void init() {
		this.setProperty("url", "jdbc:sqlite:mmut.db");
	}
}
