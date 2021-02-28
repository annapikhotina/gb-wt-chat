import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

  private class UserData {
    String login;
    String password;
    String nickNme;

    public UserData(String login, String password, String nickNme) {
      this.login = login;
      this.password = password;
      this.nickNme = nickNme;
    }
  }

  private List<UserData> users;

  public SimpleAuthService() {
    users = new ArrayList<>();
    users.add(new UserData("a", "a", "a"));
    users.add(new UserData("b", "b", "b"));
    users.add(new UserData("c", "c", "c"));
    users.add(new UserData("d", "d", "d"));
  }

  @Override
  public String getNickNameByLoginAndPassword(String login, String password) {
    for (UserData user : users) {
      if (user.login.equals(login) && user.password.equals(password)) {
        return user.nickNme;
      }
    }
    return null;
  }
}
