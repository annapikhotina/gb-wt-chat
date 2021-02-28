import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import commands.Commands;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Controller implements Initializable {
  private Stage stage;
  private final int PORT = 8189;
  private final String IP_ADDRESS = "localhost";
  private Socket socket;
  private DataInputStream in;
  private DataOutputStream out;
  private boolean authenticated = false;
  private String nickname;

  @FXML
  public HBox authPanel;
  @FXML
  public HBox messagePanel;
  @FXML
  public TextField loginField;
  @FXML
  public PasswordField passwordField;
  @FXML
  public TextArea chatArea;
  @FXML
  public TextArea inputArea;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Platform.runLater(() -> {
      stage = (Stage) chatArea.getScene().getWindow();
    });
    setAuthenticated(false);
  }

  private void connect() {

    try {
      socket = new Socket(IP_ADDRESS, PORT);
      in = new DataInputStream(socket.getInputStream());
      out = new DataOutputStream(socket.getOutputStream());

      new Thread(() -> {
        try {
          //Auth loop
          while (true) {
            String message = in.readUTF();

            if (message.startsWith("/")) {
              if (message.equals(Commands.END)) {
                throw new RuntimeException("Server is disconnected");
              }

              if (message.startsWith(Commands.AUTH_OK)) {
                String[] token = message.split("\\s");
                nickname = token[1];
                setAuthenticated(true);
                break;
              }
            } else {
              chatArea.appendText(message + "\n");
            }
          }

          //Chat loop
          while (true) {
            String message = in.readUTF();

            if (message.equals(Commands.END)) {
              System.out.println("Client disconnected");
              break;
            }

            chatArea.appendText(message + "\n");
          }
        }
        catch (RuntimeException e) {
          System.out.println(e.getMessage());
        }
        catch (IOException e) {
          e.printStackTrace();
        }
        finally {
          setAuthenticated(false);
          try {
            socket.close();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }

      }).start();

    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  @FXML
  public void sendMessageByKey(KeyEvent keyEvent) {
    if (keyEvent.isShiftDown() && keyEvent.getCode().equals(KeyCode.ENTER)) {
      sendMessage();
    }
  }

  @FXML
  public void sendMessage(ActionEvent actionEvent) {
    sendMessage();
  }

  private void sendMessage() {
    try {
      out.writeUTF(inputArea.getText());
      inputArea.clear();
      inputArea.requestFocus();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setAuthenticated(boolean authenticated) {
    this.authenticated = authenticated;
    messagePanel.setVisible(authenticated);
    messagePanel.setManaged(authenticated);
    authPanel.setVisible(!authenticated);
    authPanel.setManaged(!authenticated);

    if (!authenticated) {
      nickname = "";
    }
    setTitle(nickname);
    chatArea.clear();
  }

  public void tryToLogin(ActionEvent actionEvent) {
    if (socket == null || socket.isClosed()) {
      connect();
    }
    try {
      out.writeUTF(String.format("%s %s %s", Commands.AUTH, loginField.getText().trim(), passwordField.getText().trim()));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      passwordField.clear();
    }
  }

  private void setTitle(String nickname) {
    Platform.runLater(() -> {
      if (nickname.equals("")) {
        stage.setTitle("Walkie Talkie");
      } else {
        stage.setTitle(String.format("Walkie Talkie - [%s]", nickname));
      }
    });
  }
}

