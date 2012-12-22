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

import static io.PathFragmentConverter.toFragments;
import static io.PathFragmentConverter.toPath;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class PathFragmentConverterTest {
	
	private Path getRoot() {
		Path root = null;
		
		Iterable<Path> rootDirs =  FileSystems.getDefault().getRootDirectories();
		
		for(Path dir : rootDirs){
			root = dir;
			break;
		}

		return root;
	}

	@Test
	public void testToFragmentsAbsolute() {
		Path root = getRoot();
		Path relative = Paths.get("foo", "bar", "cat.jpg");
		Path absolute = root.resolve(relative);
		
		List<String> fragments = toFragments(absolute);
		
		assertThat(fragments.get(0), is("foo"));
		assertThat(fragments.get(1), is("bar"));
		assertThat(fragments.get(2), is("cat"));
		assertThat(fragments.get(3), is(".jpg"));
		assertThat(fragments.size(), is(4));
	}
	
	@Test
	public void testToFragmentsRelative() {
		Path relative = Paths.get("foo", "bar", "cat.jpg");
		
		List<String> fragments = toFragments(relative);
		
		assertThat(fragments.get(0), is("foo"));
		assertThat(fragments.get(1), is("bar"));
		assertThat(fragments.get(2), is("cat"));
		assertThat(fragments.get(3), is(".jpg"));
		assertThat(fragments.size(), is(4));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testToFragmentsNull() {
		toFragments(null);
	}
	
	@Test
	public void testToFragmentsEmptyPath() {
		Path path = Paths.get("","");

		List<String> fragments = toFragments(path);
		
		assertThat(fragments.size(), is(0));
	}

	@Test
	public void testToPathStringArray() {
		String[] fragments = {"foo", "bar", "cat", ".jpg"};
		Path path = Paths.get("foo", "bar", "cat.jpg");
		
		Path fragmentPath = toPath(fragments);
		assertThat(fragmentPath, is(path));
	}
	
	@Test
	public void testToPathStringArrayEmptyArray() {
		String[] fragments = {};
		Path path = Paths.get("");
		
		Path fragmentPath = toPath(fragments);
		assertThat(fragmentPath, is(path));
	}
	
	@Test
	public void testToPathStringArrayEmptyString() {
		String[] fragments = {""};
		Path path = Paths.get("");
		
		Path fragmentPath = toPath(fragments);
		assertThat(fragmentPath, is(path));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testToPathStringArrayNull() {
		String[] fragments = null;
		
		toPath(fragments);
	}

	@Test
	public void testToPathListOfString() {
		LinkedList<String> fragmentList = new LinkedList<>();
		fragmentList.add("foo");
		fragmentList.add("bar");
		fragmentList.add("cat");
		fragmentList.add(".jpg");
		
		Path path = Paths.get("foo", "bar", "cat.jpg");
		
		Path fragmentPath = toPath(fragmentList);
		assertThat(fragmentPath, is(path));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testToPathListOfStringNull() {
		LinkedList<String> fragmentList = null;
		
		toPath(fragmentList);
	}

	@Test
	public void testToPathListOfStringEmptyList() {
		LinkedList<String> fragmentList = new LinkedList<>();
		
		Path path = Paths.get("");
		
		Path fragmentPath = toPath(fragmentList);
		assertThat(fragmentPath, is(path));
	}
}
