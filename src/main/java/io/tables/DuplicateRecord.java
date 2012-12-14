/*  Copyright (C) 2012  Nicholas Wright
	
	part of 'AidUtil', a collection of maintenance tools for 'Aid'.

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

import com.github.dozedoff.commonj.file.FileInfo;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="fileduplicate")
public class DuplicateRecord extends FileRecord {
	
public DuplicateRecord() {}

public DuplicateRecord(FileInfo info, LocationRecord location) {
	super(info, location);
}


}
