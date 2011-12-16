package testDummy;

import io.ConnectionPoolaid;
import io.MySQLaid;
import io.ResourceCreationException;

import java.util.Properties;


public class DummyConnectionPool extends ConnectionPoolaid{
	boolean block = false, filter = false;
	
	public DummyConnectionPool() {
		super(new Properties(), 10); // default values
	}
	
	
	
	public DummyConnectionPool(boolean block, boolean filter) {
		super(new Properties(), 10); // default values
		this.block = block;
		this.filter = filter;
	}



	@Override
	public MySQLaid getResource() throws InterruptedException,ResourceCreationException {
		return new DummySqlAid(block,filter);
	}
	
	@Override
	public MySQLaid getResource(long maxWaitMillis) throws InterruptedException,	ResourceCreationException {
		return new DummySqlAid(block,filter);
	}

	@Override
	public void returnResource(MySQLaid res) {

	}
}