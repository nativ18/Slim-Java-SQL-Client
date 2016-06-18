package dao;

import com.amazonaws.services.lambda.runtime.Context;
import engine.GenericDao;
import model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDao extends GenericDao<User> {

    public static final String TABLE_NAME = "users";
    private static final String SELECT_PASSWORD_QUERY = "select password from users where email=?;";

    private static final int SELECT_PASSWORD_CACHE_KEY = 600;

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    public UserDao() throws Exception {
        super(User.class);
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