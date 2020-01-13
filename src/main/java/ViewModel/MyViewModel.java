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
     *
     * @param model- IModel
     */
    public MyViewModel(IModel model) {
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            setChanged();
            notifyObservers(arg);
        }
    }

    /**
     * Receives the "Stemming" checkbox state and forwards the selection to the model
     *
     * @param selected - boolean - the "Stemming" checkbox state
     */
    public void stemmingSelection(boolean selected) {
        model.setStemming(selected);
    }

    /**
     * Receives the path of the dictionary from the user and forwards it to the model.
     *
     * @param path - String - the path of the dictionary
     * @return - boolean - true if the loading completed successfully, else false
     */
    public boolean loadDictionary(String path) {
        return model.loadDictionary(path);
    }

    /**
     * Receives the path of the folder from which to clear all the files related
     * to the IR engine and forwards it to the model.
     *
     * @param path - String - the path of the folder
     * @return - boolean - true if deleting  the files completed successfully, else false
     */
    public boolean clear(String path) {
        return model.clear(path);
    }

    /**
     * Receives the dictionary status from the model.
     *
     * @return - boolean - true if the dictionary is loaded to the memory in the model, else false
     */
    public boolean getDictionaryStatus() {

        return model.getDictionaryStatus();
    }

    /**
     * Returns a String representation of the dictionary in memory.
     * If there is no dictionary loaded to the main memory the result will be null.
     * A dictionary MUST be loaded to the memory.
     *
     * @return - LinkedList<Pair<String,Integer>> - dictionary representation or null, if no dictionary is loaded.
     */
    public LinkedList<Pair<String, Integer>> getDictionary() {
        if (model.getDictionaryStatus()) {
            return model.getDictionary();
        }
        return null;
    }

    /**
     * Forwards the text fields from the user to the model.
     *
     * @param corpusPath - String - "corpusPath" text field content
     * @param resultPath - String - "resultPath" text field content
     */
    public void start(String corpusPath, String resultPath) {
        model.start(corpusPath, resultPath);
    }


    /**
     * Returns the number of the unique terms
     *
     * @return - int - the number of the unique terms
     */
    public int getUniqueTermsCount() {
        return model.getUniqueTermsCount();
    }

    /**
     * Returns the number of the documents processed
     *
     * @return - int - the number of the documents processed
     */
    public int getDocumentsProcessedCount() {
        return model.getDocumentsProcessedCount();
    }

    /**
     * Returns the relevant documents to a given query sorted by their relevance.
     *
     * @param query               - String - a phrase to retrieve relevant documents for.
     * @param useSemanticAnalysis - boolean - whether to use semantic analysis on the given query.
     * @return - ArrayList<Pair<String, ArrayList<String>>> - a list containing a pair of the query as key and the retrieved documents for it as a list.
     */
    public ArrayList<Pair<String, ArrayList<String>>> runQuery(String query, boolean useSemanticAnalysis) {
        return model.runQuery(query, useSemanticAnalysis);
    }

    /**
     * Returns the relevant documents to the queries in the given path, sorted by their relevance.
     *
     * @param queriesPath         - String - a path to the queries file.
     * @param useSemanticAnalysis - boolean - whether to use semantic analysis on the given query.
     * @return - ArrayList<Pair<String, ArrayList<String>>> - a list containing pairs of each query number as key and the retrieved documents for it as a list.
     */
    public ArrayList<Pair<String, ArrayList<String>>> runQueries(String queriesPath, boolean useSemanticAnalysis) {
        return model.runQueries(queriesPath, useSemanticAnalysis);
    }

    /**
     * Receives the stop words list status from the model.
     *
     * @return - boolean - true if the stop words list is loaded to the memory in the model, else false.
     */
    public boolean getStopWordsStatus() {
        return model.getStopWordsStatus();
    }

    /**
     * Loads a stop words file to memory from a given file path.
     * If a stop words list is already loaded returns true.
     *
     * @param path - String - path to a stop words file.
     * @return - boolean - true if a stop words list is loaded to memory.
     */
    public boolean loadStopWords(String path) {
        return model.loadStopWords(path);
    }

    /**
     * Checks if a given String is a valid document number
     *
     * @param documentNumber - String - a string to check
     * @return - boolean - true if a given String is a valid document number, else false
     */
    public boolean checkValidDocumentNumber(String documentNumber) {
        return model.checkValidDocumentNumber(documentNumber);
    }

    /**
     * Returns an sorted array of the entities, based on importance, in the document.
     *
     * @param documentNumber - String - a valid document number.
     * @return - ArrayList<Pair<String, Double>> - an sorted array of the entities, based on importance.
     */
    public ArrayList<Pair<String, Double>> getDocumentEntities(String documentNumber) {
        return model.getDocumentEntities(documentNumber);
    }

    /**
     * Orders the model to save the latest retrieval results to the given path.
     * If a file already exists in the destination path then overrides its.
     *
     * @param path - String - a path to save the latest retrieval results to.
     */
    public void saveLatestRetrievalResults(String path) {
        model.saveLatestRetrievalResults(path);
    }
}
