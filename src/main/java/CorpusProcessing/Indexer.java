package CorpusProcessing;

import javafx.util.Pair;

import java.util.*;

public class Indexer {

    /**
     * Corpus dictionary
     * entry: term-->{document frequency, cumulative frequency, posting file index}
     */
    private Map<String, DictionaryEntryTrio> dictionary;

    private TreeSet<String> entities;

    private HashMap<String, HashMap<String, Integer>> allDocumentsEntities;//<DocNum, Map of all the entities in the document and their frequency in the document>

    private int documentsCount;

    private HashMap<String, String> documentsDetails;


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
     * @param dictionary           - Map<String, DictionaryEntryTrio>
     * @param entities             - TreeSet<String>
     * @param allDocumentsEntities
     * @param documentDetails
     */
    public Indexer(Map<String, DictionaryEntryTrio> dictionary, TreeSet<String> entities, HashMap<String, HashMap<String, Integer>> allDocumentsEntities, HashMap<String, String> documentDetails) {
        this.dictionary = dictionary;
        this.entities = entities;
        this.allDocumentsEntities = allDocumentsEntities;
        this.documentsDetails = documentDetails;
    }


    /**
     * Receives a list of TermDocumentTrio and builds inverted indices from them.
     * Generates individual posting file for each starting letter.
     * Saves the generated posting files (temporary inverted indices).
     *
     * @param postingEntries - ArrayList<TermDocumentTrio> - posting entries trios
     */
    public void buildInvertedIndex(ArrayList<TermDocumentTrio> postingEntries) {

        //this array contains in each cell the directory title - [`,a,b,c,d,e,...,z]
        String[] invertedIndexDirectoriesTitles = {"`", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

        //Posting buffer - entry: term-->({docId,term frequency},{(docId,term frequency)},...)
        Map<String, PriorityQueue<Pair<String, Integer>>>[] posting = new TreeMap[INVERTEDINDEXDIRECTORIESCOUNT]; //the priority queue functions as a sorted list

        for (int i = 0; i < posting.length; i++) {
            posting[i] = new TreeMap<>();
        }

        //Enter to dictionary
        int invertedArrayIndex = 26;
        char limitCharacter = 'y';
        String singleLetterPosting = "";
        boolean isLowerCaseLetters = true;

        for (int i = postingEntries.size() - 1; i >= 0; i--) {
            TermDocumentTrio postingEntry = postingEntries.get(i);
            String term = postingEntry.getTerm();
            String docId = postingEntry.getDocid();
            int termFrequency = postingEntry.getFrequency();
            if (!term.equals("")) {
                if (isLowerCaseLetters && (term.charAt(0) > limitCharacter)) {
                    if (dictionary.containsKey(term)) {

                        int newDocumentFrequency = dictionary.get(term).getDocumentFrequency() + 1;
                        int newCumulativeFrequency = dictionary.get(term).getCumulativeFrequency() + termFrequency;

                        //The function put override the previous value;
                        dictionary.put(term, new DictionaryEntryTrio(newDocumentFrequency, newCumulativeFrequency, invertedIndexDirectoriesTitles[invertedArrayIndex]));
                        //dictionary.put(term, new Pair<Integer, String>(newDocumentFrequency, invertedIndexDirectoriesTitles[invertedArrayIndex]));

                        if (posting[invertedArrayIndex].get(term) == null) {
                            PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                            postingLine.add(new Pair<>(docId, termFrequency));
                            posting[invertedArrayIndex].put(term, postingLine);
                        } else {
                            posting[invertedArrayIndex].get(term).add(new Pair<>(docId, newDocumentFrequency));
                        }
                    } else {
                        dictionary.put(term, new DictionaryEntryTrio(1, termFrequency, invertedIndexDirectoriesTitles[invertedArrayIndex]));

                        PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                        postingLine.add(new Pair<>(docId, termFrequency));
                        posting[invertedArrayIndex].put(term, postingLine);
                    }
                } else if (!isLowerCaseLetters && (term.charAt(0) > limitCharacter)) {
                    String lowerCaseTerm = term.toLowerCase();
                    //the dictionary already contains the term in lower case
                    if (dictionary.containsKey(lowerCaseTerm)) {

                        int newDocumentFrequency = dictionary.get(lowerCaseTerm).getDocumentFrequency() + 1;
                        int newCumulativeFrequency = dictionary.get(lowerCaseTerm).getCumulativeFrequency() + termFrequency;

                        dictionary.put(lowerCaseTerm, new DictionaryEntryTrio(newDocumentFrequency, newCumulativeFrequency, invertedIndexDirectoriesTitles[invertedArrayIndex]));

                        if (posting[invertedArrayIndex].get(lowerCaseTerm) == null) {
                            PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                            postingLine.add(new Pair<>(docId, termFrequency));
                            posting[invertedArrayIndex].put(lowerCaseTerm, postingLine);
                        } else {
                            posting[invertedArrayIndex].get(lowerCaseTerm).add(new Pair<>(docId, newDocumentFrequency));
                        }
                    } else if (dictionary.containsKey(term)) {//the dictionary already contains the term in upper case
                        DictionaryEntryTrio entry = dictionary.get(term);
                        int newDocumentFrequency = entry.getDocumentFrequency() + 1;
                        int newCumulativeFrequency = entry.getCumulativeFrequency() + termFrequency;

                        String postingDirectory = entry.getPostingIndex();
                        //The function put override the previous value;
                        dictionary.put(term, new DictionaryEntryTrio(newDocumentFrequency, newCumulativeFrequency, postingDirectory));
                        if (posting[invertedArrayIndex].get(lowerCaseTerm) == null) {
                            PriorityQueue<Pair<String, Integer>> postingLine = new PriorityQueue<>(new PairComparator());
                            postingLine.add(new Pair<>(docId, termFrequency));
                            posting[invertedArrayIndex].put(lowerCaseTerm, postingLine);
                        } else {
                            posting[invertedArrayIndex].get(lowerCaseTerm).add(new Pair<>(docId, newDocumentFrequency));
                        }
                    }
                    //the dictionary doesn't contain the term
                    else {
                        dictionary.put(term, new DictionaryEntryTrio(1, termFrequency, invertedIndexDirectoriesTitles[invertedArrayIndex]));
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

    public Map<String, DictionaryEntryTrio> getDictionary() {
        return dictionary;
    }

    public static int getINVERTEDINDEXDIRECTORIESCOUNT() {
        return INVERTEDINDEXDIRECTORIESCOUNT;
    }

    /**
     * Receives a partial dictionary and merges it into the dictionary field of this Indexer
     *
     * @param partialDictionary - Map<String, DictionaryEntryTrio> - dictionary to be merged
     */
    public void addPartialDictionary(Map<String, DictionaryEntryTrio> partialDictionary) {
        if (this.dictionary.size() == 0) {
            this.dictionary = partialDictionary;
        } else {
            for (Map.Entry<String, DictionaryEntryTrio> dictionaryEntry : partialDictionary.entrySet()) {
                String term = dictionaryEntry.getKey();
                DictionaryEntryTrio dictionaryEntryTrio = dictionaryEntry.getValue();

                if (this.dictionary.containsKey(term)) {
                    DictionaryEntryTrio entry = this.dictionary.get(term);
                    int newDocumentFrequency = entry.getDocumentFrequency() + dictionaryEntryTrio.getDocumentFrequency();
                    int newCumulativeFrequency = entry.getCumulativeFrequency() + dictionaryEntryTrio.getCumulativeFrequency();

                    //The function put override the previous value;
                    this.dictionary.put(term, new DictionaryEntryTrio(newDocumentFrequency, newCumulativeFrequency, dictionaryEntryTrio.getPostingIndex()));

                } else if (this.dictionary.containsKey(term.toLowerCase())) {
                    DictionaryEntryTrio entry = this.dictionary.get(term.toLowerCase());
                    int newDocumentFrequency = entry.getDocumentFrequency() + dictionaryEntryTrio.getDocumentFrequency();
                    int newCumulativeFrequency = entry.getCumulativeFrequency() + dictionaryEntryTrio.getCumulativeFrequency();

                    //The function put override the previous value;
                    this.dictionary.put(term.toLowerCase(), new DictionaryEntryTrio(newDocumentFrequency, newCumulativeFrequency, dictionaryEntryTrio.getPostingIndex()));
                } else if (this.dictionary.containsKey(term.toUpperCase())) {
                    String existingTerm = term.toUpperCase();
                    DictionaryEntryTrio existingTrio = this.dictionary.get(existingTerm);

                    int newDocumentFrequency = existingTrio.getDocumentFrequency() + dictionaryEntryTrio.getDocumentFrequency();
                    int newCumulativeFrequency = existingTrio.getCumulativeFrequency() + dictionaryEntryTrio.getCumulativeFrequency();

                    this.dictionary.remove(existingTerm);
                    this.dictionary.put(term, new DictionaryEntryTrio(newDocumentFrequency, newCumulativeFrequency, dictionaryEntryTrio.getPostingIndex()));
                } else {
                    dictionary.put(term, dictionaryEntryTrio);
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


    public void setDocumentEntities(HashMap<String, HashMap<String, Integer>> documentsEntities) {
        this.allDocumentsEntities = documentsEntities;
    }

    public void setDocumentDetails(HashMap<String, String> documentsDetails) {
        this.documentsDetails = documentsDetails;
    }

    /**
     * Receive term and return df(term)
     *
     * @param term - String - term in the dictionary
     * @return df(term) - int - the document frequency of the term, if the term isn't in the dictionary return 0
     */
    public int getDocumentFrequency(String term) {
        if (dictionary.containsKey(term)) {
            return this.dictionary.get(term).getDocumentFrequency();
        }
        return 0;
    }

    /**
     * Receive document ID and return the length of the document
     *
     * @param documentID
     * @return
     */
    public int getDocumentLength(String documentID) {
        //docId,maxTermFrequency + "," + uniqTermsCount + "," + length + "," + documentDate
        String documentDetails = this.documentsDetails.get(documentID);
        String[] splitDocumentDetails = documentDetails.split(",");
        int documentLength = Integer.parseInt(splitDocumentDetails[2]);
        return documentLength;

    }

    public ArrayList<Pair<String, Integer>> getTermPosting(String term) {
        //todo: reach to the appropriate posting file and pull the relevant posting line - all the documents and dfs pairs
        DictionaryEntryTrio dictionaryEntryTrio = this.dictionary.get(term);
        boolean toLowerCase = false;

        if(dictionaryEntryTrio == null){
            dictionaryEntryTrio = this.dictionary.get(term.toLowerCase());
            if(dictionaryEntryTrio == null){
                return null;
            }
            toLowerCase = true;
        }
        if(toLowerCase){
            term = term.toLowerCase();
        }
        ArrayList<Pair<String, Integer>> postingLine = Documenter.retrievePosting(term, dictionaryEntryTrio.getPostingIndex());
        return postingLine;
    }

    /**
     * Checks if a given String is a valid document number by comparing it with the pre-loaded documents details.
     *
     * @param documentNumber - String - a string to check.
     * @return - boolean - true if a given String is a valid document number, else false.
     */
    public boolean isValidDocumentNumber(String documentNumber) {
        return this.allDocumentsEntities.containsKey(documentNumber); //Check
    }

    /**
     * Returns all the document entities and their frequency for the given documentNumber as a Map of (entity , frequency).
     *
     * @param documentNumber - String - a valid document number
     * @return - HashMap<String, Integer> - all the document entities
     */
    public HashMap<String, Integer> getDocumentEntitiesMap(String documentNumber) {
        return allDocumentsEntities.get(documentNumber);
    }
    /**
     * Returns all the document entities  for the given documentNumber.
     *
     * @param documentNumber - String - a valid document number
     * @return - ArrayList<String> - all the document entities
     */
    public ArrayList<String> getDocumentEntitiesList(String documentNumber) {
        ArrayList<String> result = new ArrayList<>();
        HashMap<String, Integer> documentEntities = allDocumentsEntities.get(documentNumber);
        for(Map.Entry<String,Integer> entry: documentEntities.entrySet()){
            result.add(entry.getKey());
        }
        return result;
    }

    /**
     * Returns all the saved document details for the given documentNumber as an array of strings.
     *
     * @param documentNumber - String - a valid document number
     * @return - String[] - an array of all the document details
     */
    public String[] getDocumentDetails(String documentNumber) {
        String details =  this.documentsDetails.get(documentNumber);
        //maxTermFrequency + "," + uniqTermsCount + "," + length + "," + documentDate+","+documentHeader

        String maxTermFrequency = details.substring(0,details.indexOf(","));
        details = details.substring(details.indexOf(",")+1);

        String uniqTermsCount = details.substring(0,details.indexOf(","));
        details = details.substring(details.indexOf(",")+1);

        String length = details.substring(0,details.indexOf(","));
        details = details.substring(details.indexOf(",")+1);

        String documentDate = details.substring(0,details.indexOf(","));
        details = details.substring(details.indexOf(",")+1);

        String documentHeader = details;

        return new String[]{maxTermFrequency, uniqTermsCount, length, documentDate, documentHeader};
    }

    public double getAverageDocumentLength() {
        int numberOfDocuments = documentsDetails.size();
        int sigmaLength = 0;

        for (Map.Entry<String, String> entry: documentsDetails.entrySet()){
            sigmaLength = sigmaLength + getDocumentLength(entry.getKey());
        }

        double avdl = sigmaLength/numberOfDocuments;
        return avdl;
    }

    /**
     * Receive Document ID and return the Document Header
     * @param documentID
     * @return
     */
    public String getDocumentHeader(String documentID) {

        String[] details = getDocumentDetails(documentID);
        return details[details.length-1];

    }
}