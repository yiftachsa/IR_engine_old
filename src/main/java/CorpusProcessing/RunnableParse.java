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
        ArrayList<Trio> entirePostingEntries = new ArrayList<>();
        for (File directory : filesToParse) {
            String filePath = directory.listFiles()[0].getAbsolutePath();
            if (Files.isReadable(Paths.get(filePath))) {
                ArrayList<Document> documents = CorpusProcessing.ReadFile.separateFileToDocuments(filePath);

                ArrayList<ArrayList<Trio>> postingEntriesListsOfFile = new ArrayList<>();

                //ExecutorService mergersPool = Executors.newFixedThreadPool(4); //FIXME:MAGIC NUMBER
                //ArrayList<Future<ArrayList<Trio>>> futures = new ArrayList<>();

                for (Document document : documents) {
                    ArrayList<String> bagOfWords = parser.parseDocument(document);
                    ArrayList<Trio> postingsEntries = Mapper.processBagOfWords(document.getId(), bagOfWords);
                    //TODO: check if the function add create a new object in memory - in that case , we should delete the original postingsEntries.
                    postingEntriesListsOfFile.add(postingsEntries);
                }
                while (postingEntriesListsOfFile.size() > 1) {
                    postingEntriesListsOfFile.add(Mapper.mergeAndSortTwoPostingEntriesLists(postingEntriesListsOfFile.remove(0), postingEntriesListsOfFile.remove(0)));
                }
                //merge postingEntriesListsOfFile into entirePostingEntries
                entirePostingEntries=(Mapper.mergeAndSortTwoPostingEntriesLists(postingEntriesListsOfFile.get(0),entirePostingEntries));
            }
        }
        Documenter.savePostingEntries(entirePostingEntries);
                    /*
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

                */
    }
}
