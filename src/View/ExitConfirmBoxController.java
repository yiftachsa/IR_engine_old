package View;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ExitConfirmBoxController implements EventHandler<ActionEvent> {

    @FXML
    private Label message;

    /**
     * sets the answer returned from the window to True
     */
    public void yesHandle(){
        ExitConfirmBox.setAnswer(true);
    }
    /**
     * sets the answer returned from the window to False
     */
    public void noHandle(){
        ExitConfirmBox.setAnswer(false);
    }


    @Override
    public void handle(ActionEvent event) {
        String text = ((Button) event.getSource()).getText();
        if (text.equals("Yes")) {
            yesHandle();
        } else if (text.equals("No")) {
            noHandle();
        }

        Node source = (Node) event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }

}
