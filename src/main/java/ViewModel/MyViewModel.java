package ViewModel;

import Model.IModel;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
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
        return model.loadDictionary(path);
    }

    /**
     * Receives the path of the folder from which to clear all the files related
     * to the IR engine and forwards it to the model.
     * @param path - String - the path of the folder
     * @return - boolean - true if deleting  the files completed successfully, else false
     */
    public boolean clear(String path) {
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
     * Returns a String representation of the dictionary in memory.
     * If there is no dictionary loaded to the main memory the result will be null.
     * A dictionary MUST be loaded to the memory.
     * @return - LinkedList<Pair<String,Integer>> - dictionary representation or null, if no dictionary is loaded.
     */
    public LinkedList<Pair<String,Integer>> getDictionary() {
        if(model.getDictionaryStatus()) {
            return model.getDictionary();
        }
        return null;
    }

    /**
     * Forwards the text fields from the user to the model.
     * @param corpusPath - String - "corpusPath" text field content
     * @param resultPath - String - "resultPath" text field content
     */
    public void start(String corpusPath, String resultPath) {
        model.start(corpusPath , resultPath);
    }


    /**
     * Returns the number of the unique terms
     * @return - int - the number of the unique terms
     */
    public int getUniqueTermsCount() {
        return model.getUniqueTermsCount();
    }

    /**
     * Returns the number of the documents processed
     * @return - int - the number of the documents processed
     */
    public int getDocumentsProcessedCount() {
        return model.getDocumentsProcessedCount();
    }

    public ArrayList<String> runQuery(String query, boolean useSemanticAnalysis) {
        return model.runQuery(query, useSemanticAnalysis);
    }

    public ArrayList<Pair<String , ArrayList<String>>> runQueries(String queriesPath, boolean useSemanticAnalysis) {
        return model.runQueries(queriesPath,useSemanticAnalysis);
    }

    public boolean getStopWordsStatus() {
        return model.getStopWordsStatus();
    }

    public boolean loadStopWords(String path) {
        return model.loadStopWords(path);
    }
}
