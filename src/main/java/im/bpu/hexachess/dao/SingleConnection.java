package im.bpu.hexachess.dao;

import im.bpu.hexachess.Config;

import java.sql.Connection;
import java.sql.SQLException;

import com.mysql.cj.jdbc.MysqlDataSource;

public class SingleConnection {
	private static Connection connect;
	private SingleConnection() throws ClassNotFoundException, SQLException {
		String url =
			Config.get("DB_URL", "jdbc:mysql://localhost:3306/hexachess?serverTimezone=UTC");
		String login = Config.get("DB_USER", "root");
		String password = Config.get("DB_PASS", getPassword());
		MysqlDataSource mysqlDS = new MysqlDataSource();
		mysqlDS.setURL(url);
		mysqlDS.setUser(login);
		mysqlDS.setPassword(password);
		connect = mysqlDS.getConnection();
	}
	private String getPassword() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("win"))
			return "";
		return "password123";
	}
	public static Connection getInstance() throws ClassNotFoundException, SQLException {
		if (connect == null || connect.isClosed())
			new SingleConnection();
		return connect;
	}
	public static void close() {
		try {
			if (connect != null && !connect.isClosed())
				connect.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
