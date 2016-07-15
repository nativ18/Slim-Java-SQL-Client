package engine.sqlpager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Function;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;

import datamanager.model.BaseEntity;
import engine.GenericDao;
import engine.SqlConnector;
import utils.CheckedParam;

public class SQLPager<T extends BaseEntity> implements ISQLPager {

	private static final String TAG = "SQLPager";

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final long DEFAULT_VALUE = -1;

	private static final String SELECT_FIRST_PAGE = "SELECT * FROM %s WHERE %s=? order by id desc LIMIT %s;";
	private static final String SELECT_COUNT = "SELECT count(*) FROM %s WHERE %s=?;";
	private static final String SELECT_MORE_PAGES = "SELECT * FROM %s WHERE %s=? and id<? order by id desc LIMIT %s;";
	private static final String SELECT_ONLY_NEW_FOR_ID = "SELECT * FROM %s WHERE %s=? and id>? order by id desc LIMIT %s;";

	protected Connection connection;
	private Context mContext;
	private Gson gson;
	private ResultSet mCurrentResultSet;
	private Function<CheckedParam, ArrayList<T>> mResultSetParserFunc;

	protected int mPageSize = DEFAULT_PAGE_SIZE;
	private boolean mBlockLoadToMem = false;

	public SQLPager(Function<CheckedParam, ArrayList<T>> function, Context context) {
		this.mResultSetParserFunc = function;
		this.mContext = context;
		this.gson = new Gson();
	}

	public CheckedParam getCheckedParams(ResultSet rs, int pageSize) {
		return new CheckedParam(rs, pageSize);
	}

	// Override it in order to create a sorted result set
	public String getFirstPageQuery(String tableName, String pkName, long entityId) {
		if (mPageSize == -1) {
			String stmnt = getFirstPageQuery().replace("LIMIT %s", "");
			return String.format(stmnt, tableName, pkName);
		}
		return String.format(getFirstPageQuery(), tableName, pkName, mPageSize);
	}

	// Override it in order to create a sorted result set
	public String getNextPageQuery(String tableName, String... fkName) {
		if (mPageSize == -1) {
			String stmnt = getNextPageQuery().replace("LIMIT %s", "");
			return String.format(stmnt, tableName, fkName);
		}
		return String.format(getNextPageQuery(), tableName, fkName[0], mPageSize);
	}

	// Override it in order to create a sorted result set
	public String getNewResultsForIdQuery(String tableName, String... fkName) {
		if (mPageSize == -1) {
			String stmnt = getOnlyNewEntitiesQuery().replace("LIMIT %s", "");
			return String.format(stmnt, tableName, fkName);
		}
		return String.format(getOnlyNewEntitiesQuery(), tableName, fkName[0], mPageSize);
	}

	// Override it in order to create a sorted result set
	public void bindNextPageStmnt(PreparedStatement preparedStatement, Object[] indexesValues, long entityId)
			throws SQLException {
		preparedStatement.setLong(1, entityId);

		for (int i = 2; i < indexesValues.length + 2; i++)
			preparedStatement.setLong(i, (long) indexesValues[i - 2]);
	}

	// To be overridden by subclasses who need to bind params for the first page
	// statement.
	public void bindFirstPageStmnt(PreparedStatement preparedStatement, String pkName, long entityId) throws Exception {
		preparedStatement.setLong(1, entityId);
	}

	public String getResults(PaginationParams paginationParams, GenericDao<? extends BaseEntity> genericDao,
			String tableName, String indexName) throws Exception {
		initIfNecessary();

		ArrayList<T> entities = new ArrayList<>();
		int count = 0;
		boolean hasMorePages = false;

		if (paginationParams.isGetFirstPageRequest()) {
			entities = getFirstPage(tableName, indexName, paginationParams.getIndexValue());
			count = genericDao.getCountForColumn(mContext, indexName, paginationParams.getIndexValue());
			hasMorePages = entities != null && !entities.isEmpty() && count > DEFAULT_PAGE_SIZE;
		} else if (paginationParams.isGetPageByIndexRequest()) {
			entities = getPageByIndex(tableName, indexName, paginationParams.getIndexValue(),
					paginationParams.getPageIndex(), new Object[] { DEFAULT_VALUE });
			count = selectCountForPaging(paginationParams.getPageIndex(), paginationParams.getIndexValue(), tableName,
					indexName);
			hasMorePages = entities != null && !entities.isEmpty() && count > 0;
		} else if (paginationParams.isLoadMoreRequest()) {
			entities = getNextPage(tableName, indexName, paginationParams.getIndexValue(),
					paginationParams.getLoadMoreId());
			count = genericDao.getCountForPagination(mContext, indexName, paginationParams.getIndexValue(),
					paginationParams.getExtraIndexName(), paginationParams.getLoadMoreId());
			hasMorePages = entities != null && !entities.isEmpty() && count > 0;
		} else if (paginationParams.isGetNewResultsForIdRequest()) {
			entities = getNewResultsForId(tableName, indexName, paginationParams.getIndexValue(),
					paginationParams.getHeighestThen());
			count = genericDao.getCountForColumn(mContext, indexName, paginationParams.getIndexValue());
			hasMorePages = false;
		}

		return gson.toJson(new GetPageResponse<T>(entities, hasMorePages));
	}

	private int selectCountForPaging(int pageIndex, long pkValue, String tableName, String primaryKey)
			throws SQLException {
		java.sql.PreparedStatement preparedStatement = connection
				.prepareStatement(String.format(SELECT_COUNT, tableName, primaryKey));

		preparedStatement.setObject(1, pkValue);
		ResultSet rs = preparedStatement.executeQuery();
		if (rs.next()) {
			int totalResults = rs.getInt(1);
			return totalResults - (pageIndex + 1) * DEFAULT_PAGE_SIZE;
		}

		return 0;
	}

	private ArrayList<T> getFirstPage(String tableName, String pkName, long entityId) throws Exception {
		return getNextPage(tableName, pkName, entityId, new Object[] { DEFAULT_VALUE });
	}

	private ArrayList<T> getNewResultsForId(String tableName, String fkName, long userId, long visibleHeighestId)
			throws Exception {
		String query = getNewResultsForIdQuery(tableName, fkName);
		java.sql.PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setLong(1, userId);
		preparedStatement.setLong(2, visibleHeighestId);
		mCurrentResultSet = preparedStatement.executeQuery();
		if (mBlockLoadToMem)
			return null;

		return mResultSetParserFunc.apply(getCheckedParams(mCurrentResultSet, (int) DEFAULT_PAGE_SIZE));
	}

	private ArrayList<T> getNextPage(String tableName, String pkName, long entityId, Object... indexesValues)
			throws Exception {
		long heighestId = (long) indexesValues[0];
		java.sql.PreparedStatement preparedStatement;

		if (heighestId == PaginationParams.DEFAULT_HEIGHEST_ID) {
			String selectFirstPage = getFirstPageQuery(tableName, pkName, entityId);
			preparedStatement = connection.prepareStatement(selectFirstPage);
			bindFirstPageStmnt(preparedStatement, pkName, entityId);
		} else {
			String selectNextPage = getNextPageQuery(tableName, pkName);
			preparedStatement = connection.prepareStatement(selectNextPage);
			bindNextPageStmnt(preparedStatement, indexesValues, entityId);
		}

		mCurrentResultSet = preparedStatement.executeQuery();
		if (mBlockLoadToMem)
			return null;

		return mResultSetParserFunc.apply(getCheckedParams(mCurrentResultSet, (int) DEFAULT_PAGE_SIZE));
	}

	private ArrayList<T> getPageByIndex(String tableName, String fkName, long entityId, int pageIndex,
			Object... indexesValues) throws Exception {

		mBlockLoadToMem = pageIndex != 0;
		mPageSize = pageIndex == 0 ? DEFAULT_PAGE_SIZE : (int) DEFAULT_VALUE;

		ArrayList<T> page = getFirstPage(tableName, fkName, entityId);
		if (pageIndex == 0) {
			resetState();
			return page;
		} else {
			for (int i = 0; i < DEFAULT_PAGE_SIZE * pageIndex; i++)
				if (!mCurrentResultSet.next())
					break;

			page = mResultSetParserFunc.apply(getCheckedParams(mCurrentResultSet, (int) DEFAULT_PAGE_SIZE));
		}
		resetState();
		return page;
	}

	private void resetState() {
		mBlockLoadToMem = false;
		mPageSize = DEFAULT_PAGE_SIZE;
	}

	private void initIfNecessary() {
		if (connection != null)
			return;

		try {
			connection = SqlConnector.getInstance(mContext).getRestoreConnection();
		} catch (Exception e) {
			mContext.getLogger().log(
					String.format("%s: Couldn't initiate connnection against SQL database. %s", TAG, e.getMessage()));
			e.printStackTrace();
		}
	}

	@Override
	public String getFirstPageQuery() {
		return SELECT_FIRST_PAGE;
	}

	@Override
	public String getNextPageQuery() {
		return SELECT_MORE_PAGES;
	}

	@Override
	public String getOnlyNewEntitiesQuery() {
		return SELECT_ONLY_NEW_FOR_ID;
	}
}
