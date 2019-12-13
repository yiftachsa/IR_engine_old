package CorpusProcessing;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RunnableParse implements Runnable {

    private static final int MERGERSPOOLSIZE = 2;
    private HashSet<String> entities;
    private HashSet<String> singleAppearanceEntities;
    private File[] filesToParse;
    private Parse parser;

    public HashSet<String> getEntities() {
        return entities;
    }

    public HashSet<String> getSingleAppearanceEntities() {
        return singleAppearanceEntities;
    }

    public RunnableParse(HashSet<String> entities, HashSet<String> singleAppearanceEntities, boolean useStemmer) {
        this.entities = entities;
        this.singleAppearanceEntities = singleAppearanceEntities;
        this.parser = new Parse(entities, singleAppearanceEntities, useStemmer);
    }

    public void setFilesToParse(File[] filesToParse) {
        this.filesToParse = filesToParse;
    }

    @Override
    public void run() {
        double startTime = System.currentTimeMillis()/1000;
        String timePrint = "Thread: "+Thread.currentThread().getId()+" StartTime: "+startTime;


        ArrayList<ArrayList<Trio>> entirePostingEntries = new ArrayList<>();
        ExecutorService mergersPool = Executors.newFixedThreadPool(MERGERSPOOLSIZE);
        ArrayList<Future<ArrayList<Trio>>> futures = new ArrayList<>();

        for (File directory : filesToParse) {
            String filePath = directory.listFiles()[0].getAbsolutePath();
            if (Files.isReadable(Paths.get(filePath))) {
                ArrayList<Document> documents = CorpusProcessing.ReadFile.separateFileToDocuments(filePath);

                ArrayList<ArrayList<Trio>> postingEntriesListsOfFile = new ArrayList<>();

                for (Document document : documents) {
                    ArrayList<String> bagOfWords = parser.parseDocument(document);
                    ArrayList<Trio> postingsEntries = Mapper.processBagOfWords(document.getId(), bagOfWords);
                    //TODO: check if the function add create a new object in memory - in that case , we should delete the original postingsEntries.
                    postingEntriesListsOfFile.add(postingsEntries);
                }
                while (postingEntriesListsOfFile.size() > 1) {
                    postingEntriesListsOfFile.add(Mapper.mergeAndSortTwoPostingEntriesLists(postingEntriesListsOfFile.remove(0), postingEntriesListsOfFile.remove(0)));
                }
                //insert all posting entries of the file to entirePostingEntries
                entirePostingEntries.add(postingEntriesListsOfFile.get(0));
                //check if we can merge two posting list to one
                if (entirePostingEntries.size() >= 2) {
                    Future<ArrayList<Trio>> future = mergersPool.submit(new CallableMerge(entirePostingEntries.remove(0), entirePostingEntries.remove(0)));
                    futures.add(future);
                }
                //Getting result from callableMerge
                if (futures.size() > 0) {
                    if (futures.get(0).isDone()) {
                        Future<ArrayList<Trio>> future = futures.remove(0);
                        try {
                            entirePostingEntries.add(future.get());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                //merge postingEntriesListsOfFile into entirePostingEntries

                //entirePostingEntries=(Mapper.mergeAndSortTwoPostingEntriesLists(postingEntriesListsOfFile.get(0),entirePostingEntries));

            }
        }
        for (Future<ArrayList<Trio>> future : futures) {
            while (!future.isDone()) ;
            try {
                entirePostingEntries.add(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mergersPool.shutdown();
        while (entirePostingEntries.size() > 1) {
            entirePostingEntries.add(Mapper.mergeAndSortTwoPostingEntriesLists(entirePostingEntries.remove(0), entirePostingEntries.remove(0)));
        }

        double endTime = System.currentTimeMillis()/1000;
        timePrint = timePrint+" EndTime: " +endTime + " Total: "+(endTime-startTime);
        System.out.println(timePrint);

        Documenter.savePostingEntries(entirePostingEntries);
    }


    public void saveAndClearEntitiesSets(){
        Documenter.saveEntitiesSets(this.entities, this.singleAppearanceEntities);
        this.entities = new HashSet<>();
        this.singleAppearanceEntities = new HashSet<>();
    }
}
