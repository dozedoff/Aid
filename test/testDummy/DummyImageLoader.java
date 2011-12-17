package testDummy;

import io.FileLoader;

import java.io.File;

public class DummyImageLoader extends FileLoader {

	public DummyImageLoader() {
		super(new DummyFileWriter(), new DummyFilter(), new File("D:\test"), 1);
	}

}
