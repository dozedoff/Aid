package testDummy;

import io.ImageLoader;

import java.io.File;

public class DummyImageLoader extends ImageLoader {

	public DummyImageLoader() {
		super(new DummyFileWriter(), new DummyFilter(), new File("D:\test"), 1, 1);
	}

}
