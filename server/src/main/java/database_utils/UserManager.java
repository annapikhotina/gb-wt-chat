package database_utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserManager {

  private final DatabaseService databaseService = new DatabaseService();

  public String findNickNameByLoginAndPassword(String login, String password) {
    String nickName = "";
    try {
      PreparedStatement prdStatement = databaseService.getConnection().prepareStatement(
        "SELECT nickname FROM USERS WHERE login = ? AND password = ?");

      prdStatement.setString(1, login);
      prdStatement.setString(2, password);
      nickName = prdStatement.executeQuery().getString("NICKNAME");
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

  public boolean isLoginExisted(String login) {
    try {
      PreparedStatement prdStatement =
        databaseService.getConnection().prepareStatement("SELECT login FROM USERS WHERE login = ?");
      prdStatement.setString(1, login);
      ResultSet result = prdStatement.executeQuery();
      if (!result.isClosed()) {
        return result.getString("LOGIN").equals(login);
      } else {
        return false;
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return true;
  }

  public boolean insertUser(String login, String password, String nickname) {
    try {
      PreparedStatement prdStatement =
        databaseService.getConnection().prepareStatement(
          "INSERT INTO USERS (login, password, nickname) VALUES (?, ?, ?)");
      prdStatement.setString(1, login);
      prdStatement.setString(2, password);
      prdStatement.setString(3, nickname);
      prdStatement.executeUpdate();
      return true;
    }
    catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean updateNickname(String login, String nickname) {
    try {
      PreparedStatement prdStatement =
        databaseService.getConnection().prepareStatement("UPDATE USERS SET nickname = ? WHERE login = ?");
      prdStatement.setString(1, nickname);
      prdStatement.setString(2, login);
      prdStatement.executeUpdate();
      return true;
    }
    catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }
}
