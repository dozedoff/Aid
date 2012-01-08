import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({ 	App.class,
					Board.class, 
					Filter.class, 
					Io.class
})
public class AllTests {

}
