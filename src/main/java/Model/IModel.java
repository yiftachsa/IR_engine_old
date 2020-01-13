package Model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;

public interface IModel {

    /**
     * Receives the "Stemming" checkbox state and sets its value
     *
     * @param selected - boolean - the "Stemming" checkbox state
     */
    void setStemming(boolean selected);

    /**
     * Receives the path of the dictionary from the user and loads it to the memory.
     *
     * @param path - String - the path of the dictionary
     * @return - boolean - true if the loading completed successfully, else false
     */
    boolean loadDictionary(String path);

    /**
     * Clears all the files related to the IR engine.
     *
     * @param path - String - the path of the folder
     * @return - boolean - true if deleting  the files completed successfully, else false
     */
    boolean clear(String path);

    /**
     * Checks if the dictionary is already loaded to the memory.
     *
     * @return - boolean - true if the dictionary is loaded to the memory in the model, else false
     */
    boolean getDictionaryStatus();

    /**
     * Indexes the given corpus from the "corpusPath" and saves the index files (dictionary and posting files) in the "resultPath"
     *
     * @param corpusPath - String - "corpusPath" text field content
     * @param resultPath - String - "resultPath" text field content
     */
    void start(String corpusPath, String resultPath);

    /**
     * Returns a String representation of the dictionary in memory.
     * If there is no dictionary loaded to the main memory the result will be null.
     *
     * @return - LinkedList<Pair<String,Integer>> - dictionary representation or null, if no dictionary is loaded.
     */
    LinkedList<Pair<String, Integer>> getDictionary();


    /**
     * The dictionary size, the number of the unique terms
     *
     * @return - int -  the number of the unique terms
     */
    int getUniqueTermsCount();

    /**
     * Returns the number of the documents processed
     *
     * @return - int - the number of the documents processed
     */
    int getDocumentsProcessedCount();

    /**
     * Returns the relevant documents to a given query sorted by their relevance.
     *
     * @param query               - String - a phrase to retrieve relevant documents for.
     * @param useSemanticAnalysis - boolean - whether to use semantic analysis on the given query.
     * @return - ArrayList<Pair<String, ArrayList<String>>> - a list containing a pair of the query as key and the retrieved documents for it as a list.
     */
    ArrayList<Pair<String, ArrayList<String>>> runQuery(String query, boolean useSemanticAnalysis);

    /**
     * Returns the relevant documents to the queries in the given path, sorted by their relevance.
     *
     * @param queriesPath         - String - a path to the queries file.
     * @param useSemanticAnalysis - boolean - whether to use semantic analysis on the given query.
     * @return - ArrayList<Pair<String, ArrayList<String>>> - a list containing pairs of each query number as key and the retrieved documents for it as a list.
     */
    ArrayList<Pair<String, ArrayList<String>>> runQueries(String queriesPath, boolean useSemanticAnalysis);

    /**
     * Checks if the stop words list is already loaded to the memory.
     *
     * @return - boolean - true if the stop words list is loaded to the memory in the model, else false.
     */
    boolean getStopWordsStatus();

    /**
     * Loads a stop words file to memory from a given file path.
     * If a stop words list is already loaded returns true.
     *
     * @param path - String - path to a stop words file.
     * @return - boolean - true if a stop words list is loaded to memory.
     */
    boolean loadStopWords(String path);


    /**
     * Checks if a given String is a valid document number by comparing it with the pre-loaded documents details.
     *
     * @param documentNumber - String - a string to check.
     * @return - boolean - true if a given String is a valid document number, else false.
     */
    boolean checkValidDocumentNumber(String documentNumber);

    /**
     * Returns an sorted array of the entities, based on importance, in the document.
     *
     * @param documentNumber - String - a valid document number.
     * @return - String[] - an sorted array of the entities, based on importance.
     */
    ArrayList<Pair<String, Double>> getDocumentEntities(String documentNumber);

    /**
     * Saves the latest retrieval results to the given path.
     * If a file already exists in the destination path then overrides its.
     *
     * @param path - String - a path to save the latest retrieval results to.
     */
    void saveLatestRetrievalResults(String path);
}
