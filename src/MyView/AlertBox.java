package MyView;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class AlertBox {
    /**
     * Displays a simple alert box.
     * @param title - String - the stage title
     * @param subTitle - String - title in the scene
     * @param text - String - text in the middle of the scene
     * @param buttonText - String
     * @param backgroundImagePath - String - path to the background image
     */
    public static void display(String title, String subTitle, String text, String buttonText, String backgroundImagePath ) {
        Stage window = new Stage();
        window.setTitle(title);
        Parent root = null;
        try {
            root = FXMLLoader.load(AlertBox.class.getResource("MyView/AlertBox.fxml"));
        } catch (
                IOException e) {
            System.out.println("Couldn't open the \"ExitConfirmBox.fxml\" fxml file");
            e.printStackTrace();
        }

        Label lsubTitle = (Label) root.lookup("#subTitle");
        lsubTitle.setText(subTitle);

        Text tText = (Text) root.lookup("#text");
        tText.setText(text);

        Button closeButton = (Button) root.lookup("#closeButton");
        closeButton.setText(buttonText);
        closeButton.setOnAction(e-> window.close());

        VBox vBox = (VBox)root;
        if (!backgroundImagePath.equals("default background")){
            String backgroundConfig = "-fx-background-image: url("+backgroundImagePath+");";
            vBox.setStyle(backgroundConfig);
        }
        
        Scene scene = new Scene(root);
        window.setScene(scene);

        scene.getStylesheets().add(AlertBox.class.getResource("AlertBox.css").toExternalForm());


        window.showAndWait();
    }
}

