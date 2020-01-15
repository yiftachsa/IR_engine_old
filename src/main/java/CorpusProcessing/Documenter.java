package CorpusProcessing;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

/**
 * Responsible to write to Disk.
 * Receive the information about specific document(Doc ID , Max tf , number of unique words).
 */
public class Documenter {

    private static int invertedIndexIndex = 0;

    private static HashMap<String, String> documentsDetails = new HashMap<>();

    private static ReentrantLock documentsDetailsMutex;
    private static ReentrantLock invertedIndexMutex;

    private static String filesPath;


   // private static Pattern pipelinePattern = Pattern.compile("[\\|]");

    /**
     * Initializes the fields and the directories required for the indexing process.
     *
     * @param path - String - the results path.
     */
    public static void start(String path) {
        filesPath = path + "";
        documentsDetailsMutex = new ReentrantLock();
        invertedIndexMutex = new ReentrantLock();
        new File(filesPath).mkdir();
        new File(filesPath + "\\Entities").mkdir();
        new File(filesPath + "\\DocumentsDetails").mkdir();
        new File(filesPath + "\\PostingFiles").mkdir();
        char directoryName = '`';
        for (int i = 0; i < Indexer.getINVERTEDINDEXDIRECTORIESCOUNT(); i++) {
            new File(filesPath + "\\PostingFiles\\" + directoryName).mkdir();
            directoryName++;
        }
        new File(filesPath + "\\Dictionary").mkdir();
    }

    /**
     * Getter for the documentsDetails.
     *
     * @return - HashMap<String, String> - Document details map.
     */
    public static HashMap<String, String> getDocumentsDetails() {
        return documentsDetails;
    }

    /**
     * Receives details about a document and adds it to the field documentsDetails list.
     *
     * @param docId            - String - uniq document identifier
     * @param maxTermFrequency - int - the maximum term frequency in the document
     * @param uniqTermsCount   - int - the number of uniq terms in the document
     * @param length           - int - the total number of terms in the document
     */
    public static void saveDocumentDetails(String docId, int maxTermFrequency, int uniqTermsCount, int length, String documentDate, String documentHeader) {
        if (filesPath != null) {
            documentsDetailsMutex.lock();
            documentsDetails.put(docId, maxTermFrequency + "," + uniqTermsCount + "," + length + "," + documentDate + "," + documentHeader);
            documentsDetailsMutex.unlock();
        }
    }

    /**
     * Saves the documentsDetails field to file
     */
    private static void saveDocumentsDetailsToFile() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filesPath + "\\DocumentsDetails\\DocumentsDetails"));
            for (Map.Entry<String, String> documentDetails : documentsDetails.entrySet()) {
                writer.write(documentDetails.getKey() + "," + documentDetails.getValue());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the documentsDetails field to file
     *
     * @param path - String - path
     * @return - HashSet<String> - documents details
     */
    public static HashMap<String, String> loadDocumentsDetailsFromFile(String path) {
        documentsDetails = new HashMap<>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path + "\\DocumentsDetails\\DocumentsDetails"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String documentID = line.substring(0, line.indexOf(","));
                String documentDetails = line.substring(line.indexOf(",") + 1);
                documentsDetails.put(documentID, documentDetails);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documentsDetails;
    }


    /**
     * Saves all the postings to the disk
     *
     * @param postingArray - Map<String, PriorityQueue<Pair<String, Integer>>>[] - postings
     */
    public static void saveInvertedIndex(Map<String, PriorityQueue<Pair<String, Integer>>>[] postingArray) {

        if (filesPath != null) {

            invertedIndexMutex.lock();
            int fileIndex = invertedIndexIndex;
            invertedIndexIndex++;
            invertedIndexMutex.unlock();

            String filePath = filesPath + "\\PostingFiles";

            char startChar = '`';

            for (int i = 0; i < postingArray.length; i++) {
                savePostingFile(postingArray[i], filePath + "\\" + (char) ((int) startChar + i) + "\\postingFile" + fileIndex);
            }
        }
    }

    /**
     * Saves a given final posting file to the given path
     *
     * @param posting  - ArrayList<String> - posting
     * @param filePath - String - path
     */
    public static void saveFinalPostingFile(ArrayList<String> posting, String filePath) {
        File file = new File(filePath);
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            for (int i = 0; i < posting.size(); i++) {
                fileWriter.write(posting.get(i) + "\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Saves an individual posting file as text.
     *
     * @param posting  - PriorityQueue<Pair<String, Integer>>> - posting
     * @param filePath - String - path
     */
    public static void savePostingFile(Map<String, PriorityQueue<Pair<String, Integer>>> posting, String filePath) {
        File file = new File(filePath);
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            List<String> allPosting = new LinkedList<>();
            for (SortedMap.Entry<String, PriorityQueue<Pair<String, Integer>>> postingLine : posting.entrySet()) {
                String term = postingLine.getKey();
                PriorityQueue<Pair<String, Integer>> pairs = postingLine.getValue();
                String out = term + "!";
                for (Pair<String, Integer> pair : pairs) {
                    out = out + "<" + pair.getKey() + "," + pair.getValue() + ">|";
                }
                allPosting.add(out);
            }
            if (posting.size() != 0) {
                String s = "";
                for (String post : allPosting) {
                    s = s + post + "\n";
                }
                s = s.substring(0, s.length() - 1);
                fileWriter.write(s);
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Saves a given String TreeSet to a file as an Object.
     *
     * @param treeSetToSave - TreeSet<String> - an ordered set of Strings
     */
    public static void saveEntities(TreeSet<String> treeSetToSave) {
        String filePath = filesPath + "\\Entities\\Entities";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeObject(treeSetToSave);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Receives a path and loads the Entities from it (loads a Tree<Set> object).
     *
     * @param path - String - path
     * @return - TreeSet<String> - entities
     */
    public static TreeSet<String> loadEntities(String path) {
        String entitiesPath = path + "\\Entities\\Entities";
        FileInputStream fileInputStream;
        TreeSet<String> entities = null;

        try {
            fileInputStream = new FileInputStream(entitiesPath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            entities = (TreeSet<String>) objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return entities;
    }

    /**
     * Saves all the data structures which are in the memory and need to be saved for later use
     */
    public static void shutdown() {
        if (documentsDetails.size() > 0) {
            saveDocumentsDetailsToFile();
        }
    }

    /**
     * Saves a given dictionary to hard drive.
     *
     * @param dictionary - Map<String, DictionaryEntryTrio>
     */
    public static void saveDictionary(Map<String, DictionaryEntryTrio> dictionary) {
        if (filesPath != null) {
            //WRITE TO DISK! 
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(filesPath + "\\Dictionary\\dictionary"));
                for (Map.Entry<String, DictionaryEntryTrio> entry : dictionary.entrySet()) {
                    String term = entry.getKey();
                    DictionaryEntryTrio dictionaryEntryTrio = entry.getValue();
                    String outLine = term + "~" + dictionaryEntryTrio.getDocumentFrequency() + "," + dictionaryEntryTrio.getCumulativeFrequency() + "," + dictionaryEntryTrio.getPostingIndex();
                    writer.write(outLine);
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Receives a path to a directory containing a dictionary, parses the text file and returns the reconstructed dictionary.
     *
     * @param dictionaryPath - String - a path containing a dictionary
     * @return - Map<String, DictionaryEntryTrio> - dictionary
     */
    public static Map<String, DictionaryEntryTrio> loadDictionary(String dictionaryPath) {
        BufferedReader reader = null;
        Map<String, DictionaryEntryTrio> dictionary = new TreeMap<>();
        try {
            reader = new BufferedReader((new FileReader(dictionaryPath + "\\Dictionary\\dictionary")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String term = line.substring(0, line.indexOf('~'));
                line = line.substring(line.indexOf('~') + 1);
                String[] entreeDetails = line.split(",");
                dictionary.put(term, new DictionaryEntryTrio(Integer.parseInt(entreeDetails[0]), Integer.parseInt(entreeDetails[1]), entreeDetails[2]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    /**
     * Receives a path and loads all the posting files in it.
     *
     * @param path - String - a path to a directory containing posting files
     * @return - ArrayList<String> - list of String lines from all the posting lists
     */
    public static ArrayList<String> loadPostingFile(String path) {
        ArrayList<String> stringArrayList = new ArrayList<>();
        File folder = new File(path);
        try {
            for (File fileEntry : folder.listFiles()) {
                List<String> stringList = Files.readAllLines(fileEntry.toPath(), StandardCharsets.UTF_8);
                stringArrayList.addAll(stringList);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return stringArrayList;
    }

    /**
     * Deletes all the files from a given directory
     *
     * @param path - String - The directory absolute path
     */
    public static void deleteAllFilesFromDirectory(String path) {
        File directory = new File(path);
        File[] postingFiles = directory.listFiles();
        for (int i = 0; i < postingFiles.length; i++) {
            postingFiles[i].delete();
        }
    }

    /**
     * delete indexing files from the directory based on the given path
     *
     * @param path - String - path to a directory containing the indexing files
     * @return - boolean - true if the deletion was complected successfully
     */
    public static boolean deleteIndexingFilesFromDirectory(String path) {
        File stemmedDirectory = new File(path + "\\Stemmed");
        File unstemmedDirectory = new File(path + "\\UnStemmed");


        if (!stemmedDirectory.exists() && !unstemmedDirectory.exists()) {
            return false;
        }

        boolean clearSuccessful = true;
        if (stemmedDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(stemmedDirectory);
            } catch (IOException e) {
                clearSuccessful = false;
            }
        }

        if (unstemmedDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(unstemmedDirectory);
            } catch (IOException e) {
                clearSuccessful = false;
            }
        }

        return clearSuccessful;
    }

    /**
     * Saves the given DocumentsEntities Map to the hard drive as an object.
     *
     * @param allDocumentsEntities - HashMap<String, HashMap<String, Integer>> - a DocumentsEntities map to be saved.
     */
    public static void saveDocumentEntities(HashMap<String, HashMap<String, Integer>> allDocumentsEntities) {

        String filePath = filesPath + "\\Entities\\DocumentsEntities";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeObject(allDocumentsEntities);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a DocumentsEntities Map from the hard drive and returns it.
     *
     * @param path - A path to a directory containing the DocumentEntities Map.
     * @return - HashMap<String, HashMap<String, Integer>> - A DocumentsEntities map.
     */
    public static HashMap<String, HashMap<String, Integer>> loadDocumentEntities(String path) {
        String entitiesPath = path + "\\Entities\\DocumentsEntities";
        FileInputStream fileInputStream;
        HashMap<String, HashMap<String, Integer>> allDocumentsEntities = null;

        try {
            fileInputStream = new FileInputStream(entitiesPath);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            allDocumentsEntities = (HashMap<String, HashMap<String, Integer>>) objectInputStream.readObject();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return allDocumentsEntities;
    }

    /**
     * Retrieves a single posting line for a given term.
     *
     * @param term         - String - A term to retrieve by.
     * @param postingIndex - string - The name of the posting directory
     * @return - ArrayList<Pair<String, Integer>> - Pair: documentID --> term frequency.
     */
    public static ArrayList<Pair<String, Integer>> retrievePosting(String term, String postingIndex) {
        String postingFilePath = filesPath + "\\PostingFiles\\" + postingIndex + "\\posting";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader((new FileReader(postingFilePath)));
            String line = "";
            while ((line = reader.readLine()) != null) {
                if (term.equals(line.substring(0, line.indexOf('!')))) {
                    String s = line;
                    reader.close();
                    return getTermPairs(s);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns all the (docID, tf) pairs from a given posting line.
     *
     * @param line - String - A posting line.
     * @return - ArrayList<Pair<String, Integer>> - Pair: documentID --> term frequency.
     */
    private static ArrayList<Pair<String, Integer>> getTermPairs(String line) {
        line = line.substring(line.indexOf('!') + 1);
        //String[] allPairs = pipelinePattern.split(line);
        String[] allPairs = line.split("[|]");
        ArrayList<Pair<String, Integer>> result = new ArrayList<>();
        for (int i = 0; i < allPairs.length; i++) {
            try {
                String documentID = allPairs[i].substring(1, allPairs[i].indexOf(','));
                int termFrequency = Integer.parseInt(allPairs[i].substring(allPairs[i].indexOf(',') + 1, (allPairs[i].length() - 1)));
                Pair<String, Integer> pair = new Pair<>(documentID, termFrequency);
                result.add(pair);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("");
            }
        }
        return result;
    }

    /**
     * Setter for the filePath
     *
     * @param path - String - An absolute file path
     */
    public static void setFilePath(String path) {
        filesPath = path;
    }

    /**
     * Saves the given query retrieval results list to the hard drive as text.
     *
     * @param path              - An absolute path to a file to be written to.
     * @param latestQueryResult - ArrayList<Pair<String, ArrayList<String>>> - Query results
     */
    public static void saveRetrievalResults(String path, ArrayList<Pair<String, ArrayList<String>>> latestQueryResult) {
        if (path != null) {
            //WRITE TO DISK
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(path));
                for (Pair<String, ArrayList<String>> pair : latestQueryResult) {
                    String queryNum = pair.getKey();
                    ArrayList<String> documents = pair.getValue();
                    for (String documentNumber : documents) {
                        String outLine = queryNum + " 0 " + documentNumber + " 1 42.38 mt";
                        writer.write(outLine);
                        writer.newLine();
                    }
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

