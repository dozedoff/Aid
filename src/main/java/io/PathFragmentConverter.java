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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.github.dozedoff.commonj.file.FileUtil;

public class PathFragmentConverter {
	private static final String NULL_ERR_MSG = "Null is not a valid parameter";
	private static final String[] SPLITTABLE_EXTENSIONS = { ".jpg", ".png",
			".gif" };

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

		if (!fragments.isEmpty()) {
			splittExtension(fragments);
		}

		return fragments;
	}

	private static void splittExtension(LinkedList<String> fragments) {
		String lastFragment = fragments.getLast();
		int lastDot = lastFragment.lastIndexOf(".");

		if (lastDot != -1) {
			String extension = lastFragment.substring(lastDot);
			if (validExtension(extension)) {
				int fragmentsSize = fragments.size();
				fragments.remove(fragmentsSize - 1);

				String newFragment = lastFragment.substring(0, lastDot);

				fragments.add(newFragment);
				fragments.add(extension);
			}
		}
	}

	private static boolean validExtension(String extension) {
		boolean matches = false;

		for (String validExtension : SPLITTABLE_EXTENSIONS) {
			matches = extension.equals(validExtension);

			if (matches) {
				break;
			}
		}

		return matches;
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
		String[] processedFragments = fragments;

		if (fragments.length > 1) {
			processedFragments = mergeExtension(fragments);
		}

		fragmentPath = Paths.get("", processedFragments);
		return fragmentPath;
	}

	private static String[] mergeExtension(String[] fragments) {
		String lastFragment = fragments[fragments.length - 1];

		if (!validExtension(lastFragment)) {
			return fragments;
		}

		String[] mergedFragments = new String[fragments.length - 1];

		StringBuilder mergedFragment = new StringBuilder();
		mergedFragment.append(fragments[fragments.length - 2]);
		mergedFragment.append(fragments[fragments.length - 1]);

		mergedFragments = Arrays
				.copyOfRange(fragments, 0, fragments.length - 1);
		mergedFragments[mergedFragments.length - 1] = mergedFragment.toString();

		return mergedFragments;
	}

	public static Path toPath(List<String> fragments)
			throws IllegalArgumentException {

		checkNull(fragments);

		String[] fragmentArray = new String[fragments.size()];
		fragmentArray = fragments.toArray(fragmentArray);
		return toPath(fragmentArray);
	}
}
