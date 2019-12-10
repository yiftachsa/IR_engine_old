package Model;

import CorpusProcessing.*;

import java.io.File;
import java.util.*;

//import IR_engine.CorpusProcessing;

public class MyModel extends Observable implements IModel {

    private boolean stemming;
    private Indexer indexer;
    private static final int NUMBEROFDOCUMENTPROCESSORS = 4;
    private static final int NUMBEROFDOCUMENTPERPARSER = 5;


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
    public void start(String corpusPath, String resultPath) {

        if (!testPath(corpusPath) || !testPath(resultPath)) {
            setChanged();
            notifyObservers("Bad input"); //TODO: Maybe replace with enum
        }
        //From now on the paths are assumed to be valid
        Documenter.setPath(resultPath);
        //initializing the stop words set
        Parse.loadStopWords(corpusPath);
        File Corpus = new File(corpusPath);
        File[] directories = Corpus.listFiles();
        int currentDirectoryIndex = 0;

        //ExecutorService documentProcessorsPool = Executors.newFixedThreadPool(NUMBEROFDOCUMENTPROCESSORS); //FIXME:MAGIC NUMBER
        Thread[] threads = new Thread[NUMBEROFDOCUMENTPROCESSORS];
        RunnableParse[] runnableParses = new RunnableParse[NUMBEROFDOCUMENTPROCESSORS];

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
            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER));
            threads[finishedThreadIndex] = new Thread(runnableParse);
            threads[finishedThreadIndex].start();
            currentDirectoryIndex = currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER;
        }
        int numberOfDocumentsLeft = NUMBEROFDOCUMENTPERPARSER - currentDirectoryIndex;
        if(numberOfDocumentsLeft > 0) {
            int finishedThreadIndex = getFinishedThreadIndex(threads);
            RunnableParse runnableParse = runnableParses[finishedThreadIndex];
            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + numberOfDocumentsLeft));
            threads[finishedThreadIndex] = new Thread(runnableParse);
            threads[finishedThreadIndex].start();
        }

        //merge all the parsers from the RunnableParse
        HashSet<String> allSingleAppearanceEntities = getExcludedEntitiesAndSaveEntitiesToFile(threads, runnableParses);

        //merge all the individuals posting entries and sort them
        Documenter.mergeAllPostingEntries();
        //now we have sorted posting entries files and we can iterate through them based on term name
        this.indexer = new Indexer(resultPath , allSingleAppearanceEntities);
        indexer.buildInvertedIndex();
/*
        for (File directory : directories) {
            String filePath = directory.listFiles()[0].getAbsolutePath();
            if (Files.isReadable(Paths.get(filePath))) {
                ArrayList<Document> documents = CorpusProcessing.ReadFile.separateFileToDocuments(filePath);

                ArrayList<ArrayList<Trio>> allPostingEntriesLists = new ArrayList<>();
                ExecutorService mergersPool = Executors.newFixedThreadPool(4); //FIXME:MAGIC NUMBER
                ArrayList<Future<ArrayList<Trio>>> futures = new ArrayList<>();

                for (Document document : documents) {
                    ArrayList<String> bagOfWords = Parse.parseDocument(document, stemming);
                    //TODO: check if the function add create a new object in memory - in that case , we should delete the original postingsEntries.
                    ArrayList<Trio> postingsEntries = Mapper.processBagOfWords(document.getId(), bagOfWords);
                    allPostingEntriesLists.add(postingsEntries);

                    if (allPostingEntriesLists.size() >= 2) {
                        Future<ArrayList<Trio>> future = mergersPool.submit(new CallableMerge(allPostingEntriesLists));
                        futures.add(future);
                    }
                    if (futures.size() > 0) {
                        if (futures.get(0).isDone()) {
                            Future<ArrayList<Trio>> future = futures.remove(0);
                            try {
                                allPostingEntriesLists.add(future.get());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                for (Future<ArrayList<Trio>> future : futures) {
                    while (!future.isDone()) ;
                    try {
                        allPostingEntriesLists.add(future.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mergersPool.shutdown();
                while (allPostingEntriesLists.size() > 1) {
                    allPostingEntriesLists.add(Mapper.mergeAndSortTwoPostingEntriesLists(allPostingEntriesLists.remove(0), allPostingEntriesLists.remove(0)));
                }
                Documenter.savePostingEntries(allPostingEntriesLists);

            }

        }
*/



    }

    private HashSet<String> getExcludedEntitiesAndSaveEntitiesToFile(Thread[] threads, RunnableParse[] runnableParses) {
        TreeSet<String> entitiesTreeSet = new TreeSet<>(); //TODO: Check if using a hashset and then sorting us quicker
        LinkedList<String> singleAppearanceEntitiesList = new LinkedList<>();

        for (int i = 0; i < threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            entitiesTreeSet.addAll(runnableParses[i].getEntities());
            singleAppearanceEntitiesList.addAll(runnableParses[i].getSingleAppearanceEntities());
        }
        //merge-sorting single entities
        HashSet<String> [] multipleAndUniqEntities = getMultipleAppearancesEntities(singleAppearanceEntitiesList);
        entitiesTreeSet.addAll(multipleAndUniqEntities[0]);
        //Writing the entities
        Documenter.saveEntities(entitiesTreeSet);
        //sorting the entities
        //TreeSet<String> sortedEntities = new TreeSet<>(entitiesHashSet);
        /*ArrayList<String> sortedEntities = new ArrayList<>(entitiesHashSet);
        Collections.sort(sortedEntities);*/
        return multipleAndUniqEntities[1];
    }


    private HashSet<String>[] getMultipleAppearancesEntities(LinkedList<String> singleAppearanceEntitiesList) {
        HashSet<String> uniqueEntities = new HashSet<>();
        HashSet<String> duplicatedEntities = new HashSet<>();
        for (int i = 0; i < singleAppearanceEntitiesList.size(); i++) {
            String currentEntity = singleAppearanceEntitiesList.get(i);
            if(uniqueEntities.contains(currentEntity)){
                duplicatedEntities.add(currentEntity);
            }else{
                uniqueEntities.add(currentEntity);
            }
        }
        uniqueEntities.removeAll(duplicatedEntities);
        HashSet<String> [] result = new HashSet[2];
        result[0] = duplicatedEntities;
        result[1] = uniqueEntities;
        return result;
    }

    private int getFinishedThreadIndex(Thread[] threads) {
        while(true){
            for (int i = 0; i < threads.length; i++) {
                if(!threads[i].isAlive()){
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



