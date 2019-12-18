package CorpusProcessing;

import javafx.util.Pair;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RunnableParse implements Runnable {

    private static final int MERGERSPOOLSIZE = 2;
    private HashSet<String> entities;
    private HashSet<String> singleAppearanceEntities;
    private File[] filesToParse;
    private Parse parser;
    private Indexer indexer;


    public HashSet<String> getEntities() {
        return entities;
    }

    public HashSet<String> getSingleAppearanceEntities() {
        return singleAppearanceEntities;
    }

    public RunnableParse(String pathToPostingDirectories , boolean useStemmer) {
        this.entities =new HashSet<>();
        this.singleAppearanceEntities = new HashSet<>();
        this.indexer = new Indexer(pathToPostingDirectories);
        this.parser = new Parse(entities, singleAppearanceEntities, useStemmer);
    }

    public void setFilesToParse(File[] filesToParse) {
        this.filesToParse = filesToParse;
    }

    @Override
    public void run() {
        //FIXME: for debugging!!!
        double startTime = System.currentTimeMillis() / 1000;
        String timePrint = "Thread: " + Thread.currentThread().getId() + " StartTime: " + startTime;


        ArrayList<HashMap<String, Pair<String, Integer>>> postingEntriesListsOfFile = new ArrayList<>();

        for (File directory : filesToParse) {
            String filePath = directory.listFiles()[0].getAbsolutePath();
            if (Files.isReadable(Paths.get(filePath))) {
                ArrayList<Document> documents = CorpusProcessing.ReadFile.separateFileToDocuments(filePath);

                for (Document document : documents) {
                    HashMap<String, Pair<String, Integer>> bagOfWords = parser.parseDocument(document);
                    postingEntriesListsOfFile.add(bagOfWords);
                }

            }
            double endTime = System.currentTimeMillis() / 1000;
            timePrint = timePrint + " EndTime: " + endTime + " Total: " + (endTime - startTime);
            System.out.println(timePrint);

            //entirePostingEntries contains all the sorted trios from all the documents - per thread
            //build posting file for all the documents in the thread
        }
        this.indexer.buildInvertedIndex1(postingEntriesListsOfFile);
    }

    public void saveAndClearEntitiesSets(){
        Documenter.saveEntitiesSets(this.entities, this.singleAppearanceEntities);
        this.entities = new HashSet<>();
        this.singleAppearanceEntities = new HashSet<>();
    }

    public Map<String, Pair<Integer, String>> getDictionary() {
        return this.indexer.getDictionary();
    }
}
