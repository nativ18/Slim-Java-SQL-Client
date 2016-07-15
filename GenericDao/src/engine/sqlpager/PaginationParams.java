package engine.sqlpager;

public class PaginationParams {

	public static final long DEFAULT_HEIGHEST_ID = -1;

	private long loadMoreId = DEFAULT_HEIGHEST_ID;
	private int pageIndex = (int) DEFAULT_HEIGHEST_ID;
	private long heighestVisibleId = DEFAULT_HEIGHEST_ID;
	private long extraIndexValue;
	private String extraIndexName;

	private long indexValue;

	public PaginationParams() {
	}

	public boolean isGetFirstPageRequest() {
		return pageIndex == 0 || pageIndex == DEFAULT_HEIGHEST_ID && loadMoreId == DEFAULT_HEIGHEST_ID
				&& heighestVisibleId == DEFAULT_HEIGHEST_ID;
	}

	public boolean isGetPageByIndexRequest() {
		return pageIndex != DEFAULT_HEIGHEST_ID && loadMoreId == DEFAULT_HEIGHEST_ID;

	}

	public boolean isGetNewResultsForIdRequest() {
		return loadMoreId == DEFAULT_HEIGHEST_ID && heighestVisibleId != DEFAULT_HEIGHEST_ID;
	}

	public boolean isLoadMoreRequest() {
		return loadMoreId != DEFAULT_HEIGHEST_ID && heighestVisibleId == DEFAULT_HEIGHEST_ID;
	}

	public long getHeighestThen() {
		return heighestVisibleId;
	}

	public void setHeighestThen(long value) {
		this.heighestVisibleId = value;
	}

	public long getLoadMoreId() {
		return loadMoreId;
	}

	public void setLoadMoreId(long loadMoreId) {
		this.loadMoreId = loadMoreId;
	}

	public long getExtraIndex() {
		return extraIndexValue;
	}

	public long getIndexValue() {
		return indexValue;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public long getExtraIndexValue() {
		return extraIndexValue;
	}

	public void setExtraIndexValue(long extraIndexValue) {
		this.extraIndexValue = extraIndexValue;
	}

	public String getExtraIndexName() {
		return extraIndexName;
	}

	public void setExtraIndexName(String extraIndexName) {
		this.extraIndexName = extraIndexName;
	}

	public void setIndexValue(long indexValue) {
		this.indexValue = indexValue;
	}
}
