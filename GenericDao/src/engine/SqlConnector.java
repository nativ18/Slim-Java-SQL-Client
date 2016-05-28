package engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.amazonaws.services.lambda.runtime.Context;

import environment.EnvConstants;

public class SqlConnector {

	private static final String jdbcUrl;

	private static String db_name;
	private static String username;
	private static String password;

	static {
		db_name = EnvConstants.env.getDatabase();
		username = EnvConstants.env.getUser();
		password = EnvConstants.env.getPassword();

		jdbcUrl = "jdbc:mysql://" + EnvConstants.env.getHost() + ":" + EnvConstants.env.getPort() + "/" + db_name;
	}

	private Connection connection;

	private static SqlConnector INSTANCE;

	public static synchronized SqlConnector getInstance(Context context) throws Exception {
		if (INSTANCE == null)
			INSTANCE = new SqlConnector(context);

		return INSTANCE;
	}

	public synchronized Connection getRestoreConnection() throws SQLException {
		if (connection.isClosed())
			connection = DriverManager.getConnection(jdbcUrl, username, password);

		return connection;
	}

	private SqlConnector(Context context) throws Exception {
		try {
			context.getLogger().log("connecting to db..");
			String driver = "com.mysql.jdbc.Driver";
			Class.forName(driver).newInstance();
			connection = DriverManager.getConnection(jdbcUrl, username, password);
			context.getLogger().log("successfully connected!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("username: " + username + ". password: " + password + ". jdbcUrl: " + jdbcUrl + ". "
					+ e.getMessage());
		}
	}
}