import java.util.ArrayList;
import java.util.List;
import database_utils.UserManager;

public class SimpleAuthService implements AuthService {

  UserManager userManager = new UserManager();

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
    return userManager.findNickNameByLoginAndPassword(login, password);
  }

  @Override
  public boolean registration(String login, String password, String nickname) {
    if (userManager.isLoginExisted(login)) {
      return false;
    }
    return userManager.insertUser(login, password, nickname);
  }

  @Override
  public boolean updateNickname(String login, String nickname) {
    if(userManager.isLoginExisted(nickname)) {
      return false;
    }
    return userManager.updateNickname(login, nickname);
  }
}
