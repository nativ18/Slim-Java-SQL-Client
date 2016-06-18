package environment;

public class EnvConstants {

	private static final Environment ENV = Environment.LOCAL;

	public static ConfigEnv env;

	private enum Environment {
		LOCAL, REMOTE
	}

	public static final String DATABASE = "genericdao_db";
	public static final String LOCAL_USER = "";
	public static final String LOCAL_PASSWORD = "";
	public static final String LOCAL_HOST = "localhost";

	public static final String REMOTE_USER = "";
	public static final String REMOTE_PASSWORD = "";
	public static final String REMOTE_HOST = "***.amazonaws.com";

	public static final int PORT = 3306;

	static {
		if (ENV == Environment.REMOTE) {
			env = new ConfigEnv(REMOTE_HOST, DATABASE, REMOTE_USER, REMOTE_PASSWORD, PORT);
		} else {
			env = new ConfigEnv(LOCAL_HOST, DATABASE, LOCAL_USER, LOCAL_PASSWORD, PORT);
		}
	}

}