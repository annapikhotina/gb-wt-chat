import commands.Commands;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class UpdateNickNameController {
  @FXML
  public TextField nicknameField;
  @FXML
  public Button saveButton;

  private Controller controller;

  public void setController(Controller controller) {
    this.controller = controller;
  }

  public void tryToUpdateNickname(ActionEvent actionEvent) {
    String nickname = nicknameField.getText().trim();
    if(nickname.length() == 0) {
      return;
    }
    controller.updateNickname(nickname);
  }

  public void setUpdateRes(String command) {
    if (command.equals(Commands.UPDNICKOK)) {
      saveButton.textProperty().setValue("SAVED");
    }
    if (command.equals(Commands.UPDNICKNO)) {
      saveButton.textProperty().setValue("SAVED");
    }
  }
}
