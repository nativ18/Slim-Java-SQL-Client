package engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;

import model.BaseEntity;
import utils.Tuple;

/**
 * 
 * @author nativ
 *
 * @param <T>
 */

public abstract class GenericDao<T extends BaseEntity>{

	private static final int SLECT_COUNT = 1;
	private static final int INSERT = 2;
	private static final int SLECT_COUNT_WHERE = 3;
	private static final int SLECT_COUNT_FROM_ID = 4;
	private static final int SELECT_WHERE = 5;
	private static final int SELECT_ALL = 6;
	private static final int UPDATE = 7;
	private static final int SELECT_FOR_INDEX = 100;
	private static final int DELETE_FOR_INDEX = 200;

	private Class<T> classType;
	private ArrayList<Field> mFields;
	private ArrayList<String> mSqlColumns;
	private Constructor<T> mDeserializer;
	
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

	protected java.sql.PreparedStatement getOrSet(Context context, int stmntIndex, String statement) throws Exception {
		java.sql.PreparedStatement preparedStmt = mStmntCache.get(stmntIndex);
		if (preparedStmt == null) {
			Connection connection = SqlConnector.getInstance(context).getRestoreConnection();
			preparedStmt = connection.prepareStatement(statement);
			mStmntCache.put(stmntIndex, preparedStmt);
		}
		return preparedStmt;
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

	public ArrayList<T> selectForIndex(Context context, String string, long id) throws Exception {
		return selectForIndex(context, string, id, -1);
	}

	public ArrayList<T> selectForIndex(Context context, String string, long id, int maxResults) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, (int) (SELECT_FOR_INDEX + string.hashCode() + id),
				String.format("select * from %s where %s=? limit %s;", getTableName(), string, maxResults));

		preparedStmt.setLong(1, id);
		ResultSet rs = preparedStmt.executeQuery();
		return buildEntities(rs, -1);
	}

	public void deleteForIndex(Context context, String string, long id) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, (int) (DELETE_FOR_INDEX + string.hashCode() + id),
				String.format("delete from %s where %s=?;", getTableName(), string));

		preparedStmt.setLong(1, id);
		preparedStmt.executeUpdate();
	}

	public int getCount(Context context) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, SLECT_COUNT,
				String.format("select count(*) from %s;", getTableName()));

		ResultSet rs = preparedStmt.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	private String initInsertStmnt(T t) {
		if (this.insertStmntStr == null) {
			StringBuilder builder = new StringBuilder(String.format("insert into %s (", getTableName()));
			StringBuilder questionMarks = new StringBuilder();

			for (int i = 0; i < mSqlColumns.size() - 1; i++) { // -1 because
																	// we
																	// don't
																	// want to
																	// insert an
																	// id.
				questionMarks.append(i == 0 ? "?" : ",?");
			}

			ArrayList<String> list = new ArrayList<String>(mSqlColumns);
			list.remove("id");
			String columns = list.toString();
			builder.append(columns.substring(1, columns.length() - 1));
			builder.append(") VALUES(").append(questionMarks.toString()).append(");");
			insertStmntStr = builder.toString();
		}
		return insertStmntStr;
	}

	public void update(Context context, T t) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, UPDATE, initUpdateStmnt(t));
		bindColumns(preparedStmt, t);
		preparedStmt.setLong(mFields.size(), t.getId());
		preparedStmt.executeUpdate();
	}

	private String initUpdateStmnt(T t) {
		if (this.updateStmntStr == null) {
			StringBuilder builder = new StringBuilder(String.format("update %s set ", getTableName()));

			for (int i = 1; i < mSqlColumns.size(); i++) {
				builder.append(mSqlColumns.get(i)).append("=?").append(i == mSqlColumns.size() - 1 ? "" : ",");
			}
			builder.append(" where id = ?;");

			updateStmntStr = builder.toString();
		}
		return updateStmntStr;
	}

	public Tuple<Integer, PreparedStatement> insert(Context context, T t) throws Exception {
		java.sql.PreparedStatement preparedStmt = mStmntCache.get(INSERT);
		if (preparedStmt == null) {
			Connection connection = SqlConnector.getInstance(context).getRestoreConnection();
			preparedStmt = connection.prepareStatement(initInsertStmnt(t), Statement.RETURN_GENERATED_KEYS);
			mStmntCache.put(INSERT, preparedStmt);
		}

		bindColumns(preparedStmt, t);
		int rowsAffected = preparedStmt.executeUpdate();
		return new Tuple<Integer, PreparedStatement>(rowsAffected, preparedStmt);
	}

	public void bindColumns(java.sql.PreparedStatement preparedStatement, T t) throws Exception {
		String methodName;
		String fieldName;
		for (int i = 0; i < mFields.size(); i++) {
			fieldName = mFields.get(i).getName();
			if (fieldName.equals("id")) // we use the bindColumns only for
										// insert for now and for insert we
										// never wants to assign a id ourselves.
				continue;

			methodName = String.format("get%s%s", fieldName.substring(0, 1).toUpperCase(), fieldName.substring(1));
			preparedStatement.setObject(i, classType.getMethod(methodName).invoke(t));
		}
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

	public T buildEntity(ResultSet rs) throws Exception {

		ArrayList<Object> parmas = new ArrayList<Object>();

		for (int i = 0; i < mFields.size(); i++) {
			SqlBinder cnao = mFields.get(i).getAnnotation(SqlBinder.class);
			parmas.add(rs.getObject(cnao.val()));
		}

		return mDeserializer.newInstance(parmas.toArray());
	}

	public int getCountForColumn(Context context, String fkName, long userId) throws Exception {
		java.sql.PreparedStatement preparedStmt = getOrSet(context, SLECT_COUNT_WHERE,
				String.format("select count(*) from %s where %s=?;", getTableName(), fkName));

		preparedStmt.setObject(1, userId);
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
}
