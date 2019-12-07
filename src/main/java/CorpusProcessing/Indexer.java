package  CorpusProcessing;

import javafx.collections.transformation.SortedList;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class Indexer {

    /**
     * Corpus dictionary
     * entry: term-->{document frequency,posting file index}
     */
    private Map<String, Pair<Integer, String>> dictionary;//FIXME: If the dictionary need to be sorted we need to use SortedMap
    /**
     * Temporary posting buffer, reinitialized after each save to the secondary memory.
     * entry: term-->({docId,term frequency},{(docId,term frequency)},...)
     */
    private SortedMap<String, ArrayList<Pair<String, Integer>>> posting;

    private Map<String, String> uniqueDictionary;

    private Map<String, String> entitiesDictionary;

    private int postingCount;

    private String filePath;

    private static final double NUMBEROFTRIOSINPOSTINGFILE = Math.pow(2, 12);

    public Indexer(String filePath) {
        this.dictionary = new HashMap<>(); //FIXME: If the dictionary need to be sorted we need to use TreeMap
        this.posting = new TreeMap<>();
        this.uniqueDictionary = new HashMap<>();
        this.entitiesDictionary = new HashMap<>();
        this.postingCount = 0;
        this.filePath = filePath;
    }

    public void buildInvertedIndex() {
        int numberOfPostingPortions = Documenter.getIterationNumber();
        for (int i = 0; i < numberOfPostingPortions; i++) {
            int trioCount = 0;
            //open postingPortion i
            ArrayList<Trio> sortedPostingPortion = new ArrayList<>();
            try {
                FileInputStream fileInputStream = new FileInputStream(this.filePath + "\\postingPortions\\" + i);
                ObjectInputStream ObjectInputStream = new ObjectInputStream(fileInputStream);
                sortedPostingPortion = (ArrayList<Trio>) ObjectInputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Enter to dictionary
            if (sortedPostingPortion != null) {
                for (int j = 0; j < sortedPostingPortion.size(); j++) {
                    Trio postingEntry = sortedPostingPortion.get(j);
                    String term = postingEntry.getTerm();
                    String docId = postingEntry.getDocid();
                    int termFrequency = postingEntry.getFrequency();
                    if (dictionary.containsKey(term)) {
                        int newFrequency = dictionary.get(term).getKey() + 1;
                        dictionary.put(term, new Pair<Integer, String>(newFrequency, postingCount + ""));
                        posting.get(term).add(new Pair<>(docId, newFrequency));
                    } else {
                        dictionary.put(term, new Pair<Integer, String>(1, postingCount + ""));
                        ArrayList<Pair<String, Integer>> postingLine = new ArrayList<>();
                        postingLine.add(new Pair<>(docId, termFrequency));
                        posting.put(term, postingLine);
                    }
                    trioCount++;
                    if (trioCount >= NUMBEROFTRIOSINPOSTINGFILE) {
                        if (sortedPostingPortion.size() < j + 1) {
                            String nextTerm = sortedPostingPortion.get(j + 1).getTerm();
                            if (!nextTerm.equals(term)) {
                                Documenter.saveInvertedIndex(posting, postingCount);
                                posting = new TreeMap<>();
                                trioCount = 0;
                                postingCount++;
                            }
                        }
                    }
                }
            }
        }
    }


    public boolean getDictionaryStatus() {
        if (dictionary == null) {
            return false;
        } else {
            return true;
        }
    }


    public boolean doesDictionaryContains(String key) {
        return dictionary.containsKey(key);
    }

    public Map<String, Pair<Integer, String>> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Map<String, Pair<Integer, String>> dictionary) {
        this.dictionary = dictionary;
    }

    public SortedMap<String, ArrayList<Pair<String, Integer>>> getPosting() {
        return posting;
    }

    public void setPosting(SortedMap<String, ArrayList<Pair<String, Integer>>> posting) {
        this.posting = posting;
    }

    public Map<String, String> getUniqueDictionary() {
        return uniqueDictionary;
    }

    public void setUniqueDictionary(Map<String, String> uniqueDictionary) {
        this.uniqueDictionary = uniqueDictionary;
    }

    public Map<String, String> getEntitiesDictionary() {
        return entitiesDictionary;
    }

    public void setEntitiesDictionary(Map<String, String> entitiesDictionary) {
        this.entitiesDictionary = entitiesDictionary;
    }

    public int getPostingCount() {
        return postingCount;
    }

    public void setPostingCount(int postingCount) {
        this.postingCount = postingCount;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public static double getNUMBEROFTRIOSINPOSTINGFILE() {
        return NUMBEROFTRIOSINPOSTINGFILE;
    }
}