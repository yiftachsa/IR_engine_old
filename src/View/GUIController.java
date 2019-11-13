package View;

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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class GUIController implements Observer {


    private MyViewModel viewModel;

    @FXML
    private TextField corpusText;
    @FXML
    private TextField resultText;
    @FXML
    private CheckBox stemmingCheckBox;

    private static boolean stemming=false; //TODO: Move to model


    /**
     * sets the view model private field
     * @param viewModel - MyViewModel
     */
    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }



    @Override
    public void update(Observable o, Object arg) {
        if (o == viewModel) {

        }
    }

    public void clearHandler(){


        String path = resultText.getText();
        if(path == null || path== "")
        {
            AlertBox.display("Wrong Input","Wrong Input", "Please check your inputs and try again\n\n\n\n\n","Close" , "default background");

        }
        else
        {
            if(!viewModel.clear(path)){

                AlertBox.display("Clear", "Clear", "Everything is already cleared\n\n\n\n\n", "Back to menu", "default background");
            }
            else
            {
                AlertBox.display("Succeed","Succeed", "clear was successful\n\n\n\n\n","Close" , "default background");
            }
        }
    }


    public void displayDictionaryHandler()
    {
        if(viewModel.getDictionaryStatus())
        {
            String dictionaryToDisplay = viewModel.getDictionary();
            AlertBox.display("Dictionary","", "\n\n\n\n\n","Back to menu" , "default background" );

        }
        else
        {
            AlertBox.display("Dictionary display failed","Dictionary display failed", "\n\n\n\n\n","Back to menu" , "default background" );
        }
               //TODO: display dictionary!!!!!!!!
    }

    public void corpusBrowseHandler(ActionEvent event)
    {
        String path = browse(event);
        if (path != "") {
            corpusText.setText(path);
        }
    }

    public void resultBrowseHandler(ActionEvent event)
    {
        String path = browse(event);
        if(path != "") {
            resultText.setText(path);
        }
    }

    private String browse(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Button browseButton = (Button) event.getSource();
        Scene scene = browseButton.getScene();
        Stage stage = (Stage) scene.getWindow();
        File file = directoryChooser.showDialog(stage);
        if (file != null) {
            String path = file.getAbsolutePath();
            return path;
        }
        return "";
    }

    public void stemmingHandler()
    {
        //TODO: finish handling.
        viewModel.stemmingSelection(stemmingCheckBox.isSelected());
    }

    public void startHandler()
    {
        if (!corpusText.getText().equals("") && !resultText.getText().equals("")){
            //Do something - send paths to viewModel, THE MODEL should know if a dictionary was loaded already
        }else {
            AlertBox.display("Wrong Inputs","Wrong Inputs", "Please check your inputs and try again\n\n\n\n\n","Back to menu" , "default background" );
        }
        //TODO: fill the function!!!!!!!!
    }

    /**
     * Handles the "load" button.
     * @param event - ActionEvent
     */
    public void loadHandler(ActionEvent event) {

        String path = resultText.getText();
        if(path == null || path== "")
        {
            AlertBox.display("Wrong Input","Wrong Input", "Please check your inputs and try again\n\n\n\n\n","Close" , "default background");

        }
        else
        {
            if(viewModel.loadDictionary(path)) {
                AlertBox.display("Succeed","Succeed", "Loading dictionary successful\n\n\n\n\n","Close" , "default background");
            }
            else
            {
                AlertBox.display("Failed","Failed", "Please check your inputs and try again\n\n\n\n\n","Close" , "default background");
            }
        }
    }


    /**
     * Handles the "about" button
     */
    public void aboutHandler (){
        String sAbout = "The Creators:\n" + "        Merav Shaked\n" + "        Yiftach Savransky";
        AlertBox.display("About","About us:", sAbout,"Close" ,"default background");
    }


    /**
     * Closes the main stage.
     * Calls exitHandler to orderly close the application.
     * @param event - ActionEvent - exit button was pressed
     */
    public void exit(ActionEvent event){
        exitHandler(event);
    }

    /**
     * Handles the 'x' button being pressed.
     * Closes the application in an orderly fashion.
     * @param event - Event
     */
    public void exitHandler(Event event)
    {
        //Decide whether to close the application based on the user input
        Boolean closeWindow = true;
        closeWindow = ExitConfirmBox.display("confirmBoxButton","Are you sure you \n want to exit?");
        //closing the main window - depends on the button used to close (exit button or the 'X' sign)
        if (closeWindow){
            if(event instanceof WindowEvent){
                //'X' was pressed
                close((WindowEvent)event);
            }else{
                //"exit" button was pressed
                close((ActionEvent) event);
            }
        }
    }

    /**
     * Closes a stage based on an ActionEvent
     * @param actionEvent - ActionEvent - "exit" button was pressed
     */
    private static void close (ActionEvent actionEvent){
        Node source = (Node) actionEvent.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }

    /**
     * Closes a stage based on an WindowEvent
     * @param windowEvent - WindowEvent - 'X' was pressed
     */
    private static void close (WindowEvent windowEvent){
        Stage stage  = (Stage) windowEvent.getSource();
        stage.close();
    }

}


