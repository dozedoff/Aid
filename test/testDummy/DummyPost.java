package testDummy;

import java.net.URL;

import board.Post;

public class DummyPost extends Post {
	public DummyPost(String imageName, URL imageUrl, String comment){
		hasImage = true;
		hasComment = true;
		
		this.imageName = imageName;
		this.imageUrl = imageUrl;
		this.comment = comment;
	}
	
	@Override
	public String getComment() {
		return super.getComment();
	}

	@Override
	public void processHtml(String html) {
		// no OP
	}

	@Override
	public String getImageName() {
		return super.getImageName();
	}

	@Override
	public URL getImageUrl() {
		return super.getImageUrl();
	}

	@Override
	public boolean hasComment() {
		return super.hasComment();
	}

	@Override
	public boolean hasImage() {
		return super.hasImage();
	}
	
	public void setImage(String imageName, URL imageUrl){
		hasImage = true;
		this.imageName = imageName;
		this.imageUrl = imageUrl;
	}
	
	public void setComment(String comment){
		hasComment = true;
		this.comment = comment;
	}
	
	public void clearImage(){
		hasImage = false;
		imageName = null;
		imageUrl = null;
	}
	
	public void clearComment(){
		hasComment = false;
		comment = null;
	}
	
}
