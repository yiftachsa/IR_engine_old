package View;

import Test.TestsPart1;
import ViewModel.MyViewModel;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import java.io.File;
import java.util.*;

public class GUIController implements Observer {


    private MyViewModel viewModel;

    @FXML
    private TextField corpusText;
    @FXML
    private TextField resultText;
    @FXML
    private TextField queriesText;
    @FXML
    private TextField queryText;
    @FXML
    private CheckBox stemmingCheckBox;
    @FXML
    private CheckBox semanticCheckBox;

    /**
     * Sets the view model private field
     *
     * @param viewModel - MyViewModel
     */
    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }


    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {
            if ((arg).equals("Bad input")) {
                AlertBox.display("Wrong Input", "Wrong Input", "Please check your inputs and try again\n\n\n\n\n", "Close", "default background");
            }
        }
    }

    /**
     * Handles the "Clear" button. Forwards the path in "resultText" text box to the viewModel to
     * clear all the files related to the IR engine from the disk drive and the memory.
     * Displays the appropriate message for a successful deletion, wrong path or if  the files weren't found in the path given.
     */
    public void clearHandler() {
        String path = resultText.getText();
        if (path == null || path.isEmpty()) {
            AlertBox.display("Wrong Input", "Wrong Input", "Please check your inputs and try again\n\n\n\n\n", "Close", "default background");
        } else {
            if (!viewModel.clear(path)) {
                AlertBox.display("Clear Failed", "Clear", "Everything is already cleared\n\n\n\n\n", "Back to menu", "default background");
            } else {
                AlertBox.display("Succeed", "Succeed", "clear was successful\n\n\n\n\n", "Close", "default background");
            }
        }
    }

    /**
     * Handles the "displayDictionary" button. Displays the sorted dictionary with the total occurrences of these word in the corpus.
     */
    public void displayDictionaryHandler() {
        if (viewModel.getDictionaryStatus()) {
            LinkedList<Pair<String, Integer>> dictionaryToDisplay = viewModel.getDictionary();
            TableView.display("Dictionary", dictionaryToDisplay, "close");
        } else {
            AlertBox.display("Dictionary display failed", "Dictionary display failed", "\n\n\n\n\n", "Back to menu", "default background");
        }
    }

    /**
     * Handles the "Browse..." button in the corpus line.
     * Displays a folder selection window and updates the corpusText field according to the selection.
     *
     * @param event - ActionEvent - the button pressed
     */
    public void corpusBrowseHandler(ActionEvent event) {
        String path = browseDirectoryChooser(event);
        if (!path.equals("")) {
            corpusText.setText(path);
        }
    }

    /**
     * Handles the "Browse..." button in the corpus line.
     * Displays a folder selection window and updates the corpusText field according to the selection.
     *
     * @param event - ActionEvent - the button pressed
     */
    public void resultBrowseHandler(ActionEvent event) {
        String path = browseDirectoryChooser(event);
        if (!path.equals("")) {
            resultText.setText(path);
        }
    }

    /**
     * Handles the "Browse..." button in the queries line.
     * Displays a folder selection window and updates the queriesText field according to the selection.
     *
     * @param event - ActionEvent - the button pressed
     */
    public void queriesBrowseHandler(ActionEvent event) {
        String path = browseFileChooser(event);
        if (!path.equals("")) {
            queriesText.setText(path);
        }
    }

    /**
     * Displays a file selection window and returns the absolute path of the file chosen.
     *
     * @param event - ActionEvent - the button that was pressed
     * @return - String - the absolute path of the file chosen or an empty String
     */
    private String browseFileChooser(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        Button browseButton = (Button) event.getSource();
        Scene scene = browseButton.getScene();
        Stage stage = (Stage) scene.getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            return file.getAbsolutePath();
        }
        return "";
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

    /**
     * Handles th "stemming" checkbox. Forwards the selection to the viewModel.
     */
    public void stemmingHandler() {
        viewModel.stemmingSelection(stemmingCheckBox.isSelected());
    }

    /**
     * Handles the "Start" button.
     * checks if text boxes aren't empty, if they aren't sends their content to the viewModel,
     * if they are then it displays an error message for wrong inputs
     */
    public void startHandler() {
        if (!corpusText.getText().equals("") && !resultText.getText().equals("")) {
            AlertBox.display("Inverted Index Creation", "Inverted Index Creation", "The Inverted Index and the dictionary\nare being created.\nA message will be displayed when \nthe process has finished.\nPlease do not attempt to use the main \nmenu until the message appears \n\n\n\n", "Got it!", "default background");

            //Set timer
            double startTime = System.currentTimeMillis();
            viewModel.start(corpusText.getText(), resultText.getText());
            double endTime = System.currentTimeMillis();

            String time = "Total indexing time: " + (endTime - startTime) / 1000 + " sec\n";

            int uniqueTermsCount = viewModel.getUniqueTermsCount();
            String termsCount = "Total number of unique terms: " + uniqueTermsCount + "\n";

            int documentsProcessedCount = viewModel.getDocumentsProcessedCount();
            String documentsCount = "Total number of documents processed: " + documentsProcessedCount + "\n";


            AlertBox.display("Inverted Index Creation", "Inverted Index Created Successfully", "The Inverted Index and the dictionary\nwere successfully created\n\n" + time + termsCount + documentsCount, "    Yeah!\nBack to menu", "default background");
        } else {
            AlertBox.display("Wrong Inputs", "Wrong Inputs", "Please check your inputs and try again\n\n\n\n\n", "Back to menu", "default background");
        }
    }

    public void runQueryHandler(ActionEvent actionEvent) {
        String query = queryText.getText();
        Boolean preConditionsMet = true;
        if (query.isEmpty()) {
            AlertBox.display("Wrong Inputs", "Wrong Inputs", "Please check your inputs and try again.\n\t\tNo query was entered\n\n\n\n", "Back to menu", "default background");
            preConditionsMet = false;
        } else if (!viewModel.getDictionaryStatus()) {
            AlertBox.display("No indexing files", "No Indexing files", "Please check your inputs and try again.\n\tNo dictionary was loaded to memory\n\n\n\n", "Back to menu", "default background");
            preConditionsMet = false;
        } else if (!viewModel.getStopWordsStatus()) {
            if (!viewModel.loadStopWords(corpusText.getText())) {
                preConditionsMet = false;
                AlertBox.display("No stop words loaded", "No stop words loaded", "Please enter corpus path\nto load the stop words list.\n\n\n\n\n", "Back to menu", "default background");
            }
        }

        if (preConditionsMet) {
            ArrayList<Pair<String, ArrayList<String>>> rankedDocuments = viewModel.runQuery(query, semanticCheckBox.isSelected());

            RetrievalResultView.display("Single query results", rankedDocuments, viewModel);
        }

    }

    public void runQueriesHandler(ActionEvent event) {
        String queriesPath = queriesText.getText();
        Boolean preConditionsMet = true;

        if (queriesPath.isEmpty()) {
            AlertBox.display("Wrong Inputs", "Wrong Inputs", "Please check your inputs and try again\n\n\n\n\n", "Back to menu", "default background");
            preConditionsMet = false;
        } else if (!viewModel.getDictionaryStatus()) {
            AlertBox.display("No indexing files", "No Indexing files", "Please check your inputs and try again.\n\tNo dictionary was loaded to memory\n\n\n\n", "Back to menu", "default background");
            preConditionsMet = false;
        } else if (!viewModel.getStopWordsStatus()) {
            if (!viewModel.loadStopWords(corpusText.getText())) {
                AlertBox.display("No stop words loaded", "No stop words loaded", "Please enter corpus path\nto load the stop words list.\n\n\n\n\n", "Back to menu", "default background");
                preConditionsMet = false;
            }
        }
        if (preConditionsMet) {
            AlertBox.display("Running Queries", "Running Queries", "The queries are being evaluated\nA message will be displayed when \nthe process has finished.\nPlease do not attempt to use the main \nmenu until the message appears \n\n\n\n", "Got it!", "default background");

            ArrayList<Pair<String, ArrayList<String>>> rankedDocumentsNumbers = viewModel.runQueries(queriesPath, semanticCheckBox.isSelected());
            //TODO: Display results. TextField or plain alert box
            RetrievalResultView.display("Multiple queries results", rankedDocumentsNumbers, viewModel);

            //TODO: IMPORTANT - remember to associate each list of returned docs with the correct query ID
        }
    }

    /**
     * Handles the "load" button. sends the path in the "resultText: text box to the view model.
     * If the viewModel loads the dictionary to the memory successfully then displays a success message,
     * else, displays error message.
     *
     * @param event - ActionEvent - the button pressed
     */
    public void loadHandler(ActionEvent event) {

        String path = resultText.getText();
        if (path == null || path.equals("")) {
            AlertBox.display("Wrong Input", "Wrong Input", "Please check your inputs and try again\n\n\n\n\n", "Close", "default background");

        } else {
            if (viewModel.loadDictionary(path)) {
                AlertBox.display("Succeed", "Succeed", "Loading dictionary successful\n\n\n\n\n", "Close", "default background");
            } else {
                AlertBox.display("Failed", "Failed", "Please check your inputs and try again\n\n\n\n\n", "Close", "default background");
            }
        }
    }


    /**
     * Handles the "about" button. Displays an about window with the creators details.
     */
    public void aboutHandler() {
        String sAbout = "The Creators:\n" + "        Merav Shaked\n" + "        Yiftach Savransky";
        AlertBox.display("About", "About us:", sAbout, "Close", "default background");
    }


    /**
     * Closes the main stage.
     * Calls exitHandler to orderly close the application.
     *
     * @param event - ActionEvent - exit button was pressed
     */
    public void exit(ActionEvent event) {
        exitHandler(event);
    }

    /**
     * Handles the 'x' button being pressed.
     * Closes the application in an orderly fashion.
     *
     * @param event - Event
     */
    public void exitHandler(Event event) {
        //Decide whether to close the application based on the user input
        boolean closeWindow;
        closeWindow = ExitConfirmBox.display("confirmBoxButton", "Are you sure you \n want to exit?");
        //closing the main window - depends on the button used to close (exit button or the 'X' sign)
        if (closeWindow) {
            if (event instanceof WindowEvent) {
                //'X' was pressed
                close((WindowEvent) event);
            } else {
                //"exit" button was pressed
                close((ActionEvent) event);
            }
        }
    }

    /**
     * Closes a stage based on an ActionEvent
     *
     * @param actionEvent - ActionEvent - "exit" button was pressed
     */
    private static void close(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Closes a stage based on an WindowEvent
     *
     * @param windowEvent - WindowEvent - 'X' was pressed
     */
    private static void close(WindowEvent windowEvent) {
        Stage stage = (Stage) windowEvent.getSource();
        stage.close();
    }
}


