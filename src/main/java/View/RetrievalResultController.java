package View;

import Model.IModel;
import ViewModel.MyViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
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
                    if (documentEntities.get(i) != null && !documentEntities.get(i).getKey().equals("null")) {
                        documentEntitiesToPrint = documentEntitiesToPrint +"Entity: "+ documentEntities.get(i).getKey() + " Rank: " + documentEntities.get(i).getValue()+ "\n";
                    }
                }
                if(documentEntitiesToPrint.length()>0) {
                    documentEntitiesToPrint = documentEntitiesToPrint.substring(0, documentEntitiesToPrint.length() - 1);
                }
                AlertBox.display("Document Entities", "Document Entities for Document\n\t" + documentNumber, documentEntitiesToPrint + "\n\n\n", "Close", "default background");
            }
        }
        //TODO: Maybe call alertBox to display entities
    }

    public void saveResults(ActionEvent actionEvent) {
        String path = browseFileChooser(actionEvent);
        viewModel.saveLatestRetrievalResults(path);
    }
    /**
     * Displays a file selection window and returns the absolute path of the file chosen.
     *
     * @param event - ActionEvent - the button that was pressed
     * @return - String - the absolute path of the file chosen or an empty String
     */
    private String browseFileChooser(ActionEvent event) {
        //TODO:FIXME
        FileChooser fileChooser = new FileChooser();
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        Button browseButton = (Button) event.getSource();
        Scene scene = browseButton.getScene();
        Stage stage = (Stage) scene.getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            return file.getAbsolutePath();
        }
        return "";
    }
}
