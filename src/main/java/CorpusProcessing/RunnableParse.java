package CorpusProcessing;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RunnableParse implements Runnable {

    private static final int MERGERSPOOLSIZE = 2;
    private HashSet<String> entities;
    private HashSet<String> singleAppearanceEntities;
    private HashMap<String, HashMap<String, Integer>> documentsEntities; //<DocNum, Map of all the entities in the document and their frequency in the document>
    private File[] filesToParse;
    private Parse parser;
    private Indexer indexer;
    private int documentsCount;

    /**
     * Constructor
     *
     * @param useStemmer - boolean - use stemmer.
     */
    public RunnableParse(boolean useStemmer) {
        this.entities = new HashSet<>();
        this.singleAppearanceEntities = new HashSet<>();
        this.indexer = new Indexer();
        this.parser = new Parse(entities, singleAppearanceEntities, useStemmer);
        this.documentsCount = 0;
        this.documentsEntities = new HashMap<>();
    }

    /**
     * Return the entities field.
     *
     * @return - HashSet<String> - the entities field.
     */
    public HashSet<String> getEntities() {
        return entities;
    }

    /**
     * Return the singleAppearanceEntities field.
     *
     * @return - HashSet<String> - the singleAppearanceEntities field.
     */
    public HashSet<String> getSingleAppearanceEntities() {
        return singleAppearanceEntities;
    }

    /**
     * Return the documentsCount field.
     *
     * @return - int - the entities field.
     */
    public int getDocumentsCount() {
        return documentsCount;
    }

    /**
     * Return the dictionary from the indexer field.
     *
     * @return - Map<String, Pair<Integer, String>> - dictionary.
     */
    public Map<String, DictionaryEntryTrio> getDictionary() {
        return this.indexer.getDictionary();
    }

    /**
     * Sets the files to parse field.
     *
     * @param filesToParse - File[] - files to parse.
     */
    public void setFilesToParse(File[] filesToParse) {
        this.filesToParse = filesToParse;
    }

    @Override
    public void run() {

        ArrayList<ArrayList<TermDocumentTrio>> entirePostingEntries = new ArrayList<>();
        ExecutorService mergersPool = Executors.newFixedThreadPool(MERGERSPOOLSIZE);
        ArrayList<Future<ArrayList<TermDocumentTrio>>> futures = new ArrayList<>();

        System.out.println("RunnableParse Thread: " + Thread.currentThread().getName());

        for (File directory : filesToParse) {
            String filePath = directory.listFiles()[0].getAbsolutePath();
            if (Files.isReadable(Paths.get(filePath))) {
                ArrayList<Document> documents = CorpusProcessing.ReadFile.separateFileToDocuments(filePath);

                ArrayList<ArrayList<TermDocumentTrio>> postingEntriesListsOfFile = new ArrayList<>();

                for (Document document : documents) {
                    ArrayList<String> bagOfWords = parser.parseDocument(document);
                    ArrayList<TermDocumentTrio> postingsEntries = Mapper.processBagOfWords(false, document.getId(), document.getDate(), bagOfWords, document.getHeader());
                    postingEntriesListsOfFile.add(postingsEntries);

                    this.documentsEntities.put(document.getId(), parser.getLastProcessedDocumentEntities());

                    this.documentsCount++;
                }

                while (postingEntriesListsOfFile.size() > 1) {
                    postingEntriesListsOfFile.add(Mapper.mergeAndSortTwoPostingEntriesLists(postingEntriesListsOfFile.remove(0), postingEntriesListsOfFile.remove(0)));
                }

                //insert all posting entries of the file to entirePostingEntries

                entirePostingEntries.add(postingEntriesListsOfFile.get(0));

                //check if we can merge two posting list to one
                if (entirePostingEntries.size() >= 2) {
                    Future<ArrayList<TermDocumentTrio>> future = mergersPool.submit(new CallableMerge(entirePostingEntries.remove(0), entirePostingEntries.remove(0)));
                    futures.add(future);
                }
                //Getting result from callableMerge
                if (futures.size() > 0) {
                    if (futures.get(0).isDone()) {
                        Future<ArrayList<TermDocumentTrio>> future = futures.remove(0);
                        try {
                            entirePostingEntries.add(future.get());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        for (Future<ArrayList<TermDocumentTrio>> future : futures) {
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

        //entirePostingEntries contains all the sorted trios from all the documents - per thread
        //build posting file for all the documents in the thread
        this.indexer.buildInvertedIndex(entirePostingEntries.get(0));
    }

    /**
     * Getter for the documentsEntities.
     *
     * @return - HashMap<String, HashMap<String, Integer>> - The documents entities.
     */
    public HashMap<String, HashMap<String, Integer>> getDocumentsEntities() {
        return documentsEntities;
    }
}
