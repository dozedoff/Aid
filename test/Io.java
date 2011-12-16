

import io.*;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ FileWriterTest.class,
				ImageItemTest.class,
				MySqlTest.class,
				ImageLoaderTest.class,
				ThumbnailLoaderTest.class
})
public class Io {

}
