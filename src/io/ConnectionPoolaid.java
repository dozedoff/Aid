/*  Copyright (C) 2011  Nicholas Wright

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

import java.util.Properties;
import java.util.logging.Logger;

/**
 * Connection pool for database connections.
 */
public class ConnectionPoolaid extends ResourcePool<MySQLaid> {
	private Properties dbProps;
	private final int DEF_WAIT_TIME = 5000;

	static final Logger logger = Logger.getLogger(ConnectionPoolaid.class.toString());
	
	public ConnectionPoolaid(Properties dbProps,int maxResources) {
		super(maxResources);
		this.dbProps = dbProps;
	}

	@Override
	protected MySQLaid createResource() {
		MySQLaid sql = new MySQLaid(dbProps);
		sql.init();
		return sql;
	}
	
	@Override
	public MySQLaid processBeforReturn(MySQLaid res){
		if(res == null || !res.isValid())
			return createResource();
		else
			return res;
	}
	
	public void closeAll(){
		logger.info("Closing all database connections...")
		;
		for(MySQLaid m : resources)
			m.disconnect();
		
		logger.info("All database connections closed.");
	}
	
	/**
	 * Gets a database connection using default values.
	 * 
	 * @return a database connection
	 * @throws InterruptedException
	 * @throws ResourceCreationException
	 */
	public MySQLaid getResource() throws InterruptedException, ResourceCreationException {
		return getResource(DEF_WAIT_TIME);
	}
}
