package Model;

import CorpusProcessing.*;
import javafx.util.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MyModel extends Observable implements IModel {

    private boolean stemming;
    private Indexer indexer;
    /**
     * The number of the parallel threads processing the files.
     */
    private static final int NUMBEROFDOCUMENTPROCESSORS = 4;
    /**
     * The number of documents being processed at once by a single thread.
     */
    private static final int NUMBEROFDOCUMENTPERPARSER = 4;
    /**
     * The number of the parallel threads merging the posting files.
     */
    private static final int POSTINGMERGERSPOOLSIZE = 3;

    /**
     * Constructor
     */
    public MyModel() {
        stemming = false;
    }

    @Override
    public void setStemming(boolean selected) {
        stemming = selected;
    }

    /**
     * Returns the result directory path based on the stemming selection.
     * A different directory will be created for each option.
     *
     * @param path - String - given path
     * @return - String - result directory path
     */
    private String getResultPath(String path) {
        if (stemming) {
            path = path + "\\Stemmed";
        } else {
            path = path + "\\UnStemmed";
        }
        return path;
    }


    @Override
    public boolean loadDictionary(String path) {
        path = getResultPath(path);

        Map<String, DictionaryEntryTrio> dictionary = Documenter.loadDictionary(path);
        TreeSet<String> entities = Documenter.loadEntities(path);
        if (dictionary == null || entities == null) {
            return false;
        }
        this.indexer = new Indexer(dictionary, entities);
        if ((this.indexer != null)) {
            return this.indexer.getDictionaryStatus();
        }
        return false;
    }

    @Override
    public boolean clear(String resultPath) {
        this.indexer = new Indexer();
        return Documenter.deleteIndexingFilesFromDirectory(resultPath);
    }

    @Override
    public boolean getDictionaryStatus() {
        if (this.indexer != null) {
            return indexer.getDictionaryStatus();
        }
        return false;
    }

    @Override
    public LinkedList<Pair<String, Integer>> getDictionary() {
        LinkedList<Pair<String, Integer>> resultDictionary = new LinkedList<>();
        Map<String, DictionaryEntryTrio> dictionary = this.indexer.getDictionary();
        for (Map.Entry<String, DictionaryEntryTrio> entry : dictionary.entrySet()) {
            String term = entry.getKey();
            DictionaryEntryTrio dictionaryEntryTrio = entry.getValue();
            Pair<String, Integer> pair = new Pair<>(term, dictionaryEntryTrio.getCumulativeFrequency());
            resultDictionary.add(pair);
        }
        return resultDictionary;
    }

    @Override
    public void start(String dataPath, String resultPath) {
        //Checking paths
        if (!testPath(dataPath) || !testPath(resultPath)) {
            setChanged();
            notifyObservers("Bad input");
        }

        //From now on the paths are assumed to be valid
        resultPath = getResultPath(resultPath);
        String stopwordsPath = dataPath + "\\stop_words.txt";
        String corpusPath = dataPath + "\\corpus";


        //Initializing the Documenter
        Documenter.start(resultPath);
        //Initializing the stop words set
        if(!Parse.getStopwordsStatus()) {
            if (!Parse.loadStopWords(stopwordsPath)) {
                setChanged();
                notifyObservers("Bad input");
            }
        }
        //Initializing this.indexer
        this.indexer = new Indexer();

        File Corpus = new File(corpusPath);
        File[] directories = Corpus.listFiles();

        RunnableParse[] runnableParses = new RunnableParse[NUMBEROFDOCUMENTPROCESSORS];
        //Start the parallel processing and indexing of the files;
        generatePostingFilesParallel(directories, runnableParses, resultPath);

        //merge all the parsers from the RunnableParse
        HashSet<String> allSingleAppearanceEntities = getExcludedEntitiesAndSaveEntities(runnableParses);

        int totalDocumentsCount = getTotalDocumentsCount(runnableParses);
        this.indexer.setDocumentsCount(totalDocumentsCount);

        // merge all posting files within each directory - Parallel
        this.mergeAllPostingFiles(resultPath, runnableParses, allSingleAppearanceEntities);

        //finished with "runnableParses" we can call garbage collector to delete (just in case its still in the memory).
        runnableParses = null;
        System.gc();

        Documenter.saveDictionary(this.indexer.getDictionary());

        // now we have single posting file in each directory and we have a dictionary

        //Closing all open ends
        Documenter.shutdown();

    }

    /**
     * Sums all the documents counters from all the runnable parses
     *
     * @param runnableParses - RunnableParse[]
     * @return - int - total
     */
    private int getTotalDocumentsCount(RunnableParse[] runnableParses) {
        int total = 0;
        for (int i = 0; i < runnableParses.length; i++) {
            total = total + runnableParses[i].getDocumentsCount();
        }
        return total;
    }

    /**
     * Generates the temporary posting files and the partial dictionaries.
     * Initializes the given RunnableParse[] and assigns each RunnableParse documents to parse from "directories"
     * Saves the generated posting files to the given "resultPath"
     *
     * @param directories    - File[] - the directories containing the files that will be index.
     * @param runnableParses - RunnableParse[] - will be used for further processing (dictionary creation)
     * @param resultPath     - String - the path that the posting files will be written to.
     */
    private void generatePostingFilesParallel(File[] directories, RunnableParse[] runnableParses, String resultPath) {
        Thread[] threads = new Thread[runnableParses.length];
        int currentDirectoryIndex = 0;

        //Initialize each RunnableParse and begin its first documents processing
        for (int i = 0; i < threads.length; i++) {
            RunnableParse runnableParse = new RunnableParse(stemming);
            //Assigning files to a RunnableParse
            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER));

            runnableParses[i] = runnableParse;
            currentDirectoryIndex = currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER;

            threads[i] = new Thread(runnableParse);
            threads[i].start();
        }

        //Assigning all the files in given "directories"
        while (currentDirectoryIndex < directories.length - NUMBEROFDOCUMENTPERPARSER) {
            int finishedThreadIndex = getFinishedThreadIndex(threads);
            RunnableParse runnableParse = runnableParses[finishedThreadIndex];
            //Assigning files to a RunnableParse
            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER));

            threads[finishedThreadIndex] = new Thread(runnableParse);
            threads[finishedThreadIndex].start();
            currentDirectoryIndex = currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER;
        }

        //parsing the last files
        int numberOfDocumentsLeft = directories.length - currentDirectoryIndex;
        if (numberOfDocumentsLeft > 0) {
            int finishedThreadIndex = getFinishedThreadIndex(threads);
            RunnableParse runnableParse = runnableParses[finishedThreadIndex];
            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + numberOfDocumentsLeft));
            threads[finishedThreadIndex] = new Thread(runnableParse);
            threads[finishedThreadIndex].start();
        }

        //Waiting for all the threads to finished
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Receives all the runnableParses that finished creating the temporary posting files and the partial dictionary,
     * saves a unified entities list and returns a list of all the single appearances entities.
     *
     * @param runnableParses - RunnableParse[] - runnableParses that finished processing the initial processing of the files
     * @return - HashSet<String> - a list of all the single appearances entities.
     */
    private HashSet<String> getExcludedEntitiesAndSaveEntities(RunnableParse[] runnableParses) {
        TreeSet<String> entitiesTreeSet = new TreeSet<>();
        LinkedList<String> singleAppearanceEntitiesList = new LinkedList<>();

        for (int i = 0; i < runnableParses.length; i++) {
            entitiesTreeSet.addAll(runnableParses[i].getEntities());
            singleAppearanceEntitiesList.addAll(runnableParses[i].getSingleAppearanceEntities());
        }

        //merge-sorting single entities
        HashSet<String>[] multipleAndUniqueEntities = getUniqueAndDuplicatedEntitiesSets(singleAppearanceEntitiesList);
        entitiesTreeSet.addAll(multipleAndUniqueEntities[0]);
        //Writing the entities
        Documenter.saveEntities(entitiesTreeSet);

        this.indexer.setEntities(entitiesTreeSet);

        return multipleAndUniqueEntities[1];
    }

    /**
     * Receives a list of Strings and returns SETs of the duplicated strings and of the ones that appear only once.
     *
     * @param StringList - LinkedList<String> StringList - list of strings
     * @return - HashSet<String>[] - [duplicated strings set, unique strings set]
     */
    private HashSet<String>[] getUniqueAndDuplicatedEntitiesSets(LinkedList<String> StringList) {
        HashSet<String> uniqueEntities = new HashSet<>();
        HashSet<String> duplicatedEntities = new HashSet<>();

        for (String currentEntity : StringList) {
            if (uniqueEntities.contains(currentEntity)) {
                duplicatedEntities.add(currentEntity);
            } else {
                uniqueEntities.add(currentEntity);
            }
        }

        uniqueEntities.removeAll(duplicatedEntities);

        HashSet<String>[] result = new HashSet[2];
        result[0] = duplicatedEntities;
        result[1] = uniqueEntities;
        return result;
    }

    /**
     * Receives Thread[] and returns the index of the thread that has finished.
     *
     * @param threads - Thread[] - array of threads, not null
     * @return - int - the index of the finished thread
     */
    private int getFinishedThreadIndex(Thread[] threads) {
        while (true) {
            for (int i = 0; i < threads.length; i++) {
                if (!threads[i].isAlive()) {
                    return i;
                }
            }
        }
    }

    /**
     * Verifies that the path is of a reachable directory.
     *
     * @param folderPath - String - an absolute path
     * @return - boolean - true if the solderPath is of a reachable directory, else false
     */
    private boolean testPath(String folderPath) {
        boolean isDirectory;
        try {
            File directory = new File(folderPath);
            isDirectory = directory.isDirectory();

        } catch (Exception e) {
            isDirectory = false;
        }
        return isDirectory;
    }

    /**
     * Mergers all the posting files of all the posting files directories from the given resultPath.
     * Merges the dictionaries from all the separate runnableParses.
     * Removes all the single appearance entities from the unified dictionary.
     *
     * @param resultPath               - String - the path of the temporary posting files
     * @param singleAppearanceEntities - HashSet<String> - a set of string to be removed from the dictionary.
     */
    private void mergeAllPostingFiles(String resultPath, RunnableParse[] runnableParses, HashSet<String> singleAppearanceEntities) {
        Map<String, DictionaryEntryTrio>[] dictionaries = getAllDictionaries(runnableParses);
        final int THREADSLEEPTIMER = 250; //in milliseconds

        //Merge all individual dictionaries
        for (Map<String, DictionaryEntryTrio> dictionary : dictionaries) {
            this.indexer.addPartialDictionary(dictionary);
        }

        //The indexer have a single unified dictionary
        this.indexer.removeAllSingleAppearances(singleAppearanceEntities);

        //Merge posting files
        ExecutorService postingMergersPool = Executors.newFixedThreadPool(POSTINGMERGERSPOOLSIZE);
        ArrayList<Future> postingMergerFutures = new ArrayList<>();

        char startCharacter = '`';
        int invertedIndexDirectoriesCount = Indexer.getINVERTEDINDEXDIRECTORIESCOUNT();
        for (int i = 0; i < invertedIndexDirectoriesCount; i++) {
            String path = resultPath + "\\PostingFiles\\" + (char) ((int) startCharacter + i);
            postingMergerFutures.add(postingMergersPool.submit(new RunnableMerge(path, this.indexer.getDictionary())));
        }

        //waiting for threads to finish
        while (postingMergerFutures.size() > 0) {
            if (postingMergerFutures.get(0).isDone()) {
                postingMergerFutures.remove(0);
            } else {
                try {
                    Thread.sleep(THREADSLEEPTIMER);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        postingMergersPool.shutdown();
    }

    /**
     * Returns an array of all the dictionaries from the given RunnableParses.
     *
     * @param runnableParses - RunnableParse[] - runnableParses that finished processing the initial processing of the files
     * @return - Map<String, DictionaryEntryTrio>[] - array of dictionaries
     */
    private Map<String, DictionaryEntryTrio>[] getAllDictionaries(RunnableParse[] runnableParses) {
        Map<String, DictionaryEntryTrio>[] allDictionaries = new TreeMap[NUMBEROFDOCUMENTPROCESSORS];

        for (int i = 0; i < runnableParses.length; i++) {
            allDictionaries[i] = runnableParses[i].getDictionary();
        }
        return allDictionaries;
    }

    @Override
    public int getDocumentsProcessedCount() {
        return this.indexer.getDocumentsCount();
    }

    @Override
    public int getUniqueTermsCount() {
        return this.indexer.getDictionarySize();
    }
}



