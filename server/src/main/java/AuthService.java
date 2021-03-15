public interface AuthService {

  String getNickNameByLoginAndPassword(String login, String password);
  boolean registration(String login, String password, String nickname);
  boolean updateNickname(String login, String nickname);
}
