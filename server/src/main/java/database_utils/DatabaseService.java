package database_utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseService {

  private Connection connection;

  public Connection getConnection() {
    return connection;
  }

  public DatabaseService() {
    try {
      connect();
    }
    catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
    }
  }

  private void connect() throws ClassNotFoundException, SQLException {
    Class.forName("org.sqlite.JDBC");
    connection = DriverManager.getConnection("jdbc:sqlite:gb.main");
  }

  private void disconnect() throws SQLException {
    connection.close();
  }

}
