package CorpusProcessing;

import javafx.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    private static int invertedIndexIndex = 0;

    private static ArrayList<String> documentsDetails = new ArrayList<>();

    private static ReentrantLock documentsDetailsMutex;
    private static ReentrantLock invertedIndexMutex;
    private static ReentrantLock postingEntriesMutex;

    private static String filesPath;

    private static int numberOfPostingPortions = 0;
    private static AtomicInteger longestPostingEntriesFile = new AtomicInteger(0);


    public static void start(String path) {
        filesPath = path + "";
        documentsDetailsMutex = new ReentrantLock();
        postingEntriesMutex = new ReentrantLock();
        invertedIndexMutex=new ReentrantLock();
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

    public static int getNumberOfPostingPortions() {
        return numberOfPostingPortions;
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

    public static ArrayList<Trio> loadPostingEntree(int postingEntreeIndex) {
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
        numberOfPostingPortions = fileNumber;
    }

    public static int getInvertedIndexIndex() {
        return invertedIndexIndex;
    }

    public static void saveInvertedIndex(Map<String, PriorityQueue<Pair<String, Integer>>> posting)
    {
        //todo: fill this function!
    }

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

    public static void saveFinalPostingFile(ArrayList<String> listWithoutEntity, String filePath) {
        File file = new File(filePath);
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            List<String> allPosting = new LinkedList<>();
            for (int i = 0; i < listWithoutEntity.size(); i++) {
                fileWriter.write(listWithoutEntity.get(i)+"\n");
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /**
     * Saves an individual posting file.
     * @param posting
     * @param filePath
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
        saveDocumentationFiles();
        deleteAllTemporaryFiles();
    }

    private static void deleteAllTemporaryFiles() {
        //TODO:Delete all temp Files
    }

    private static void saveDocumentationFiles() {
        //TODO: save all the counters and the information needed for the reconstruction of the dictionary (if necessary)
    }

    public static void saveEntitiesSets(HashSet<String> entities, HashSet<String> singleAppearanceEntities) {
        //TODO: Save all the given lists as objects
    }

    public static void saveDictionary(Map<String, Pair<Integer, String>> dictionary) {
        if (filesPath != null) {
            //WRITE TO DISK! 
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(filesPath + "\\Dictionary\\dictionary"));
                for (Map.Entry<String, Pair<Integer, String>> entry : dictionary.entrySet()) {
                    String key = entry.getKey();
                    Pair<Integer, String> pair = entry.getValue();
                    String outLine = key + "~" + pair.getKey() + "," + pair.getValue(); //TODO: Maybe use a different delimiter then ","
                    writer.write(outLine);
                    writer.newLine();
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static Map<String, Pair<Integer, String>> loadDictionary(String dictionaryPath) {
        BufferedReader reader = null;
        Map<String, Pair<Integer, String>> dictionary = new HashMap<>();
        try {
            reader = new BufferedReader((new FileReader(dictionaryPath + "\\Dictionary\\dictionary")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                String term = line.substring(0,line.indexOf('~'));
                line = line.substring(line.indexOf('~')+1);
                String[] entreeDetails = line.split(",");
                dictionary.put(term, new Pair<>(Integer.parseInt(entreeDetails[0]), entreeDetails[1]));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

/*
    public static Map<String, PriorityQueue<Pair<String, Integer>>> loadPostingFile(String path) {
        Map<String, PriorityQueue<Pair<String, Integer>>> postingResult = new TreeMap<>();
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader((new FileReader(path)));
            String line = "";
            while((line = bufferedReader.readLine()) !=null)
            {
                int indexOfTilde = line.indexOf('~');
                String term = line.substring(0 , indexOfTilde);
                line = line.substring(indexOfTilde+1);
                PriorityQueue<Pair<String, Integer>> pairs =new PriorityQueue<>(new PairComparator());
                while(line.length()>1) //until the last character '|'
                {
                    String pair = line.substring(1, line.indexOf(">"));
                    String key = pair.substring(0,pair.indexOf(','));
                    String value=pair.substring(pair.indexOf(',')+1);
                    Pair pairToAdd = new Pair(key , Integer.parseInt(value));
                    pairs.add(pairToAdd);
                    line = line.substring(line.indexOf('|'));
                    if(line.length() > 1)
                    {
                        line = line.substring(1);
                    }
                }
                postingResult.put(term, pairs);

            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return postingResult;
    }


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
     * @param path - String - The directory absolute path
     */
    public static void deleteAllFilesFromDirectory(String path) {
        File directory = new File(path);
        File[] postingFiles = directory.listFiles();
        for (int i = 0; i < postingFiles.length; i++) {
            postingFiles[i].delete();
        }
    }


    public static boolean deleteIndexingFilesFromDirectory(String path) {
        File entitiesDirectory = new File(filesPath + "\\Entities");
        File documentsDetailsDirectory = new File(filesPath + "\\DocumentsDetails");
        File postingFilesDirectory = new File(filesPath + "\\PostingFiles");
        File dictionaryDirectory = new File(filesPath + "\\Dictionary");
        boolean clearSuccessful = true;
        clearSuccessful = clearSuccessful && entitiesDirectory.delete();
        clearSuccessful = clearSuccessful && documentsDetailsDirectory.delete();
        clearSuccessful = clearSuccessful && postingFilesDirectory.delete();
        clearSuccessful = clearSuccessful && dictionaryDirectory.delete();
        return clearSuccessful;
    }
}

