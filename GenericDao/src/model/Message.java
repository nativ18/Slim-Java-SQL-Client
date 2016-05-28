package model;

import engine.Deserializer;
import engine.SqlBinder;

/**
 * An example class for creating a annotation based entity that generic Dao can
 * manage.
 * 
 * @author nativ
 */
public class Message extends BaseEntity{

	@SqlBinder(val = "content")
	private String content;
	@SqlBinder(val = "message_type")
	private Integer messageType;
	@SqlBinder(val = "image_url")
	private String imageUrl;
	@SqlBinder(val = "likes")
	private Integer likes;
	@SqlBinder(val = "date_created")
	private Long creationDate;

	@Deserializer(isDeserializer = true)
	public Message(long id, String content, Integer messageType, String imageUrl, Integer likes, Long creationDate) {
		super(id);
		this.content = content;
		this.messageType = messageType;
		this.imageUrl = imageUrl;
		this.likes = likes;
		this.creationDate = creationDate;
	}

	public Message() {
	}

	public Long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public void setMessageType(Integer messageType) {
		this.messageType = messageType;
	}

	public void setUpvoteCount(Integer upvoteCount) {
		this.likes = upvoteCount;
	}

	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public int getUpvoteCount() {
		return likes;
	}

	public void setUpvoteCount(int upvoteCount) {
		this.likes = upvoteCount;
	}

}
