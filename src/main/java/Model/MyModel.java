package Model;

import CorpusProcessing.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.concurrent.*;

//import IR_engine.CorpusProcessing;

public class MyModel extends Observable implements IModel {

    private boolean stemming;
    private Indexer indexer;
    private static final int NUMBEROFDOCUMENTPROCESSORS = 4;

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
        indexer = new Indexer(resultPath);
        Documenter.setPath(resultPath);
        File Corpus = new File(corpusPath);
        File[] directories = Corpus.listFiles();
        int currentDirectoryIndex = 0;

        //ExecutorService documentProcessorsPool = Executors.newFixedThreadPool(NUMBEROFDOCUMENTPROCESSORS); //FIXME:MAGIC NUMBER
        Thread[] threads = new Thread[NUMBEROFDOCUMENTPROCESSORS];
        RunnableParse[] runnableParses = new RunnableParse[NUMBEROFDOCUMENTPROCESSORS];

        for (int i = 0; i < threads.length; i++) {
            HashSet<String> entities = new HashSet<>();
            HashSet<String> singleAppearanceEntities = new HashSet<>();
            RunnableParse runnableParse = new RunnableParse(entities, singleAppearanceEntities);
            threads[i] = new Thread(runnableParse);
            //threads[i].start();
        }

        for (int i = 0; i < runnableParses.length; i++) {
            runnableParses[i].setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + 8));
            threads[i].start();
            currentDirectoryIndex = currentDirectoryIndex + 8;
        }
        while (currentDirectoryIndex < directories.length - 1) {
            int finishedThreadIndex = getFinishedThreadIndex(threads);
            RunnableParse runnableParse = runnableParses[finishedThreadIndex];
            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + 8));
            threads[finishedThreadIndex] = new Thread(runnableParse);
            threads[finishedThreadIndex].start();
            currentDirectoryIndex = currentDirectoryIndex + 8;
        }
        //TODO::parse the last docs


        for (File directory : directories) {
            String filePath = directory.listFiles()[0].getAbsolutePath();
            if (Files.isReadable(Paths.get(filePath))) {
                ArrayList<Document> documents = CorpusProcessing.ReadFile.separateFileToDocuments(filePath);
                Parse.loadStopWords(corpusPath);

                ArrayList<ArrayList<Trio>> allPostingEntriesLists = new ArrayList<>();
                ExecutorService mergersPool = Executors.newFixedThreadPool(4); //FIXME:MAGIC NUMBER
                ArrayList<Future<ArrayList<Trio>>> futures = new ArrayList<>();

                for (Document document : documents) {
                    ArrayList<String> bagOfWords = Parse.parseDocument(document, stemming);
                    //TODO: check if the function add create a new object in memory - in that case , we should delete the original postingsEntries.
                    ArrayList<Trio> postingsEntries = Mapper.proceedBagOfWords(document.getId(), bagOfWords);
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

        //merge all the individuals posting entries and sort them
        Documenter.mergeAllPostingEntries();
        //now we have sorted posting entries files and we can iterate through them based on term name
        indexer.buildInvertedIndex();


    }

    private int getFinishedThreadIndex(Thread[] threads) {
        return 0; //FIXME
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



