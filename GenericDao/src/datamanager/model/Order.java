package datamanager.model;

import engine.Deserializer;
import engine.SqlBinder;

/**
 * An example class for creating a annotation based entity that
 * {@code GenericDao} can manage.
 *
 * @author Nativ Levy
 */
public class Order extends BaseEntity {

	@SqlBinder(val = "content")
	private String content;
	@SqlBinder(val = "owner_id")
	private long ownerId;
	@SqlBinder(val = "order_type")
	private int orderType;
	@SqlBinder(val = "image_url")
	private String imageUrl;
	@SqlBinder(val = "price")
	private int price;
	@SqlBinder(val = "creation_date")
	private long creationDate;

	// assumption: orderType and price are nullables
	@Deserializer
	public Order(long id, long ownerId, String content, Integer orderType, String imageUrl, Integer price,
			long creationDate) {
		super(id);
		this.ownerId = ownerId;
		this.content = content;
		this.orderType = orderType == null ? 0 : orderType;
		this.imageUrl = imageUrl;
		this.price = price == null ? 0 : price;
		this.creationDate = creationDate;
	}

	public Order() {
	}

	public long getOwnerId() {
		return ownerId;
	}

	public int getPrice() {
		return price;
	}

	public void setOwnerId(long ownerId) {
		this.ownerId = ownerId;
	}

	public Long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public void setorderType(Integer orderType) {
		this.orderType = orderType;
	}

	public void setPrice(int price) {
		this.price = price;
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

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}
}
