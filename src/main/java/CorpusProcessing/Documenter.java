package CorpusProcessing;

import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Responsible to write to Disk.
 * Receive the information about specific document(Doc ID , Max tf , number of uniq words)
 */
//TODO:Maybe should be singleton
public class Documenter {

    // private static final int NUMBEROFDOCUMENTSPERFILE = (int) Math.pow(2, 12);
    // private static final int NUMBEROFCATEGORIES = 27;
    // private static final int TRIOBUFFERSIZE = 10000;
    private static int fileIndex = 0;


//    private static final int LOADERSPOOLSIZE = 2;

    private static int postingEntriesIndex = 0;
    private static ArrayList<String> documentsDetails = new ArrayList<>();

    private static ReentrantLock documentsDetailsMutex;
    private static ReentrantLock postingEntriesMutex;

    private static String filesPath;

    private static int iterationNumber = 0;
    private static AtomicInteger longestPostingEntriesFile = new AtomicInteger(0);


    public static void start(String path) {
        filesPath = path + "";
        documentsDetailsMutex = new ReentrantLock();
        postingEntriesMutex = new ReentrantLock();
        new File(filesPath + "\\entities").mkdir();
        new File(filesPath + "\\postingEntries").mkdir();
        new File(filesPath + "\\DocumentsDetails").mkdir();
        new File(filesPath + "\\postingPortions").mkdir();
    }

    public static int getIterationNumber() {
        return iterationNumber;
    }

    public static int getPostingEntriesIndex() {
        return postingEntriesIndex;
    }

    public static int getDocumentsDetailsSize() {
        return documentsDetails.size();
    }

    public static int getLongestPostingEntriesFile() {
        return longestPostingEntriesFile.get();
    }

    /**
     * Returns the path of the posting entries files
     *
     * @return - String - the path of the posting entries files
     */
    public static String getFilePathToPostingEntries() {
        return filesPath + "\\postingEntries";
    }


    /**
     * Receives details about a document and adds it to the field documentsDetails list.
     *
     * @param docId            - String - uniq document identifier
     * @param maxTermFrequency - int - the maximum term frequency in the document
     * @param uniqTermsCount   - int - the number of uniq terms in the document
     * @param length           - int - the total number of terms in the document
     */
    public static void saveDocumentDetails(String docId, int maxTermFrequency, int uniqTermsCount, int length) {
        if (filesPath != null) {
            documentsDetailsMutex.lock();
            documentsDetails.add(docId + "," + maxTermFrequency + "," + uniqTermsCount + "," + length);
            documentsDetailsMutex.unlock();
            /*
            if (documentsDetails.size() >= NUMBEROFDOCUMENTSPERFILE) {
                //WRITE TO DISK! 
                saveDocumentsDetailsToFile();
            }
             */
        }
    }

    /**
     * Saves the documentsDetails field to file
     */
    private static void saveDocumentsDetailsToFile() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filesPath + "\\DocumentsDetails\\DocumentsDetails"));
            for (String documentDetails : documentsDetails) {
                writer.write(documentDetails);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //fileIndex++;
        //documentsDetails = new ArrayList<>();
    }

    /**
     * @param postingEntriesList
     */
    public static void savePostingEntries(ArrayList<Trio> postingEntriesList) {
        if (filesPath != null) {

            postingEntriesMutex.lock();
            String filePath = filesPath + "\\postingEntries\\" + postingEntriesIndex;
            postingEntriesIndex++;
            postingEntriesMutex.unlock();

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
                outputStream.writeObject(postingEntriesList);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (longestPostingEntriesFile.get() < postingEntriesList.size()) {
                longestPostingEntriesFile.set(postingEntriesList.size());
            }

        }
    }

    public static ArrayList<Trio> loadPostingEntree(int postingEntreeIndex)
    {
        FileInputStream fileInputStream = null;
        ArrayList<Trio> trioArrayList = null;

        try {
            fileInputStream = new FileInputStream(filesPath + "\\postingEntries\\" + postingEntreeIndex);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            trioArrayList = (ArrayList<Trio>) objectInputStream.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return trioArrayList;
    }


    static void savePostingPortions(ArrayList<Trio> allPostingEntriesPortions, int fileNumber) {
        if (filesPath != null) {
            String filePath = filesPath + "\\postingPortions\\" + fileNumber;
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
                outputStream.writeObject(allPostingEntriesPortions);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveInvertedIndex(SortedMap<String, ArrayList<Pair<String, Integer>>> posting, int index) {
        if (filesPath != null) {
            //WRITE TO DISK! 
            BufferedWriter writer = null;
            try {
                if (index == 0) {
                    new File(filesPath + "\\PostingFiles").mkdir();
                }
                writer = new BufferedWriter(new FileWriter(filesPath + "\\PostingDetails\\" + index));
                for (SortedMap.Entry<String, ArrayList<Pair<String, Integer>>> postingLine : posting.entrySet()) {
                    String term = postingLine.getKey();
                    ArrayList<Pair<String, Integer>> pairs = postingLine.getValue();
                    String out = term + "~";
                    for (Pair<String, Integer> pair : pairs) {
                        out = out + "<" + pair.getKey() + "," + pair.getValue() + ">";
                    }
                    writer.write(out);
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileIndex++;
            documentsDetails = new ArrayList<>();
        }
    }

    /**
     * Saves a given String TreeSet to a file as an Object.
     *
     * @param treeSetToSave - TreeSet<String> - an ordered set of Strings
     */
    public static void saveEntities(TreeSet<String> treeSetToSave) {
        String filePath = filesPath + "\\entities\\entities";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeObject(treeSetToSave);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves all the data structures which are in the memory and need to be saved for later use
     */
    public static void shutdown() {
        if (documentsDetails.size() > 0) {
            saveDocumentsDetailsToFile();
        }
        saveDocumentationFiles();
    }

    private static void saveDocumentationFiles() {
        //TODO: save all the counters and the information needed for the reconstruction of the dictionary (if necessary)
    }

    public static void saveEntitiesSets(HashSet<String> entities, HashSet<String> singleAppearanceEntities) {
        //TODO: Save all the given lists as objects
    }
}

