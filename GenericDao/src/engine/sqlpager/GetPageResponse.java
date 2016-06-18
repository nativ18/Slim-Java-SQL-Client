package engine.sqlpager;

import java.util.ArrayList;

public class GetPageResponse<T> {

	ArrayList<T> mEntities;
	boolean hasMorePages;

	public GetPageResponse(ArrayList<T> mEntities, boolean hasMorePages) {
		super();
		this.mEntities = mEntities;
		this.hasMorePages = hasMorePages;
	}

	public GetPageResponse(boolean hasMorePages) {
		super();
		this.hasMorePages = hasMorePages;
	}

	public ArrayList<T> getEntities() {
		return mEntities;
	}

	public void setEntities(ArrayList<T> mEntities) {
		this.mEntities = mEntities;
	}

	public boolean hasMorePages() {
		return hasMorePages;
	}

	public void setHasMorePages(boolean hasMorePages) {
		this.hasMorePages = hasMorePages;
	}

}
