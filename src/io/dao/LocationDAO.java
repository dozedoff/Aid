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
package io.dao;

import io.tables.LocationRecord;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class LocationDAO extends BaseDaoImpl<LocationRecord, Integer> {
	public LocationDAO(ConnectionSource source) throws SQLException {
		super(source, LocationRecord.class);
	}
	
	public LocationRecord queryForLocation(String location) throws SQLException {
		List<LocationRecord> locations = queryForEq("location", location);
		
		if(locations.isEmpty()){
			return new LocationRecord();
		}else{
			return locations.get(0);
		}
	}
}
