package testDummy;

import java.awt.Image;
import java.util.ArrayList;

import board.Post;
import io.ThumbnailLoader;

public class DummyThumbnailLoader extends ThumbnailLoader {
	public DummyThumbnailLoader(){
		super(new DummyConnectionPool());
	}
	
	@Override
	public void downloadThumbs(String url, ArrayList<Post> postList) {
		
	}
	
	@Override
	public ArrayList<Image> getThumbs(String id) {
		return new ArrayList<Image>();
	}
}
