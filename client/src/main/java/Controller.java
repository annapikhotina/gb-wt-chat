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
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Controller implements Initializable {

  private Stage stage;
  private Stage regStage;
  private RegController regController;
  private final int PORT = 8189;
  private final String IP_ADDRESS = "localhost";
  private Socket socket;
  private DataInputStream in;
  private DataOutputStream out;
  private boolean authenticated = false;
  private String nickname;

  @FXML
  private HBox authPanel;
  @FXML
  private HBox messagePanel;
  @FXML
  private TextField loginField;
  @FXML
  private PasswordField passwordField;
  @FXML
  private TextArea chatArea;
  @FXML
  private TextArea inputArea;
  @FXML
  private ListView<String> clientList;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Platform.runLater(() -> {
      stage = (Stage) chatArea.getScene().getWindow();
      stage.setOnCloseRequest(event -> {
        System.out.println("bye");
        if (socket != null && !socket.isClosed()) {
          try {
            out.writeUTF(Commands.END);
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
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

          //Disconnection
          while (true) {
            String message = in.readUTF();

            if (message.startsWith("/")) {
              if (message.equals(Commands.END)) {
                throw new RuntimeException("Server is disconnected");
              }

              //Authentication
              if (message.startsWith(Commands.AUTH_OK)) {
                String[] token = message.split("\\s");
                nickname = token[1];
                setAuthenticated(true);
                break;
              }

              //Registration
              if (message.startsWith(Commands.RegOK)) {
                regController.setAuthResponse(Commands.RegOK);
              }

              if (message.startsWith(Commands.RegNO)) {
                regController.setAuthResponse(Commands.RegNO);
              }
            } else {
              chatArea.appendText(message + "\n");
            }
          }

          //Chatting
          while (true) {
            String message = in.readUTF();

            if (message.startsWith("/")) {
              if (message.equals(Commands.END)) {
                System.out.println("Client disconnected");
                break;
              }
              if (message.startsWith(Commands.CLIENT_LIST)) {
                String[] token = message.split("\\s");
                Platform.runLater(() -> {
                  clientList.getItems().clear();
                  for (int i = 1; i < token.length; i++) {
                    clientList.getItems().add(token[i]);
                  }
                });
              }
            } else {
              chatArea.appendText(message + "\n");
            }
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
    clientList.setVisible(authenticated);
    clientList.setManaged(authenticated);

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
      out.writeUTF(
        String.format("%s %s %s", Commands.AUTH, loginField.getText().trim(), passwordField.getText().trim()));
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

  public void clientListMouseReleased(MouseEvent mouseEvent) {
    System.out.println(clientList.getSelectionModel().getSelectedItems());
    String message =
      String.format("%s %s ", Commands.PRIVATE_MESSAGE, clientList.getSelectionModel().getSelectedItems());
    inputArea.setText(message);
  }

  public void showRegWindow(ActionEvent actionEvent) {
    if (regStage == null) {
      initRegWindow();
    }
    regStage.show();
  }

  private void initRegWindow() {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
      Parent root = fxmlLoader.load();

      regController = fxmlLoader.getController();
      regController.setController(this);

      regStage = new Stage();
      regStage.setTitle("Walkie Talkie - Registration");
      regStage.getIcons().add(new Image("/images/send_button_paw.png"));
      regStage.setScene(new Scene(root, 350, 450));
      regStage.initStyle(StageStyle.UTILITY);
      regStage.initModality(Modality.APPLICATION_MODAL);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void registration(String login, String password, String nickname) {

    if (socket == null || socket.isClosed()) {
      connect();
    }

    try {
      out.writeUTF(String.format("%s %s %s %s", Commands.Reg, login, password, nickname));
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}

