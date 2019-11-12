package MyView;

import ViewModel.MyViewModel;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class MyViewController implements Observer {


    private MyViewModel viewModel;
    private boolean alreadyFinished = false;




    private String audioPathStart = new File("resources/Audio/Come and get Your Love(Guardians of the Galaxy Intro song) - Redbone.mp3").toURI().toString();
    private String audioPathEnd = new File("resources/Audio/Electric Light Orchestra - Mr Blue Sky (Guardians of the Galaxy 2 Awesome Mix Vol. 2 ).mp3").toURI().toString();
    private MediaPlayer mediaPlayerStart = new MediaPlayer(new Media(audioPathStart));
    private MediaPlayer mediaPlayerEnd = new MediaPlayer(new Media(audioPathEnd));

    @FXML
    private BorderPane borderPane;

    private static boolean stemming=false;


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
        AlertBox.display("Clear","Clear succeeded", "All the indexing data have been deleted\n\n\n\n\n","Back to menu" , "default background" );
    }


    public void displayDictionaryHandler()
    {
        AlertBox.display("Dictionary","", "\n\n\n\n\n","Back to menu" , "default background" );
        //TODO: display dictionary!!!!!!!!
    }

    public void corpusBrowseHandler()
    {
        //TOdO: USE FILE CHOOSER TO RECEIVE THE PATH AND CHANGE THE TEXT NOX TEXT TO BE THE CHOSEN PATH
    }

    public void resultBrowseHandler()
    {
        //TODO: USE FILE CHOOSER TO RECEIVE THE PATH AND CHANGE THE TEXT NOX TEXT TO BE THE CHOSEN PATH
    }

    public void stemmingHandler()
    {
        //TODO: update the boolean stemming.
    }

    public void startHandler()
    {
        //TODO: fill the function!!!!!!!!
    }










    /**
     * Handles the "about" button
     */
    public void gameOver (){

        mediaPlayerStart.stop();
        mediaPlayerEnd .play();
        AlertBox.display("Win","YOU WON!!!", "Congratulations\n\n\n\n\n","Back to menu" ,"/Images/win.gif");
    }

    /**
     * Handles the "about" button
     */
    public void aboutHandler (){
        String sAbout = "The Creators:\n" + "        Merav Shaked\n" + "        Yiftach Savransky";
        AlertBox.display("About","About us:", sAbout,"Close" ,"default background");
    }
    /**
     * Handles the "instructions" button
     */
    public void instructionsHandler (){
        String sInstructions = "Move with:\n         4 - left\n         8 - up\n         2 - down\n         6 - right "+"\nOther keys: 7, 9 ,1 ,3\n"+"Input must be between\n"+"            5-1000";
        AlertBox.display("Instructions","Instructions", sInstructions,"Close" , "default background");
    }



    /**
     * Handles the "load" button.
     * @param event - ActionEvent
     */
    public void loadHandler(ActionEvent event) {
        FileChooser fileChooser =  new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Maze File" , "*.maze");
        fileChooser.getExtensionFilters().add(extensionFilter);
        MenuItem menuItem = (MenuItem) event.getSource();
        while(menuItem.getParentPopup() == null)
        {
            menuItem = menuItem.getParentMenu();
        }
        Scene scene = menuItem.getParentPopup().getOwnerWindow().getScene();
        Stage stage = (Stage) scene.getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if(file!=null)
        {
            alreadyFinished = false;
            mediaPlayerEnd.stop();
            mediaPlayerStart.play();
            viewModel.loadGame(file);
        }
    }

    /**
     * Handles the "Properties" button.
     * @param event - ActionEvent
     */
    public void propertiesHandler(ActionEvent event) {
        String sProperties = "";
        try(FileReader reader = new FileReader("resources//config.properties");
            BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while((line= bufferedReader.readLine()) != null)
            {
                sProperties = sProperties + line + "\n";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        AlertBox.display("Properties","Properties", sProperties,"Close" ,"default background");
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
                viewModel.shutdown();
                close((WindowEvent)event);
            }else{
                //"exit" button was pressed
                viewModel.shutdown();
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

    /**
     * sends the user input to the viewModel and return whether the input is good
     * @param firstInput - TextField
     * @param secondInput - TextField
     * @return - boolean
     */
    public boolean CheckInput(TextField firstInput, TextField secondInput) {
        return viewModel.CheckInput(firstInput , secondInput);
    }

}


