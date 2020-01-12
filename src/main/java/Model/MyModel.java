package Model;

import CorpusProcessing.*;
import javafx.util.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;


public class MyModel extends Observable implements IModel {

    private boolean stemming;
    private Indexer indexer;
    private Parse parse;
    private Searcher searcher;

    private ArrayList<Pair<String, ArrayList<String>>> latestQueryResult;
    /**
     * The number of the parallel threads processing the files.
     */
    private static final int NUMBEROFDOCUMENTPROCESSORS = 3;
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
        HashMap<String, HashMap<String, Integer>> allDocumentsEntities = Documenter.loadDocumentEntities(path);
        HashMap<String, String> documentDetails = Documenter.loadDocumentsDetailsFromFile(path);
        if (dictionary == null || entities == null || allDocumentsEntities == null || documentDetails == null) {
            return false;
        }
        this.indexer = new Indexer(dictionary, entities, allDocumentsEntities, documentDetails);
        if ((this.indexer != null)) {
            Documenter.setFilePath(path);
            return this.indexer.getDictionaryStatus();
        }
        return false;
    }

    @Override
    public boolean loadStopWords(String path) {
        String stopwordsPath = path + "\\stop_words.txt";
        if (!Parse.getStopwordsStatus()) {
            if (!Parse.loadStopWords(stopwordsPath)) {
                setChanged();
                notifyObservers("Bad input");
                return false;
            }
        }
        return true;
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
    public boolean getStopWordsStatus() {
        if (!Parse.getStopwordsStatus()) {
            return false;
        }
        return true;
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
        if (!testDirectoryPath(dataPath) || !testDirectoryPath(resultPath)) {
            setChanged();
            notifyObservers("Bad input");
        }

        //From now on the paths are assumed to be valid
        resultPath = getResultPath(resultPath);

        String corpusPath = dataPath + "\\corpus";


        //Initializing the Documenter
        Documenter.start(resultPath);
        //Initializing the stop words set
        loadStopWords(dataPath);
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

        indexer.setDocumentDetails(Documenter.getDocumentsDetails());

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
        HashMap<String, HashMap<String, Integer>> allDocumentsEntities = new HashMap<>();

        for (int i = 0; i < runnableParses.length; i++) {
            entitiesTreeSet.addAll(runnableParses[i].getEntities());
            singleAppearanceEntitiesList.addAll(runnableParses[i].getSingleAppearanceEntities());
            allDocumentsEntities.putAll(runnableParses[i].getDocumentsEntities());
        }

        //merge-sorting single entities
        HashSet<String>[] multipleAndUniqueEntities = getUniqueAndDuplicatedEntitiesSets(singleAppearanceEntitiesList);
        entitiesTreeSet.addAll(multipleAndUniqueEntities[0]);

        //Remove from allDocumentsEntities all the unique terms
        allDocumentsEntities = removeAllUniqueEntities(allDocumentsEntities, multipleAndUniqueEntities[1]);

        //Writing the entities
        Documenter.saveEntities(entitiesTreeSet);
        Documenter.saveDocumentEntities(allDocumentsEntities);

        this.indexer.setEntities(entitiesTreeSet);
        this.indexer.setDocumentEntities(allDocumentsEntities);


        return multipleAndUniqueEntities[1];
    }

    private HashMap<String, HashMap<String, Integer>> removeAllUniqueEntities(HashMap<String, HashMap<String, Integer>> allDocumentsEntities, HashSet<String> uniqueEntities) {

        HashMap<String, HashMap<String, Integer>> allDocumentsEntitiesUpdate = new HashMap<>();
        for (Map.Entry<String, HashMap<String, Integer>> mapEntry : allDocumentsEntities.entrySet()) {
            HashMap<String, Integer> documentEntities = mapEntry.getValue();
            HashMap<String, Integer> documentEntitiesUpdate = new HashMap<>();
            for (Map.Entry<String, Integer> entityEntry : documentEntities.entrySet()) {
                String entity = entityEntry.getKey();
                if (!uniqueEntities.contains(entity)) {
                    documentEntitiesUpdate.put(entity, entityEntry.getValue());
                }
            }
            allDocumentsEntitiesUpdate.put(mapEntry.getKey(), documentEntitiesUpdate);
        }
        return allDocumentsEntitiesUpdate;
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
     * @return - boolean - true if the folderPath is of a reachable directory, else false
     */
    private boolean testDirectoryPath(String folderPath) {
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
     * Verifies that the path is of a reachable txt file.
     *
     * @param filePath - String - an absolute path
     * @return - boolean - true if the filepath is of a txt readable file, else false
     */
    private boolean testFilePath(String filePath) {
        boolean isFile;
        try {
            File file = new File(filePath);
            isFile = file.isFile();
            String fileType = filePath.substring(filePath.length() - 4);
            if (!fileType.equals(".txt")) {
                isFile = false;
            }
        } catch (Exception e) {
            isFile = false;
        }
        return isFile;
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
        ReentrantLock sharedDictionaryMutex = new ReentrantLock();

        for (int i = 0; i < invertedIndexDirectoriesCount; i++) {
            String path = resultPath + "\\PostingFiles\\" + (char) ((int) startCharacter + i);
            postingMergerFutures.add(postingMergersPool.submit(new RunnableMerge(path, this.indexer.getDictionary() , sharedDictionaryMutex)));
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

    @Override
    public ArrayList<Pair<String, ArrayList<String>>> runQuery(String query, boolean useSemanticAnalysis) {

        if (searcher == null) {
            searcher = new Searcher(this.indexer, this.indexer.getDocumentsCount(),this.indexer.getAverageDocumentLength());
        }
        if (this.parse == null) {
            this.parse = new Parse(new HashSet<>(), new HashSet<>(), this.stemming);
        }

        if (useSemanticAnalysis) {
            SemanticAnalyzer semanticAnalyzer = SemanticAnalyzer.getInstance();
            query = semanticAnalyzer.expandQuery(query);
        }

        ArrayList<String> result = searcher.runQuery(query, this.indexer, this.parse);

        ArrayList<Pair<String, ArrayList<String>>> rankedDocumentsNumbers = new ArrayList<>();

        Random random = new Random();
        int queryIndex = random.nextInt(900)+100;

        rankedDocumentsNumbers.add(new Pair<>(queryIndex + "", result));

        setLatestQueryResult(rankedDocumentsNumbers);
        return rankedDocumentsNumbers;

    }


    @Override
    public ArrayList<Pair<String, ArrayList<String>>> runQueries(String queriesPath, boolean useSemanticAnalysis) {
        ArrayList<Pair<String, ArrayList<String>>> rankedDocuments = new ArrayList<>();
        if (!testFilePath(queriesPath)) {
            setChanged();
            notifyObservers("Bad input");
            return null;
        }
        Query[] queries = ReadFile.separateFileToQueries(queriesPath);
        for (int i = 0; i < queries.length; i++) {
            String queryTitle = queries[i].getTitle();
            String queryDescription = queries[i].getDescription();

            //Use semantic analysis only on the title.
            if (useSemanticAnalysis) {
                SemanticAnalyzer semanticAnalyzer = SemanticAnalyzer.getInstance();
                queryTitle = semanticAnalyzer.expandQuery(queryTitle);
            }

            String query = queryTitle + " " + queryDescription;
            ArrayList<String> currentQueryRankedDocuments = (runQuery(query, false)).get(0).getValue(); //Already used semantic analysis
            rankedDocuments.add(new Pair<>(queries[i].getNumber() + "", currentQueryRankedDocuments));
        }
        setLatestQueryResult(rankedDocuments);
        return rankedDocuments;
    }

    @Override
    public boolean checkValidDocumentNumber(String documentNumber) {
        return this.indexer.isValidDocumentNumber(documentNumber);
    }

    @Override
    public ArrayList<Pair<String, Double>> getDocumentEntities(String documentNumber) {
        HashMap<String, Integer> documentEntities = this.indexer.getDocumentEntitiesMap(documentNumber);
//      maxTermFrequency + "," + uniqTermsCount + "," + length + "," + documentDate+","+documentHeader
        String[] documentDetails = this.indexer.getDocumentDetails(documentNumber);

        String documentHeader = documentDetails[documentDetails.length-1];

        return searcher.rankEntities(documentEntities, documentHeader, this.parse);

    }

    private void setLatestQueryResult(ArrayList<Pair<String, ArrayList<String>>> rankedDocuments){
        this.latestQueryResult = rankedDocuments;
    }

    @Override
    public void saveLatestRetrievalResults(String path) {
        Documenter.saveRetrievalResults(path, this.latestQueryResult);
    }
}



