package View;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class RetrievalResultController {
    public void displayEntities(ActionEvent actionEvent) {
        //TODO: Maybe call alertBox to display entities
    }

    public void saveResults(ActionEvent actionEvent) {
        String path = browseDirectoryChooser(actionEvent);


    }


    /**
     * Displays a folder selection window and returns the absolute path of the folder chosen.
     *
     * @param event - ActionEvent - the button that was pressed
     * @return - String - the absolute path of the directory chosen or an empty String
     */
    private String browseDirectoryChooser(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Button browseButton = (Button) event.getSource();
        Scene scene = browseButton.getScene();
        Stage stage = (Stage) scene.getWindow();
        File file = directoryChooser.showDialog(stage);
        if (file != null) {
            return file.getAbsolutePath();
        }
        return "";
    }
}
