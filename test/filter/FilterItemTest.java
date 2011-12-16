package filter;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

public class FilterItemTest {
	FilterItem filterItem;
	@Before
	public void setUp() throws Exception {
		URL url = new URL("http://foo.bar/test/12345");
		filterItem = new FilterItem("foo", "bar", url, FilterState.PENDING);
	}

	@Test
	public void testToString() {
		String format = "%1$-5s %2$-10s %3$-30s";
		assertEquals(String.format(format, "foo", "12345", "bar"), filterItem.toString());
	}

}
