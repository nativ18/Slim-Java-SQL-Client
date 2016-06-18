package engine.sqlpager;

public interface ISQLPager {
	public String getFirstPageQuery();

	public String getNextPageQuery();
	public String getOnlyNewEntitiesQuery();

}
