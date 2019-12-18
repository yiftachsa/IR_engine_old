package Test;

import CorpusProcessing.*;
import Model.MyModel;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.*;

public class TestsPart1 {

    public static void main(String[] args) {
        Parse parser = new Parse( new HashSet<>(), new HashSet<>(),false);

        //ReadFile_separateFileToDocuments_Test1();
        //ReadFile_separateFileToDocuments_Test2()
        //parseTest(parser);
        //Parse_parseDocument_Test1_parseDocument(parser);
        //Mapper_Test1();
        //Mapper_Test2_mergeAndSortTwoPostingEntriesLists();
        //Model_MergerThreads_test1();
        //Model_Test2_entireCorpus();
        Model_Test2_100DocsTest();
        //testParse();
    }


    private static boolean ReadFile_separateFileToDocuments_Test1() {
        boolean result = false;
        String filePath = "C:\\scripts\\Courses_Scripts\\Information_Retrieval\\IR_Engine\\Data\\corpus\\corpus\\FB396001";
        ArrayList<Document> documentsList = ReadFile.separateFileToDocuments(filePath);
        for (int i = 0; i < documentsList.size(); i++) {
            Document document = documentsList.get(i);
            System.out.println(document.getId());
            System.out.println(document.getHeader());
            System.out.println(document.getText());
        }
        return result;
    }


    private static void ReadFile_separateFileToDocuments_Test2() {
        String filePath = "C:Users\\Desktop\\corpus\\FB396001\\FB396001";
        ArrayList<Document> documentsList = ReadFile.separateFileToDocuments(filePath);

    }



    /*
    System.out.println("\nsentenceTest");
    ArrayList<String> sentenceTest = parser.parseText(new Document("0","","I am (Merav). I am not amused/invested/interested in this code. I would like to pay (10,000) dollars for someone to replace me!"), false);
    for (int i = 0; i < sentenceTest.size(); i++) {
        System.out.println(sentenceTest.get(i));
    }
*/


    private static boolean Mapper_Test1() {
        boolean result = false;
        ArrayList<String> bagOfWords = new ArrayList<String>(Arrays.asList("B", "c", "B", "a", "b", "c", "a", "A"));
        Documenter.start("D:\\Documents\\Studies\\Documents for higher education\\Courses\\Year 3 Semester 1\\אחזור מידע\\TestIREngine");
        ArrayList<Trio> postingsEntries = Mapper.processBagOfWords("Doc1", bagOfWords);
        for (Trio trio : postingsEntries) {
            System.out.println(trio);
        }

        return result;
    }

    private static boolean Mapper_Test2_mergeAndSortTwoPostingEntriesLists() {
        boolean result = false;
        ArrayList<String> bagOfWords1 = new ArrayList<String>(Arrays.asList("B", "c", "B", "a", "b", "c", "a", "A"));
        ArrayList<String> bagOfWords2 = new ArrayList<String>(Arrays.asList("B", "b", "d", "q"));
        Documenter.start("D:\\Documents\\Studies\\Documents for higher education\\Courses\\Year 3 Semester 1\\אחזור מידע\\TestIREngine");
        ArrayList<Trio> postingsEntries1 = Mapper.processBagOfWords("Doc1", bagOfWords1);
        ArrayList<Trio> postingsEntries2 = Mapper.processBagOfWords("Doc2", bagOfWords2);
        ArrayList<Trio> mergedList = Mapper.mergeAndSortTwoPostingEntriesLists(postingsEntries1, postingsEntries2);
        for (Trio trio : mergedList) {
            System.out.println(trio);
        }
        return result;
    }

    private static boolean Model_MergerThreads_test1() {
        ArrayList<ArrayList<Trio>> allPostingEntriesLists = new ArrayList<>();
        ExecutorService mergersPool = Executors.newFixedThreadPool(4); //FIXME:MAGIC NUMBER
        ArrayList<Future<ArrayList<Trio>>> futures = new ArrayList<>();
        ArrayList<String> bagOfWords1 = new ArrayList<String>(Arrays.asList("B", "c", "B", "a", "b", "c", "a", "A"));
        ArrayList<String> bagOfWords2 = new ArrayList<String>(Arrays.asList("B", "b", "d", "q"));
        ArrayList<String> bagOfWords3 = new ArrayList<String>(Arrays.asList("C", "F", "h", "m"));
        Documenter.start("D:\\Documents\\Studies\\Documents for higher education\\Courses\\Year 3 Semester 1\\אחזור מידע\\TestIREngine");
        ArrayList<Trio> postingsEntries1 = Mapper.processBagOfWords("Doc1", bagOfWords1);
        ArrayList<Trio> postingsEntries2 = Mapper.processBagOfWords("Doc2", bagOfWords2);
        ArrayList<Trio> postingsEntries3 = Mapper.processBagOfWords("Doc3", bagOfWords3);

        allPostingEntriesLists.add(postingsEntries1);
        allPostingEntriesLists.add(postingsEntries2);
        allPostingEntriesLists.add(postingsEntries3);

        if (allPostingEntriesLists.size() >= 2) {
            Future<ArrayList<Trio>> future = mergersPool.submit(new CallableMerge(allPostingEntriesLists.remove(0),allPostingEntriesLists.remove(0)));
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
        for (Future<ArrayList<Trio>> future : futures) {
            while (!future.isDone()) ;
            try {
                allPostingEntriesLists.add(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mergersPool.shutdown();
        while (allPostingEntriesLists.size() > 1) {
            allPostingEntriesLists.add(Mapper.mergeAndSortTwoPostingEntriesLists(allPostingEntriesLists.remove(0), allPostingEntriesLists.remove(0)));
        }

        Documenter.shutdown();

        for (Trio trio : allPostingEntriesLists.get(0)) {
            System.out.println(trio);
        }
        System.out.println(allPostingEntriesLists.size());

        return false;
    }


    public static void Model_Test2_entireCorpus(){
        String corpusPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Data";
        String resultPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Result";
        MyModel myModel = new MyModel();
        myModel.start(corpusPath,resultPath);


        /*final int NUMBEROFDOCUMENTPROCESSORS = 4;
        final int NUMBEROFDOCUMENTPERPARSER = 5;



        Documenter.start(resultPath);
        //initializing the stop words set
        Parse.loadStopWords(corpusPath);
        File Corpus = new File(corpusPath);
        File[] directories = Corpus.listFiles();
        int currentDirectoryIndex = 0;

        //ExecutorService documentProcessorsPool = Executors.newFixedThreadPool(NUMBEROFDOCUMENTPROCESSORS); //FIXME:MAGIC NUMBER
        Thread[] threads = new Thread[NUMBEROFDOCUMENTPROCESSORS];
        RunnableParse[] runnableParses = new RunnableParse[NUMBEROFDOCUMENTPROCESSORS];

        for (int i = 0; i < threads.length; i++) {
            HashSet<String> entities = new HashSet<>();
            HashSet<String> singleAppearanceEntities = new HashSet<>();

            RunnableParse runnableParse = new RunnableParse(entities, singleAppearanceEntities, stemming);
            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER));
            runnableParses[i] = runnableParse;
            currentDirectoryIndex = currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER;

            threads[i] = new Thread(runnableParse);
            threads[i].start();
        }

        while (currentDirectoryIndex < directories.length - NUMBEROFDOCUMENTPERPARSER) {
            int finishedThreadIndex = getFinishedThreadIndex(threads);
            RunnableParse runnableParse = runnableParses[finishedThreadIndex];
            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER));
            threads[finishedThreadIndex] = new Thread(runnableParse);
            threads[finishedThreadIndex].start();
            currentDirectoryIndex = currentDirectoryIndex + NUMBEROFDOCUMENTPERPARSER;
        }
        int numberOfDocumentsLeft = NUMBEROFDOCUMENTPERPARSER - currentDirectoryIndex;
        if(numberOfDocumentsLeft > 0) {
            int finishedThreadIndex = getFinishedThreadIndex(threads);
            RunnableParse runnableParse = runnableParses[finishedThreadIndex];
            runnableParse.setFilesToParse(Arrays.copyOfRange(directories, currentDirectoryIndex, currentDirectoryIndex + numberOfDocumentsLeft));
            threads[finishedThreadIndex] = new Thread(runnableParse);
            threads[finishedThreadIndex].start();
        }

        //merge all the parsers from the RunnableParse
        HashSet<String> allSingleAppearanceEntities = getExcludedEntitiesAndSaveEntitiesToFile(threads, runnableParses);

        //merge all the individuals posting entries and sort them
        Documenter.mergeAllPostingEntries();
        //now we have sorted posting entries files and we can iterate through them based on term name
        Indexer indexer = new Indexer(resultPath , allSingleAppearanceEntities);
        indexer.buildInvertedIndex();

        //Closing all open ends
        Documenter.shutdown();

         */
    }


    public static void Model_Test2_100DocsTest(){
        String corpusPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Data";
        String resultPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Result";
        MyModel myModel = new MyModel();
        myModel.start(corpusPath,resultPath);
    }

    public static void Model_Test2_300DocsTest(){
        String corpusPath = "C:\\scripts\\Courses_Scripts\\Information_Retrieval\\IR_Engine\\Test Files\\300DocsTest\\corpus";
        String resultPath = "C:\\scripts\\Courses_Scripts\\Information_Retrieval\\IR_Engine\\Test Files\\300DocsTest\\Output";
        MyModel myModel = new MyModel();
        myModel.start(corpusPath,resultPath);
    }

    public static void testParse(){
        boolean result = false;
        Parse parser = new Parse(new HashSet<>() , new HashSet<>() , false);
        String filePath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Data10\\corpus\\FB396001\\FB396001";
        ArrayList<Document> documentsList = ReadFile.separateFileToDocuments(filePath);
        HashMap<String , Pair<String , Integer>> bagOfWords = parser.parseDocument(documentsList.get(0));
        for (HashMap.Entry<String , Pair<String , Integer>> record : bagOfWords.entrySet())
        {
            System.out.println(record.getKey());
        }

    }


    private static int totalPostingEntries(int numberOfFilesPerThread){
        final int totalFilesInCorpus = 1815;
        int totalPostingEntries = totalFilesInCorpus/numberOfFilesPerThread;
        return totalPostingEntries;
    }


    private static double averageTimePerFile(double[] testsTimes, int numberOfFilesPerThread){
        int totalFilesInTest = testsTimes.length * numberOfFilesPerThread;
        double totalTestTime = 0;
        for (int i = 0; i < testsTimes.length; i++) {
            totalTestTime = totalTestTime + testsTimes[i];
        }
        double averageTimePerFile = totalTestTime/totalFilesInTest;
        return averageTimePerFile;
    }



}
