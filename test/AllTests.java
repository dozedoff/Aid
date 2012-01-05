import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import app.MainTest;


@RunWith(Suite.class)
@SuiteClasses({ 	MainTest.class,
					Board.class, 
					Filter.class, 
					Io.class
})
public class AllTests {

}
