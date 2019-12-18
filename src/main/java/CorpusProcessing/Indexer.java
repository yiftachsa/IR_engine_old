package CorpusProcessing;

import com.sun.deploy.security.SelectableSecurityManager;
import javafx.collections.transformation.SortedList;
import javafx.util.Pair;

import java.util.*;

public class Indexer {

    /**
     * Corpus dictionary
     * entry: term-->{document frequency,posting file index}
     */
    private Map<String, Pair<Integer, String>> dictionary;//FIXME: If the dictionary need to be sorted we need to use SortedMap

    private Map<String, String> uniqueDictionary; //FIXME: maybe wh should delete it.

    private TreeSet<String> entities; //FIXME: maybe wh should delete it.

    private int postingCount;

    private String filePath;

    private static final int INVERTEDINDEXDIRECTORIESCOUNT = 27;

    public Indexer(String filePath) {
        this.filePath = filePath;
        this.dictionary = new TreeMap<>();
        this.uniqueDictionary = new HashMap<>();
    }

    public Indexer(Map<String, Pair<Integer, String>> dictionary, String path) {
        this.dictionary = dictionary;
        this.filePath = path;
    }

    public TreeSet<String> getEntities() {
        return this.entities;
    }

    public Indexer(Map<String, Pair<Integer, String>> dictionary, String path, TreeSet<String> entities) {
        this.dictionary = dictionary;
        this.filePath = path;
        this.entities = entities;
    }

    public void setEntities(TreeSet<String> entities) {
        this.entities = entities;
    }


   public void buildInvertedIndex1( ArrayList<HashMap<String, Pair<String, Integer>>> allPostingPerThread)
   {
     HashMap <String ,HashMap<String , List<Pair<String , Integer>>>> allRecordsSplitByFolders = new HashMap<>();
       for (int i = 0; i < allPostingPerThread.size(); i++) {
           for (HashMap.Entry<String, Pair<String, Integer>> record : allPostingPerThread.get(i).entrySet()) {
               if(record.getKey().length() == 0)
               {
                   continue;
               }
               String firstLetter = record.getKey().charAt(0)+""; //the term = first letter
               char charFirstLetter =record.getKey().charAt(0);
               String term = record.getKey(); //the term
               if(this.dictionary.containsKey(term))
               {
                   int newFrequency = dictionary.get(term).getKey() + 1;
                   //The function put override the previous value;
                   dictionary.put(term, new Pair<Integer, String>(newFrequency, ""));
               }
               else
               {
                   dictionary.put(term, new Pair<Integer, String>(1, ""));
               }
               if((charFirstLetter >= 'a' && charFirstLetter <= 'z') || (charFirstLetter >= 'A' && charFirstLetter <= 'Z'))
               {
                   if(allRecordsSplitByFolders.containsKey(firstLetter.toLowerCase())) //already has the posting first letter that needed
                   {
                       HashMap<String , List<Pair<String,Integer>>> tempHashMap = allRecordsSplitByFolders.get(firstLetter.toLowerCase()); //get the hashMap from that ket letter
                       if(tempHashMap.containsKey(record.getKey())) { //if the hash map has the term. we need to merge the pairs
                           List<Pair<String, Integer>> tempList = tempHashMap.get(record.getKey()); //get all the pair list from the hash map
                           tempList.add(record.getValue()); // add the new term
                           tempHashMap.put(record.getKey(), tempList); //update the hash map into the place that the term in it
                           allRecordsSplitByFolders.put(firstLetter.toLowerCase(), tempHashMap); // update the big hash
                       }
                       else  // hash map does not have the term
                       {
                           List<Pair<String,Integer>> tempList = new ArrayList<>(); //initialize new list
                           tempList.add(record.getValue()); // add the new pair to the term list
                           tempHashMap.put(record.getKey() , tempList); // add the term and the pair to hash map
                           allRecordsSplitByFolders.put(firstLetter.toLowerCase()+ "", tempHashMap); // update the big hash
                       }
                   }
                   else //doesnt have the first letter as a key
                   {
                       HashMap<String , List<Pair<String,Integer>>> tempHashMap = new HashMap<>(); //initialize new HashMap
                       List<Pair<String,Integer>> tempList = new ArrayList<>(); //initialize new  List
                       tempList.add(record.getValue()); //add the pair to the new list
                       tempHashMap.put(record.getKey() , tempList); //update the hash map with the term and the pair list
                       allRecordsSplitByFolders.put(firstLetter.toLowerCase()+ "" , tempHashMap); //update the big hash!
                   }
               }
               else //same as the if , but the first letter is not an alphabetic english letter
               {
                   if(allRecordsSplitByFolders.containsKey("`"))
                   {
                       HashMap<String , List<Pair<String,Integer>>> tempHashMap = allRecordsSplitByFolders.get("`");
                       if(tempHashMap.containsKey(record.getKey()))
                       {
                           List<Pair<String,Integer>> tempList = tempHashMap.get(record.getKey());
                           tempList.add(record.getValue());
                           tempHashMap.put(record.getKey() , tempList);
                           allRecordsSplitByFolders.put("`" , tempHashMap);
                       }
                       else
                       {
                           List<Pair<String,Integer>> tempList = new ArrayList<>();
                           tempList.add(record.getValue());
                           tempHashMap.put(record.getKey() , tempList);
                           allRecordsSplitByFolders.put("`" , tempHashMap);
                       }
                   }
                   else
                   {
                       HashMap<String , List<Pair<String,Integer>>> tempHashMap = new HashMap<>();
                       List<Pair<String,Integer>> tempList = new ArrayList<>();
                       tempList.add(record.getValue());
                       tempHashMap.put(record.getKey() , tempList);
                       allRecordsSplitByFolders.put("`" , tempHashMap);

                   }
               }
           }
       }
       Documenter.saveInvertedIndexes(allRecordsSplitByFolders);
   }



    public void buildInvertedIndex(ArrayList<Trio> postingEntries) {

        //this array contains in each cell all the posting lines separated by first letter - [*,a,b,c,d,e,...,z]
        String[] invertedIndex = new String[INVERTEDINDEXDIRECTORIESCOUNT];

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

        for (int i = postingEntries.size() -1; i >= 0; i--) {
            Trio postingEntry = postingEntries.get(i);
            String term = postingEntry.getTerm();
            String docId = postingEntry.getDocid();
            int termFrequency = postingEntry.getFrequency();
            if(!term.equals("")) {
                if (isLowerCaseLetters && (term.charAt(0) > limitCharacter)) {
                    if (dictionary.containsKey(term)) {
                        int newFrequency = dictionary.get(term).getKey() + 1;
                        //The function put override the previous value;
                        dictionary.put(term, new Pair<Integer, String>(newFrequency, "")); // TODO: maybe we can put the name of the posting file here.
                        if(posting[invertedArrayIndex].get(term) == null)
                        {

                            PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                            postingLine.add(new Pair<>(docId, termFrequency));
                            posting[invertedArrayIndex].put(term, postingLine);
                        }
                        else
                        {
                            posting[invertedArrayIndex].get(term).add(new Pair<>(docId, newFrequency));
                        }
                    } else {
                        dictionary.put(term, new Pair<Integer, String>(1, ""));
                        PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                        postingLine.add(new Pair<>(docId, termFrequency));
                        posting[invertedArrayIndex].put(term, postingLine);
                    }
                } else if (!isLowerCaseLetters && (term.charAt(0) > limitCharacter)) {
                    String lowerCaseTerm = term.toLowerCase();
                    //the dictionary already contains the term in lower case
                    if (dictionary.containsKey(lowerCaseTerm)) {
                        int newFrequency = dictionary.get(lowerCaseTerm).getKey() + 1;
                        dictionary.put(lowerCaseTerm, new Pair<Integer, String>(newFrequency, ""));
                        if(posting[invertedArrayIndex].get(lowerCaseTerm) == null)
                        {
                            PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                            postingLine.add(new Pair<>(docId, termFrequency));
                            posting[invertedArrayIndex].put(lowerCaseTerm, postingLine);
                        }
                        else
                        {
                            posting[invertedArrayIndex].get(lowerCaseTerm).add(new Pair<>(docId, newFrequency));
                        }
                        //posting[invertedArrayIndex].get(lowerCaseTerm).add(new Pair<>(docId, newFrequency));
                    }
                    //the dictionary already contains the term in upper case
                    else if (dictionary.containsKey(term)) {
                        int newFrequency = dictionary.get(term).getKey() + 1;
                        //The function put override the previous value;
                        dictionary.put(term, new Pair<Integer, String>(newFrequency, ""));
                        if(posting[invertedArrayIndex].get(lowerCaseTerm) == null)
                        {
                            PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                            postingLine.add(new Pair<>(docId, termFrequency));
                            posting[invertedArrayIndex].put(lowerCaseTerm, postingLine);
                        }
                        else
                        {
                            posting[invertedArrayIndex].get(lowerCaseTerm).add(new Pair<>(docId, newFrequency));
                        }
                    }
                    //the dictionary doesn't contain the term
                    else {
                        dictionary.put(term, new Pair<Integer, String>(1, ""));
                        PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                        postingLine.add(new Pair<>(docId, termFrequency));
                        posting[invertedArrayIndex].put(term, postingLine);
                    }
                } else {
                    invertedArrayIndex--;
                    limitCharacter--;
                    if (limitCharacter == '_') {//todo:
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


    public Map<String, String> getUniqueDictionary() {
        return uniqueDictionary;
    }

    public void setUniqueDictionary(Map<String, String> uniqueDictionary) {
        this.uniqueDictionary = uniqueDictionary;
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

    public static int getINVERTEDINDEXDIRECTORIESCOUNT() {
        return INVERTEDINDEXDIRECTORIESCOUNT;
    }


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
     *
     * @param singleAppearances
     */
    public void removeAllSingleAppearances(HashSet<String> singleAppearances) {
        for (String entity : singleAppearances ){
            if(this.dictionary.containsKey(entity)){
                this.dictionary.remove(entity);
            }
        }
        //TODO:Check if there is a better way
        ArrayList<String> singleDocumentTerms = new ArrayList<>();

        for (Map.Entry<String, Pair<Integer, String>> dictionaryEntree : this.dictionary.entrySet()) {
            int documentFrequency = dictionaryEntree.getValue().getKey();
            if (documentFrequency ==1 && dictionaryEntree.getKey().charAt(0) >= 'A' && dictionaryEntree.getKey().charAt(0) <= 'Z'){
                singleDocumentTerms.add(dictionaryEntree.getKey());
            }
        }

        for(String singleAppearanceTerm : singleDocumentTerms){
            this.dictionary.remove(singleAppearanceTerm);
        }
    }
}