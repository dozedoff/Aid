package testDummy;

import filter.Filter;
import gui.BlockListDataModel;

public class DummyFilter extends Filter {

	public DummyFilter() {
		super(new DummyConnectionPool(), new BlockListDataModel(), new DummyThumbnailLoader());
	}

}
