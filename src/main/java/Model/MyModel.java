package Model;

import CorpusProcessing.*;
import javafx.util.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//import IR_engine.CorpusProcessing;

public class MyModel extends Observable implements IModel {

    private boolean stemming;
    private Indexer indexer;
    private static final int NUMBEROFDOCUMENTPROCESSORS = 4;
    private static final int NUMBEROFDOCUMENTPERPARSER = 4;
    private static final int POSTINGMERGERSPOOLSIZE = 2;


/*  setChanged();
    notifyObservers();*/

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

    @Override
    public boolean loadDictionary(String path) {
        this.indexer = new Indexer(Documenter.loadDictionary(path), path);
        if ((this.indexer != null)){
            return this.indexer.getDictionaryStatus();
        }
        return false;
    }

    @Override
    public boolean clear(String path) {
        return false;
    }

    @Override
    public boolean getDictionaryStatus() {
        return indexer.getDictionaryStatus();
    }

    @Override
    public String getDictionary() {
        StringBuilder stringBuilder = new StringBuilder();
        Map<String, Pair<Integer, String>> dictionary = this.indexer.getDictionary();
        for (Map.Entry<String, Pair<Integer, String>> entry : dictionary.entrySet()) {
            String key = entry.getKey();
            Pair<Integer, String> pair = entry.getValue();
            String outLine = key + "," + pair.getKey() + "," + pair.getValue() + "\n";
            stringBuilder.append(outLine);
        }
        return stringBuilder.toString();
    }

    @Override
    public void start(String corpusPath, String resultPath) {
        double startTime  = System.currentTimeMillis();


        if (!testPath(corpusPath) || !testPath(resultPath)) {
            setChanged();
            notifyObservers("Bad input"); //TODO: Maybe replace with enum
        }

        //From now on the paths are assumed to be valid
        Documenter.start(resultPath);

        //initializing the stop words set
        Parse.loadStopWords(corpusPath + "\\stop-words");
        File Corpus = new File(corpusPath + "\\corpus");
        File[] directories = Corpus.listFiles();

        //ExecutorService documentProcessorsPool = Executors.newFixedThreadPool(NUMBEROFDOCUMENTPROCESSORS); //FIXME:MAGIC NUMBER
        Thread[] threads = new Thread[NUMBEROFDOCUMENTPROCESSORS];
        RunnableParse[] runnableParses = new RunnableParse[NUMBEROFDOCUMENTPROCESSORS];


        generatePostingFilesParallel(directories, threads, runnableParses, resultPath );
        /*
        System.out.println("Start Parsing");
        generatePostingEntriesParallel(directories, threads, runnableParses);

        double endParseTimer = System.currentTimeMillis();
        System.out.println("End Parsing: "+ (endParseTimer-startTime)/1000);
        */

        //merge all the parsers from the RunnableParse
        HashSet<String> allSingleAppearanceEntities = getExcludedEntitiesAndSaveEntities(runnableParses);

        //merge all the indexers from the RunnableParse
        this.indexer = new Indexer(resultPath);

        // merge all posting files within each directory
        this.mergeAllPostingFiles(resultPath , runnableParses, allSingleAppearanceEntities);

        //finished with "threads" and "runnableParses" we can delete them now.
        threads = null;
        runnableParses = null;
        System.gc(); // CHECK IF NEEDED

        Documenter.saveDictionary(this.indexer.getDictionary());

        // now we have single posting file in each directory and we have a dictionary

        //Closing all open ends
        Documenter.shutdown();

    }

    private void generatePostingFilesParallel(File[] directories, Thread[] threads, RunnableParse[] runnableParses , String resultPath) {

        int currentDirectoryIndex = 0;
        for (int i = 0; i < threads.length; i++) {
            RunnableParse runnableParse = new RunnableParse(resultPath, stemming);

            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER));
            runnableParses[i] = runnableParse;
            currentDirectoryIndex = currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER;

            threads[i] = new Thread(runnableParse);
            threads[i].start();
        }
        while (currentDirectoryIndex < directories.length - NUMBEROFDOCUMENTPERPARSER) {
            int finishedThreadIndex = getFinishedThreadIndex(threads);
            RunnableParse runnableParse = runnableParses[finishedThreadIndex];

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

        return multipleAndUniqueEntities[1];
    }


    private HashSet<String>[] getUniqueAndDuplicatedEntitiesSets(LinkedList<String> singleAppearanceEntitiesList) {
        HashSet<String> uniqueEntities = new HashSet<>();
        HashSet<String> duplicatedEntities = new HashSet<>();

        for (String currentEntity : singleAppearanceEntitiesList) {
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


    public boolean isStemming() {
        return stemming;
    }

    /**
     * Receive resultPath mergers all the posting files of all the posting files directories.
     * @param resultPath
     * @param singleAppearanceEntities
     */
    private void mergeAllPostingFiles(String resultPath, RunnableParse[] runnableParses, HashSet<String> singleAppearanceEntities) {
        Map<String, Pair<Integer, String>>[] dictionaries = getAllDictionaries(runnableParses);

        //Merge all individual dictionaries
        for(Map<String, Pair<Integer, String>> dictionary : dictionaries){
            this.indexer.addPartialDictionary(dictionary);
        }

        //The indexer have a single unified dictionary
        this.indexer.removeAllSingleAppearances(singleAppearanceEntities);

        //Merge posting files
        int index= 0;
        //todo: add threads - parallel
        ExecutorService postingMergersPool = Executors.newFixedThreadPool(POSTINGMERGERSPOOLSIZE);
        ArrayList<Future> postingMergerFutures = new ArrayList<>();

        char startCharacter = '`';
        int invertedIndexDirectoriesCount = Indexer.getINVERTEDINDEXDIRECTORIESCOUNT();
        for (int i = 0; i < invertedIndexDirectoriesCount ; i++) {
            String path = resultPath + "\\PostingFiles\\" + (char)((int) startCharacter + i);
            postingMergerFutures.add(postingMergersPool.submit(new RunnableMerge(path , this.indexer.getDictionary())));
        }
        //waiting for threads to finish
        while(postingMergerFutures.size() > 0){
            if(postingMergerFutures.get(0).isDone())
            {
                postingMergerFutures.remove(0);
            }
            else
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        postingMergersPool.shutdown();
    }

    private Map<String, Pair<Integer, String>>[] getAllDictionaries(RunnableParse[] runnableParses) {
        Map<String, Pair<Integer, String>>[] allDictionaries = new TreeMap[NUMBEROFDOCUMENTPROCESSORS];

        for (int i = 0; i < runnableParses.length; i++) {
            allDictionaries[i] = runnableParses[i].getDictionary();
        }
        return allDictionaries;
    }

}



