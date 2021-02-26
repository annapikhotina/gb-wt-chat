import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class Controller {

  @FXML
  public TextArea chatArea;
  @FXML
  public TextArea inputArea;

  @FXML
  public void sendMessageByKey(KeyEvent keyEvent) {
    if (keyEvent.isShiftDown() && keyEvent.getCode().equals(KeyCode.ENTER)) {
      chatArea.appendText(inputArea.getText() + "\n");
      inputArea.clear();
      inputArea.requestFocus();
    }
  }

  @FXML
  public void sendMessage(ActionEvent actionEvent) {
    chatArea.appendText(inputArea.getText() + "\n");
    inputArea.clear();
    inputArea.requestFocus();
  }
}

