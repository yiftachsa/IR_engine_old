package CorpusProcessing;

import javafx.collections.transformation.SortedList;
import javafx.util.Pair;

import java.util.*;

public class Indexer {

    /**
     * Corpus dictionary
     * entry: term-->{document frequency,posting file index}
     */
    private Map<String, Pair<Integer, String>> dictionary;

    private TreeSet<String> entities;

    private int documentsCount;

    private static final int INVERTEDINDEXDIRECTORIESCOUNT = 27;

    /**
     * Constructor
     */
    public Indexer() {
        this.dictionary = new TreeMap<>();
    }

    /**
     * Constructor
     *
     * @param dictionary - Map<String, Pair<Integer, String>>
     * @param entities   - TreeSet<String>
     */
    public Indexer(Map<String, Pair<Integer, String>> dictionary, TreeSet<String> entities) {
        this.dictionary = dictionary;
        this.entities = entities;
    }


    public void buildInvertedIndex(ArrayList<Trio> postingEntries) {

        //this array contains in each cell the directory title - [`,a,b,c,d,e,...,z]
        String[] invertedIndexDirectoriesTitles = {"`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

        //Posting buffer - entry: term-->({docId,term frequency},{(docId,term frequency)},...)
        Map<String, PriorityQueue<Pair<String, Integer>>>[] posting = new TreeMap[INVERTEDINDEXDIRECTORIESCOUNT]; //the priority queue functions as a sorted list

        for (int i = 0; i < posting.length; i++) {
            posting[i] = new TreeMap<>();
        }
        //the posting entries is sorted by ascii, the inverted index wont be like that its will be AaBb..

        //Enter to dictionary
        int invertedArrayIndex = 26;
        char limitCharacter = 'y';
        String singleLetterPosting = "";
        boolean isLowerCaseLetters = true;

        for (int i = postingEntries.size() - 1; i >= 0; i--) {
            Trio postingEntry = postingEntries.get(i);
            String term = postingEntry.getTerm();
            String docId = postingEntry.getDocid();
            int termFrequency = postingEntry.getFrequency();
            if (!term.equals("")) {
                if (isLowerCaseLetters && (term.charAt(0) > limitCharacter)) {
                    if (dictionary.containsKey(term)) {
                        int newFrequency = dictionary.get(term).getKey() + 1;
                        //The function put override the previous value;
                        dictionary.put(term, new Pair<Integer, String>(newFrequency, invertedIndexDirectoriesTitles[invertedArrayIndex]));
                        if (posting[invertedArrayIndex].get(term) == null) {

                            PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                            postingLine.add(new Pair<>(docId, termFrequency));
                            posting[invertedArrayIndex].put(term, postingLine);
                        } else {
                            posting[invertedArrayIndex].get(term).add(new Pair<>(docId, newFrequency));
                        }
                    } else {
                        dictionary.put(term, new Pair<Integer, String>(1, invertedIndexDirectoriesTitles[invertedArrayIndex]));
                        PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                        postingLine.add(new Pair<>(docId, termFrequency));
                        posting[invertedArrayIndex].put(term, postingLine);
                    }
                } else if (!isLowerCaseLetters && (term.charAt(0) > limitCharacter)) {
                    String lowerCaseTerm = term.toLowerCase();
                    //the dictionary already contains the term in lower case
                    if (dictionary.containsKey(lowerCaseTerm)) {
                        int newFrequency = dictionary.get(lowerCaseTerm).getKey() + 1;
                        dictionary.put(lowerCaseTerm, new Pair<Integer, String>(newFrequency, invertedIndexDirectoriesTitles[invertedArrayIndex]));
                        if (posting[invertedArrayIndex].get(lowerCaseTerm) == null) {
                            PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                            postingLine.add(new Pair<>(docId, termFrequency));
                            posting[invertedArrayIndex].put(lowerCaseTerm, postingLine);
                        } else {
                            posting[invertedArrayIndex].get(lowerCaseTerm).add(new Pair<>(docId, newFrequency));
                        }
                        //posting[invertedArrayIndex].get(lowerCaseTerm).add(new Pair<>(docId, newFrequency));
                    }
                    //the dictionary already contains the term in upper case
                    else if (dictionary.containsKey(term)) {
                        int newFrequency = dictionary.get(term).getKey() + 1;
                        String postingDirectory = dictionary.get(term).getValue();
                        //The function put override the previous value;
                        dictionary.put(term, new Pair<Integer, String>(newFrequency, postingDirectory));
                        if (posting[invertedArrayIndex].get(lowerCaseTerm) == null) {
                            PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                            postingLine.add(new Pair<>(docId, termFrequency));
                            posting[invertedArrayIndex].put(lowerCaseTerm, postingLine);
                        } else {
                            posting[invertedArrayIndex].get(lowerCaseTerm).add(new Pair<>(docId, newFrequency));
                        }
                    }
                    //the dictionary doesn't contain the term
                    else {
                        dictionary.put(term, new Pair<Integer, String>(1, invertedIndexDirectoriesTitles[invertedArrayIndex]));
                        PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                        postingLine.add(new Pair<>(docId, termFrequency));
                        posting[invertedArrayIndex].put(term, postingLine);
                    }
                } else {
                    invertedArrayIndex--;
                    limitCharacter--;
                    if (limitCharacter == '_') {
                        limitCharacter = 'Y';
                        invertedArrayIndex = 26;
                        isLowerCaseLetters = false;
                    }
                    if (limitCharacter == '?') {
                        limitCharacter = ' ';
                    }
                    i++;
                }
            }
        }
        Documenter.saveInvertedIndex(posting);
    }

    /**
     * Checks if the dictionary is loaded
     *
     * @return - boolean - true if the dictionary is loaded, else false
     */
    public boolean getDictionaryStatus() {
        return dictionary != null;
    }

    public int getDocumentsCount() {
        return documentsCount;
    }

    public void setDocumentsCount(int documentsCount) {
        this.documentsCount = documentsCount;
    }

    public void setEntities(TreeSet<String> entities) {
        this.entities = entities;
    }

    public boolean doesDictionaryContains(String key) {
        return dictionary.containsKey(key);
    }

    public Map<String, Pair<Integer, String>> getDictionary() {
        return dictionary;
    }

    public static int getINVERTEDINDEXDIRECTORIESCOUNT() {
        return INVERTEDINDEXDIRECTORIESCOUNT;
    }

    /**
     * Receives a partial dictionary and merges it into the dictionary field of this Indexer
     *
     * @param partialDictionary - Map<String, Pair<Integer, String>> - dictionary to be merged
     */
    public void addPartialDictionary(Map<String, Pair<Integer, String>> partialDictionary) {
        if (this.dictionary.size() == 0) {
            this.dictionary = partialDictionary;
        } else {
            for (Map.Entry<String, Pair<Integer, String>> dictionaryEntree : partialDictionary.entrySet()) {
                String term = dictionaryEntree.getKey();
                Pair<Integer, String> pair = dictionaryEntree.getValue();

                if (this.dictionary.containsKey(term)) {
                    int newFrequency = this.dictionary.get(term).getKey() + pair.getKey();
                    //The function put override the previous value;
                    this.dictionary.put(term, new Pair<Integer, String>(newFrequency, pair.getValue()));
                } else if (this.dictionary.containsKey(term.toLowerCase())) {
                    int newFrequency = this.dictionary.get(term.toLowerCase()).getKey() + pair.getKey();
                    //The function put override the previous value;
                    this.dictionary.put(term.toLowerCase(), new Pair<Integer, String>(newFrequency, pair.getValue()));
                } else if (this.dictionary.containsKey(term.toUpperCase())) {
                    String existingTerm = term.toUpperCase();
                    Pair<Integer, String> existingPair = this.dictionary.get(existingTerm);
                    int newFrequency = existingPair.getKey() + pair.getKey();

                    this.dictionary.remove(existingTerm);
                    this.dictionary.put(term, new Pair<Integer, String>(newFrequency, pair.getValue()));
                } else {
                    dictionary.put(term, pair);
                }
            }
        }
    }

    /**
     * Removes all the Strings in the given set from the dictionary field.
     *
     * @param singleAppearances - HashSet<String> - a set of Strings to be removed
     */
    public void removeAllSingleAppearances(HashSet<String> singleAppearances) {
        for (String entity : singleAppearances) {
            entity = entity.toUpperCase();
            if (this.dictionary.containsKey(entity)) {
                this.dictionary.remove(entity);
            }
        }
    }

    /**
     * Returns the dictionary size.
     *
     * @return - int - the dictionary size, the number of the unique terms
     */
    public int getDictionarySize() {
        if (this.dictionary != null) {
            return this.dictionary.size();
        }
        return 0;
    }
}