package database_utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserManager {

  private final DatabaseService databaseService = new DatabaseService();

  public String findNickNameByLoginAndPassword(String login, String password) {
    String nickName = "";

    try {
      PreparedStatement statement = databaseService.getConnection().prepareStatement(
        "SELECT nickname FROM USERS WHERE login = ? AND password = ?");

      statement.setString(1, login);
      statement.setString(2, password);
      nickName = statement.executeQuery().getString("NICKNAME");
    }
    catch (SQLException e) {
      e.printStackTrace();
    }

    if (!"".equals(nickName)) {
      return nickName;
    } else {
      return null;
    }
  }


}
