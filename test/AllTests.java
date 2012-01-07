import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import app.SettingValidatorTest;


@RunWith(Suite.class)
@SuiteClasses({ 	SettingValidatorTest.class,
					Board.class, 
					Filter.class, 
					Io.class
})
public class AllTests {

}
