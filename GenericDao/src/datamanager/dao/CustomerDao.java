package datamanager.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.amazonaws.services.lambda.runtime.Context;

import datamanager.model.Customer;
import engine.GenericDao;

public class CustomerDao extends GenericDao<Customer> {

	public static final String TABLE_NAME = "customers";
	
	private static final String SELECT_PASSWORD_QUERY = String.format("select password from %s where email=?;",
			TABLE_NAME);

	private final int SELECT_PASSWORD_CACHE_KEY = requestCacheKey();

	@Override
	protected String getTableName() {
		return TABLE_NAME;
	}

	public CustomerDao() throws Exception {
		super(Customer.class);
	}

	public boolean isPasswordValid(Context context, String email, String userPassword) throws Exception {
		PreparedStatement preparedStatement = getOrSet(context, SELECT_PASSWORD_CACHE_KEY, SELECT_PASSWORD_QUERY);
		preparedStatement.setString(1, email);
		ResultSet rs = preparedStatement.executeQuery();
		if (rs.next()) {
			String password = rs.getString("password");
			return password != null && password.equals(userPassword);
		}
		return true;
	}
}