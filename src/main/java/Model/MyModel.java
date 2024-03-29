package Model;

import CorpusProcessing.*;
import javafx.util.Pair;

import java.io.File;
import java.util.*;

//import IR_engine.CorpusProcessing;

public class MyModel extends Observable implements IModel {

    private boolean stemming;
    private Indexer indexer;
    private static final int NUMBEROFDOCUMENTPROCESSORS = 1;
    private static final int NUMBEROFDOCUMENTPERPARSER = 2;


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

        System.out.println("Start Parsing");
        generatePostingEntriesParallel(directories, threads, runnableParses);

        double endParseTimer = System.currentTimeMillis();
        System.out.println("End Parsing: "+ (endParseTimer-startTime)/1000);


        //merge all the parsers from the RunnableParse
        HashSet<String> allSingleAppearanceEntities = getExcludedEntitiesAndSaveEntities(threads, runnableParses);

        //TODO: finished with "threads" and "runnableParses" we can delete them now. CHECK IF NEEDED
        threads = null;
        runnableParses = null;
        System.gc(); // CHECK IF NEEDED

        //merge all the individuals posting entries and sort them
        HorizontalMerger.mergeAllPostingEntries();

        double endHorizontalMerger = System.currentTimeMillis();
        System.out.println("endHorizontalMerger: "+ (endHorizontalMerger-endParseTimer)/1000);

        //now we have sorted posting entries files and we can iterate through them based on term name
        this.indexer = new Indexer(resultPath, allSingleAppearanceEntities);
        indexer.buildInvertedIndex();

        //Closing all open ends
        Documenter.shutdown();
    }

    private void generatePostingEntriesParallel(File[] directories, Thread[] threads, RunnableParse[] runnableParses) {
        int currentDirectoryIndex = 0;
        //int quarterOfTheWay = directories.length/(NUMBEROFDOCUMENTPROCESSORS*4);

        for (int i = 0; i < threads.length; i++) {
            HashSet<String> entities = new HashSet<>();
            HashSet<String> singleAppearanceEntities = new HashSet<>();

            RunnableParse runnableParse = new RunnableParse(entities, singleAppearanceEntities, stemming);

            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER));
            runnableParses[i] = runnableParse;
            currentDirectoryIndex = currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER;

            threads[i] = new Thread(runnableParse);
            threads[i].start();
        }
        while (currentDirectoryIndex < directories.length - NUMBEROFDOCUMENTPERPARSER) {
            int finishedThreadIndex = getFinishedThreadIndex(threads);
            RunnableParse runnableParse = runnableParses[finishedThreadIndex];

//            if(currentDirectoryIndex>=quarterOfTheWay){ //FIXME:: Find a way to do it after every quarter
//                runnableParse.saveAndClearEntitiesSets();
//            }

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
        //
        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private HashSet<String> getExcludedEntitiesAndSaveEntities(Thread[] threads, RunnableParse[] runnableParses) {
        TreeSet<String> entitiesTreeSet = new TreeSet<>(); //TODO: Check if using a hashset and then sorting us quicker
        LinkedList<String> singleAppearanceEntitiesList = new LinkedList<>();

        for (int i = 0; i < threads.length; i++) {
            entitiesTreeSet.addAll(runnableParses[i].getEntities());
            singleAppearanceEntitiesList.addAll(runnableParses[i].getSingleAppearanceEntities());
        }
        //merge-sorting single entities
        HashSet<String>[] multipleAndUniqEntities = getUniqueAndDuplicatedEntitiesSets(singleAppearanceEntitiesList);
        entitiesTreeSet.addAll(multipleAndUniqEntities[0]);
        //Writing the entities
        Documenter.saveEntities(entitiesTreeSet);
        //sorting the entities
        //TreeSet<String> sortedEntities = new TreeSet<>(entitiesHashSet);
        /*ArrayList<String> sortedEntities = new ArrayList<>(entitiesHashSet);
        Collections.sort(sortedEntities);*/
        return multipleAndUniqEntities[1];
    }

//TODO:Change the name to getUniqueAndDuplicatedEntitiesSets
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

}

/*
    @Override
    public void saveGame(File file)
    {
        byte[] mazeByteArray = maze.toByteArray();
        saveMazeToFile(mazeByteArray, file.getAbsolutePath());
    }
    */
    /*

      Save a Maze to a file.
      @param decompressedMaze - byte[]
      @param mazeFileName - String

    private void saveMazeToFile(byte[] decompressedMaze, String mazeFileName) {
        // save maze to a file
        try {
            OutputStream fileOutputStream = new FileOutputStream(mazeFileName);
            OutputStream myCompressorOutputStream = new MyCompressorOutputStream(fileOutputStream);
            myCompressorOutputStream.write(decompressedMaze);
            fileOutputStream.close();
            myCompressorOutputStream.flush();
            myCompressorOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
   /*
    public void loadGame(File file)
    {
        //read maze from file

        try{
            byte savedMazeBytes[] = new byte[500000];
            InputStream fileInputStream = new FileInputStream( file.getAbsolutePath());
            InputStream myDecompressorInputStream = new MyDecompressorInputStream(fileInputStream);
            myDecompressorInputStream.read(savedMazeBytes);
            maze = new Maze(savedMazeBytes);
            currentCharacterPosition = maze.getStartPosition();
            myDecompressorInputStream.close();
            fileInputStream.close();
            solution=null;
            mazeChanged = true;
            characterChanged = true;
            solutionChanged=false;
            setChanged();
            notifyObservers();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
*/



