package testDummy;

import java.io.File;

import io.ImageLoader;

public class DummyImageLoader extends ImageLoader {

	public DummyImageLoader() {
		super(new DummyFileWriter(), new DummyFilter(), new File("D:\test"), 1);
	}
}
