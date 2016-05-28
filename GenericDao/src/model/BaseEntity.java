package model;

import engine.SqlBinder;

public class BaseEntity {

	public static final long DEFAULT_VALUE = -1;
	
	@SqlBinder(val = "id")
	protected long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public BaseEntity() {
	}

	public BaseEntity(long id) {
		this.id = id;
	}
}
