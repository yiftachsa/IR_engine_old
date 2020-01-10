package View;

import Model.IModel;
import ViewModel.MyViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;

public class RetrievalResultController {

    private static MyViewModel viewModel;

    @FXML
    private TextField docNumberTextField;

    public static void setViewModel(MyViewModel newViewModel) {
        viewModel = newViewModel;
    }

    public void displayEntities(ActionEvent actionEvent) {
        String documentNumber = docNumberTextField.getText();
        if (!documentNumber.isEmpty()) {
            if (!viewModel.checkValidDocumentNumber(documentNumber)) {
                AlertBox.display("Wrong Input", "Wrong Input", "Please check your inputs and try again\n\n\n\n\n", "Close", "default background");
            } else {
                ArrayList<Pair<String, Double>> documentEntities = viewModel.getDocumentEntities(documentNumber);

                String documentEntitiesToPrint = "";
                for (int i = 0; i < documentEntities.size(); i++) {
                    if (documentEntities.get(i) != null && !documentEntities.get(i).getKey().isEmpty()) {
                        documentEntitiesToPrint = documentEntitiesToPrint +"Entity: "+ documentEntities.get(i).getKey() + " Rank: " + documentEntities.get(i).getValue()+ "\n";
                    }
                }
                documentEntitiesToPrint = documentEntitiesToPrint.substring(0, documentEntitiesToPrint.length() - 1);
                AlertBox.display("Document Entities", "Document Entities for Document\n\t" + documentNumber, documentEntitiesToPrint + "\n\n\n", "Close", "default background");
            }
        }
        //TODO: Maybe call alertBox to display entities
    }

    public void saveResults(ActionEvent actionEvent) {
        String path = browseDirectoryChooser(actionEvent);
        viewModel.saveLatestRetrievalResults(path);
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
