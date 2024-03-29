package Model;

import java.util.Map;

public interface IModel {

    /**
     * Receives the "Stemming" checkbox state and sets its value
     * @param selected - boolean - the "Stemming" checkbox state
     */
    void setStemming(boolean selected);

    /**
     *Receives the path of the dictionary from the user and loads it to the memory.
     * @param path - String - the path of the dictionary
     * @return - boolean - true if the loading completed successfully, else false
     */
    boolean loadDictionary(String path);

    /**
     *  Clears all the files related to the IR engine.
     * @param path - String - the path of the folder
     * @return - boolean - true if deleting  the files completed successfully, else false
     */
    boolean clear(String path);

    /**
     * Checks if the dictionary is already loaded to the memory.
     * @return - boolean - true if the dictionary is loaded to the memory in the model, else false
     */
    boolean getDictionaryStatus();

    /**
     * Indexes the given corpus from the "corpusPath" and saves the index files (dictionary and posting files) in the "resultPath"
     * @param corpusPath - String - "corpusPath" text field content
     * @param resultPath - String - "resultPath" text field content
     */
    void start(String corpusPath, String resultPath);

    String getDictionary();
    //FIXME:expend the JavaDocs
}
