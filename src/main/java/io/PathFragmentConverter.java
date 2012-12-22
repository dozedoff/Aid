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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github.dozedoff.commonj.file.FileUtil;

public class PathFragmentConverter {
	private static final String NULL_ERR_MSG = "Null is not a valid parameter";

	private static void checkNull(Object obj) throws IllegalArgumentException {
		if (obj == null) {
			throw new IllegalArgumentException(NULL_ERR_MSG);
		}
	}

	public static List<String> toFragments(Path path)
			throws IllegalArgumentException {

		LinkedList<String> fragments = new LinkedList<>();

		checkNull(path);

		if (path.isAbsolute()) {
			FileUtil.removeDriveLetter(path);
		}

		Iterator<Path> ite = path.iterator();

		while (ite.hasNext()) {
			Path next = ite.next();
			String fragment = next.toString();
			fragments.add(fragment);
		}

		if (fragments.size() == 1) {
			removeEmptyFragment(fragments);
		}

		return fragments;
	}

	private static void removeEmptyFragment(LinkedList<String> fragments) {
		String entry = fragments.get(0);
		if (entry.equals("")) {
			fragments.remove(0);
		}
	}

	public static Path toPath(String... fragments)
			throws IllegalArgumentException {

		Path fragmentPath;

		checkNull(fragments);

		fragmentPath = Paths.get("", fragments);
		return fragmentPath;
	}

	public static Path toPath(List<String> fragments)
			throws IllegalArgumentException {

		checkNull(fragments);

		String[] fragmentArray = new String[fragments.size()];
		fragmentArray = fragments.toArray(fragmentArray);
		return toPath(fragmentArray);
	}
}
