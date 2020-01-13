package View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ExitConfirmBox {

    private static Boolean answer;

    /**
     * returns the value of the answer private field
     *
     * @return - boolean
     */
    public static boolean getAnswer() {
        return answer;
    }

    /**
     * sets the value of the answer private field
     *
     * @param answer - Boolean
     */
    public static void setAnswer(Boolean answer) {
        ExitConfirmBox.answer = answer;
    }

    /**
     * Displays an exit window alert box.
     *
     * @param title   - String - the stage title
     * @param message - String - an exit message
     * @return - boolean - the user's choice
     */
    public static boolean display(String title, String message) {
        Stage window = new Stage();
        window.setTitle(title);
        Parent root = null;
        //loading the fxml file for the scene
        try {
            root = FXMLLoader.load(ExitConfirmBox.class.getClassLoader().getResource("ExitConfirmBox.fxml"));
        } catch (IOException e) {
            System.out.println("Couldn't open the \"ExitConfirmBox.fxml\" fxml file");
            e.printStackTrace();
        }
        window.initModality(Modality.APPLICATION_MODAL); //Blocking user interaction with other stages\windows until this window is closed

        //Setting the message
        Label lmessage = (Label) root.lookup("#message");
        lmessage.setText(message);

        Scene scene = new Scene(root);
        window.setScene(scene);
        window.showAndWait();

        return answer;
    }
}

