

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import board.*;

@RunWith(Suite.class)
@SuiteClasses({ 	BoardTest.class,
					PageFactoryTest.class,
					PageTest.class,
					PageThreadTest.class,
					PostTest.class
})
public class Board {

}
