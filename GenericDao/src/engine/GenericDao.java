package engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;

import datamanager.model.BaseEntity;
import utils.CheckedParam;
import utils.Tuple;

/**
 * 
 * @author nativ
 *
 * @param <T>
 */

public abstract class GenericDao<T extends BaseEntity> implements CheckedFunction<ResultSet, Integer, ArrayList<T>> {

	private static final int SLECT_COUNT = 1;
	private static final int INSERT = 2;
	private static final int SLECT_COUNT_WHERE = 3;
	private static final int SLECT_COUNT_FROM_ID = 4;
	private static final int SELECT_WHERE = 5;
	private static final int SELECT_ALL = 6;
	private static final int UPDATE = 7;
	private static final int IS_EXISTS = 8;
	private static final int SELECT_FOR_INDEX = 100;
	private static final int DELETE_FOR_INDEX = 200;

	/**
	 * Next open cache key for custom statements
	 */
	private int mNextCacheKey = DELETE_FOR_INDEX + 1;

	/**
	 * Reflections fields. Generic by BaseEntity's type
	 */
	private Class<T> classType;
	protected ArrayList<Field> mFields;
	private ArrayList<String> mSqlColumns;
	protected Constructor<T> mDeserializer;

	protected HashMap<Integer, java.sql.PreparedStatement> mStmntCache;

	protected abstract String getTableName();

	private String insertStmntStr;
	private String updateStmntStr;

	public GenericDao(Class<T> classType) throws Exception {
		this.classType = classType;
		Constructor<T>[] constructors = (Constructor<T>[]) classType.getConstructors();
		int constructorLength = constructors.length;
		for (int i = 0; i < constructorLength; i++) {
			if (constructors[i].isAnnotationPresent(Deserializer.class)) {
				this.mDeserializer = constructors[i];
				break;
			}
		}

		if (mDeserializer == null)
			throw new Exception("Entity does not support GenericDao. No Deserilizer constructor found");

		mStmntCache = new HashMap<Integer, java.sql.PreparedStatement>(8);

		Field[] fields = classType.getDeclaredFields();
		Field[] superclassFields = classType.getSuperclass().getDeclaredFields();
		int size = fields.length + superclassFields.length;

		mSqlColumns = new ArrayList<String>(size);
		mFields = new ArrayList<>(size);
		Field field;
		for (int i = 0; i < size; i++) {
			if (i < superclassFields.length) {
				field = superclassFields[i];
			} else {
				field = fields[i - superclassFields.length];
			}
			SqlBinder ann = field.getAnnotation(SqlBinder.class);
			if (ann != null) {
				mSqlColumns.add(ann.val());
				mFields.add(field);
			}
		}
	}

	protected int requestCacheKey() {
		return mNextCacheKey++;
	}

	protected java.sql.PreparedStatement getOrSet(Context context, int stmntIndex, String statement) throws Exception {
		java.sql.PreparedStatement preparedStmt = mStmntCache.get(stmntIndex);
		if (preparedStmt == null) {
			Connection connection = SqlConnector.getInstance(context).getRestoreConnection();
			if (stmntIndex == INSERT)
				preparedStmt = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
			else
				preparedStmt = connection.prepareStatement(statement);

			mStmntCache.put(stmntIndex, preparedStmt);
		}
		return preparedStmt;
	}

	public boolean isExists(Context context, long id) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, IS_EXISTS,
				String.format("select count(*) from %s where id=?;", getTableName()));
		preparedStmt.setLong(1, id);
		ResultSet rs = preparedStmt.executeQuery();
		if (rs.next())
			return rs.getLong(1) > 0;
		else
			return false;
	}

	public ArrayList<T> selectAll(Context context) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, SELECT_ALL,
				String.format("select * from %s;", getTableName()));

		ResultSet rs = preparedStmt.executeQuery();
		return buildEntities(rs, -1);
	}

	public T select(Context context, long id) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, SELECT_WHERE,
				String.format("select * from %s where id=?;", getTableName()));

		preparedStmt.setLong(1, id);
		ResultSet rs = preparedStmt.executeQuery();
		if (rs.next())
			return buildEntity(rs);
		else
			return null;
	}

	public ArrayList<T> selectForIndex(Context context, String string, Object val) throws Exception {
		return selectForIndex(context, string, val, -1);
	}

	public ArrayList<T> selectForIndex(Context context, String string, Object val, int maxResults) throws Exception {
		int suffix = val.hashCode();
		String stmnt;
		if (maxResults == -1) {
			stmnt = String.format("select * from %s where %s=?;", getTableName(), string);
		} else
			stmnt = String.format("select * from %s where %s=? limit %s;", getTableName(), string, maxResults);

		java.sql.PreparedStatement preparedStmt = getOrSet(context,
				(int) (SELECT_FOR_INDEX + string.hashCode() + suffix), stmnt);

		preparedStmt.setObject(1, val);
		ResultSet rs = preparedStmt.executeQuery();
		return buildEntities(rs, -1);
	}

	public Tuple<Integer, PreparedStatement> deleteForIndex(Context context, String string, long id) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, (int) (DELETE_FOR_INDEX + string.hashCode() + id),
				String.format("delete from %s where %s=?;", getTableName(), string));

		preparedStmt.setLong(1, id);
		int rowsAffected = preparedStmt.executeUpdate();
		return new Tuple<Integer, PreparedStatement>(rowsAffected, preparedStmt);
	}

	public int getCount(Context context) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, SLECT_COUNT,
				String.format("select count(*) from %s;", getTableName()));

		ResultSet rs = preparedStmt.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	public Tuple<Integer, PreparedStatement> update(Context context, T t) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, UPDATE, initUpdateStmnt(t));
		bindColumns(preparedStmt, t);
		preparedStmt.setLong(mFields.size(), t.getId());
		int rowsAffected = preparedStmt.executeUpdate();
		return new Tuple<Integer, PreparedStatement>(rowsAffected, preparedStmt);
	}

	protected String initUpdateStmnt(T t, String... fieldsToRemove) {
		if (this.updateStmntStr == null) {
			StringBuilder builder = new StringBuilder(String.format("update %s set ", getTableName()));

			ArrayList<String> list = new ArrayList<String>(mSqlColumns);
			if (fieldsToRemove != null && fieldsToRemove.length > 0) {
				List<String> toRemove = Arrays.asList(fieldsToRemove);
				list.removeAll(toRemove);
			}

			for (int i = 1; i < list.size(); i++) {
				builder.append(list.get(i)).append("=?").append(i == list.size() - 1 ? "" : ",");
			}
			builder.append(" where id = ?;");

			updateStmntStr = builder.toString();
		}
		return updateStmntStr;
	}

	protected String initInsertStmnt(T t, String... fieldsToRemove) {
		if (this.insertStmntStr == null) {
			StringBuilder builder = new StringBuilder(String.format("insert into %s (", getTableName()));
			StringBuilder questionMarks = new StringBuilder();

			List<String> toRemove = null;
			int numFieldsToRemove = 0;
			if (fieldsToRemove != null) {
				toRemove = Arrays.asList(fieldsToRemove);
				numFieldsToRemove = fieldsToRemove.length;
			}

			for (int i = 0; i < mSqlColumns.size() - numFieldsToRemove - 1; i++) {
				// -1 because we don't want to insert an id.
				questionMarks.append(i == 0 ? "?" : ",?");
			}

			ArrayList<String> list = new ArrayList<String>(mSqlColumns);
			if (toRemove != null && !toRemove.isEmpty())
				list.removeAll(toRemove);

			list.remove("id");
			String columns = list.toString();
			builder.append(columns.substring(1, columns.length() - 1));
			builder.append(") VALUES(").append(questionMarks.toString()).append(");");
			insertStmntStr = builder.toString();
		}
		return insertStmntStr;
	}

	public Tuple<Integer, PreparedStatement> insert(Context context, T t) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, INSERT, initInsertStmnt(t));
		bindColumns(preparedStmt, t);
		int rowsAffected = preparedStmt.executeUpdate();
		return new Tuple<Integer, PreparedStatement>(rowsAffected, preparedStmt);
	}

	public void bindColumns(java.sql.PreparedStatement preparedStatement, T t, String... ignoreFileds)
			throws Exception {
		int stmntNextParamIndex = 1;
		String methodName;
		String fieldName;
		List<String> toIgnore = ignoreFileds == null ? new ArrayList<String>(0) : Arrays.asList(ignoreFileds);
		for (int i = 0; i < mFields.size(); i++) {
			fieldName = mFields.get(i).getName();

			String columnName = toIgnore != null && !toIgnore.isEmpty()
					? mFields.get(i).getAnnotation(SqlBinder.class).val() : null;
			// we use the bindColumns only for insert for now and for insert we
			// never wants to assign a id ourselves.
			if (fieldName.equals("id") || (columnName != null && toIgnore.contains(columnName)))
				continue;

			methodName = String.format("get%s%s", fieldName.substring(0, 1).toUpperCase(), fieldName.substring(1));
			preparedStatement.setObject(stmntNextParamIndex, classType.getMethod(methodName).invoke(t));
			stmntNextParamIndex++;
		}
	}

	public ArrayList<T> buildEntities(CheckedParam params) {
		try {
			return buildEntities(params.getResultSet(), -1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<T> buildEntities(ResultSet rs) throws Exception {
		return buildEntities(rs, -1);
	}

	public ArrayList<T> buildEntities(ResultSet rs, int maxResults) throws Exception {

		ArrayList<T> entites = new ArrayList<>();

		while (rs.next()) {
			entites.add(buildEntity(rs));

			maxResults--;
			if (maxResults == 0)
				break;
		}

		return entites;
	}

	public T buildEntity(CheckedParam param) throws Exception {
		return buildEntity(param.getResultSet());
	}

	public T buildEntity(ResultSet rs) throws Exception {

		ArrayList<Object> parmas = new ArrayList<Object>();

		for (int i = 0; i < mFields.size(); i++) {
			SqlBinder cnao = mFields.get(i).getAnnotation(SqlBinder.class);
			parmas.add(rs.getObject(cnao.val()));
		}

		return mDeserializer.newInstance(parmas.toArray());
	}

	public int getCountForColumn(Context context, String indexName, Object indexVal) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, SLECT_COUNT_WHERE,
				String.format("select count(*) from %s where %s=?;", getTableName(), indexName));

		preparedStmt.setObject(1, indexVal);
		ResultSet rs = preparedStmt.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	public int getCountForPagination(Context context, String fkName, Object fkValue, String indexName, long indexValue)
			throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, SLECT_COUNT_FROM_ID,
				String.format("select count(*) from %s where %s=? and %s<?;", getTableName(), fkName, indexName));

		preparedStmt.setObject(1, fkValue);
		preparedStmt.setObject(2, indexValue);
		ResultSet rs = preparedStmt.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	/**
	 * The following function is being used by the
	 * {@code SQLPager.mResultSetParserFunc} as an entities builder.
	 **/
	@Override
	public ArrayList<T> apply(ResultSet t, Integer maxResult) throws Exception {
		return buildEntities(t, maxResult);
	}
}
