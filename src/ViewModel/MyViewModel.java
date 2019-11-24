package ViewModel;

import Model.IModel;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer {

    private IModel model;

    /**
     * Constructor
     * @param model- IModel
     */
    public MyViewModel(IModel model) {
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o==model){
            setChanged();
            notifyObservers(arg);
        }
    }

    /**
     * Receives the "Stemming" checkbox state and forwards the selection to the model
     * @param selected - boolean - the "Stemming" checkbox state
     */
    public void stemmingSelection(boolean selected) {
        model.setStemming(selected);
    }

    /**
     * Receives the path of the dictionary from the user and forwards it to the model.
     * @param path - String - the path of the dictionary
     * @return - boolean - true if the loading completed successfully, else false
     */
    public boolean loadDictionary(String path) {
        //TODO: load dictionary from path to object
        //TODO: check stemming!!!!
        return model.loadDictionary(path);

    }

    /**
     * Receives the path of the folder from which to clear all the files related
     * to the IR engine and forwards it to the model.
     * @param path - String - the path of the folder
     * @return - boolean - true if deleting  the files completed successfully, else false
     */
    public boolean clear(String path) {
        //TODO: clear the memory,posting,dictionary!
        //TODO"check if its already clear!
        return model.clear(path);
    }

    /**
     * Receives the dictionary status from the model.
     * @return - boolean - true if the dictionary is loaded to the memory in the model, else false
     */
    public boolean getDictionaryStatus() {

        return model.getDictionaryStatus();
    }

    /**
     *
     * @return
     */
    public String getDictionary() {

        return "";//(String)model.getDictionary();
    }

    /**
     * Forwards the text fields from the user to the model.
     * @param corpusPath - String - "corpusPath" text field content
     * @param resultPath - String - "resultPath" text field content
     */
    public void start(String corpusPath, String resultPath) {
        model.start(corpusPath , resultPath);
    }
}
