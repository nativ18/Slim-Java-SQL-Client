package environment;

public class ConfigEnv {

	private String host;
	private String database;
	private String user;
	private String password;
	private int port;

	public ConfigEnv(String host, String database, String user, String password, int port) {
		super();
		this.host = host;
		this.database = database;
		this.user = user;
		this.password = password;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
