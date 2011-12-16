package testDummy;

import io.FileWriter;

public class DummyFileWriter extends FileWriter {

	public DummyFileWriter() {
		super(new DummyFilter());
	}

}
