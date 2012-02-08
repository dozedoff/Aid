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

public class DefaultMySQLconnection extends Properties {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Parameters for a connection to the local database (aid). 
	 */
	public DefaultMySQLconnection(){
		init();
	}
	
	/**
	 * Setup connection properties for a database.
	 * @param ip IP of the database
	 * @param port Port to use
	 * @param database Name of the database
	 */
	public DefaultMySQLconnection(String ip, int port, String database, String user, String password){
		init();
		
		this.setProperty("url", "jdbc:mysql://"+ip+":"+port+"/"+database);
		this.setProperty("user", user);
		this.setProperty("password", password);
	}
	
	/**
	 * Set up the default values.
	 */
	private void init(){
		this.setProperty("url", "jdbc:mysql://127.0.0.1:3306/aid");
		this.setProperty("user", "aid");
		this.setProperty("password", "dia");
		this.setProperty("CachePrepStmts", "true");
		this.setProperty("holdResultsOpenOverStatementClose", "true");
	}
}
