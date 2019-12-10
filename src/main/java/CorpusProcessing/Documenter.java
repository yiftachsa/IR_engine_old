package CorpusProcessing;

import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Responsible to write to Disk.
 * Receive the information about specific document(Doc ID , Max tf , number of uniq words)
 */
public class Documenter {

    private static final int NUMBEROFDOCUMENTSPERFILE = 200;
    // private static final int NUMBEROFCATEGORIES = 27;
    // private static final int TRIOBUFFERSIZE = 10000;
    private static final int NUMBEROFPOSTINGLINES = (int) Math.pow(2, 20);


    public static void setPath(String path) {
        filesPath = path + "";
    }

    private static int fileIndex = 0;
    private static int postingEntriesIndex = 0;
    private static ArrayList<String> documentsDetails = new ArrayList<>();
    private static String filesPath;
    private static int iterationNumber = 0;
    private static int longestPostingEntriesFile = 0;

    public static int getIterationNumber() {
        return iterationNumber;
    }


    public static String getFilePathToPostingEntries() {
        return filesPath + "\\postingEntries";
    }

    public static void saveDocumentDetails(String docId, int maxTermFrequency, int uniqTermsCount) {
        if (filesPath != null) {
            documentsDetails.add(docId + "," + maxTermFrequency + "," + uniqTermsCount);
            if (documentsDetails.size() >= NUMBEROFDOCUMENTSPERFILE) {
                //WRITE TO DISK! 
                BufferedWriter writer = null;
                try {
                    if (fileIndex == 0) {
                        new File(filesPath + "\\DocumentsDetails").mkdir();
                    }
                    writer = new BufferedWriter(new FileWriter(filesPath + "\\DocumentsDetails\\" + fileIndex));
                    for (String documentDetails : documentsDetails) {
                        writer.write(documentDetails);
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
    }

    public static int getNUMBEROFPOSTINGLINES() {
        return NUMBEROFPOSTINGLINES;
    }

    public static void savePostingEntries(ArrayList<ArrayList<Trio>> allPostingEntriesLists) {
        if (filesPath != null) {
            if (postingEntriesIndex == 0) {
                new File(filesPath + "\\postingEntries").mkdir();
            }
            String filePath = filesPath + "\\postingEntries\\" + postingEntriesIndex;
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
                outputStream.writeObject(allPostingEntriesLists);
                if (Documenter.longestPostingEntriesFile < allPostingEntriesLists.size()) {
                    Documenter.longestPostingEntriesFile = allPostingEntriesLists.size();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            postingEntriesIndex++;
        }
    }

    public static ArrayList<Trio> loadPostingEntree() {
        return null;
    }

    public static void mergeAllPostingEntries() {
        final int THREADPOOLSIZE = 2;
        ExecutorService documentLoadersPool = Executors.newFixedThreadPool(THREADPOOLSIZE); //FIXME:MAGIC NUMBER
        ArrayList<Future<ArrayList<Trio>>> futureLoaders = new ArrayList<>();
        ArrayList<ArrayList<Trio>> allPostingEntriesPortions = new ArrayList<>();
        ExecutorService mergersPool = Executors.newFixedThreadPool(4); //FIXME:MAGIC NUMBER
        ArrayList<Future<ArrayList<Trio>>> futuresMerge = new ArrayList<>();
        for (int i = 0; i < Documenter.longestPostingEntriesFile / Documenter.NUMBEROFPOSTINGLINES; i++) {
            //If there is 1000 postingEntries, will be 998 threads that will wait - is that ok? memory complexity!!!!
            while (CallableRead.getIndexDoc().get() < postingEntriesIndex) {
                Future<ArrayList<Trio>> futureRead = documentLoadersPool.submit(new CallableRead());
                futureLoaders.add(futureRead);
                if (futureLoaders.size() > 0) {
                    if (futureLoaders.get(0).isDone()) {
                        Future<ArrayList<Trio>> finishedFuture = futureLoaders.remove(0);
                        try {
                            allPostingEntriesPortions.add(finishedFuture.get());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (allPostingEntriesPortions.size() > 1) {
                    Future<ArrayList<Trio>> futureMerge = mergersPool.submit(new CallableMerge(allPostingEntriesPortions));
                    futuresMerge.add(futureMerge);
                }
                if (futuresMerge.size() > 0) {
                    if (futuresMerge.get(0).isDone()) {
                        Future<ArrayList<Trio>> futureMerge = futuresMerge.remove(0);
                        try {
                            allPostingEntriesPortions.add(futureMerge.get());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            //Waiting for all the files to be loaded and merged
            while (futureLoaders.size() > 0 || futuresMerge.size() > 0) {
                if (futureLoaders.get(0).isDone()) {
                    Future<ArrayList<Trio>> finishedFuture = futureLoaders.remove(0);
                    try {
                        allPostingEntriesPortions.add(finishedFuture.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (allPostingEntriesPortions.size() > 1) {
                        Future<ArrayList<Trio>> futureMerge = mergersPool.submit(new CallableMerge(allPostingEntriesPortions));
                        futuresMerge.add(futureMerge);
                    }
                } else if (futuresMerge.get(0).isDone()) {
                    Future<ArrayList<Trio>> futureMerge = futuresMerge.remove(0);
                    try {
                        allPostingEntriesPortions.add(futureMerge.get());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (allPostingEntriesPortions.size() > 1) {
                        Future<ArrayList<Trio>> nextFutureMerge = mergersPool.submit(new CallableMerge(allPostingEntriesPortions));
                        futuresMerge.add(nextFutureMerge);
                    }
                }
            }
            for (Future<ArrayList<Trio>> future : futuresMerge) {
                while (!future.isDone()) ;
                try {
                    allPostingEntriesPortions.add(future.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mergersPool.shutdown();
            while (allPostingEntriesPortions.size() > 1) {
                allPostingEntriesPortions.add(Mapper.mergeAndSortTwoPostingEntriesLists(allPostingEntriesPortions.remove(0), allPostingEntriesPortions.remove(0)));
            }
            Documenter.savePostingPortions(allPostingEntriesPortions.get(0), iterationNumber);
            iterationNumber++;
            CallableRead.setIterationNumber(iterationNumber);
        }
    }

    private static void savePostingPortions(ArrayList<Trio> allPostingEntriesPortions, int fileNumber) {
        if (filesPath != null) {
            if (fileNumber == 0) {
                new File(filesPath + "\\postingPortions").mkdir();
            }
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
            postingEntriesIndex++;
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


    public static void saveEntities(TreeSet<String> entitiesTreeSet) {

        new File(filesPath + "\\entities").mkdir();
        String filePath = filesPath + "\\entities\\entities";
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
            outputStream.writeObject(entitiesTreeSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

