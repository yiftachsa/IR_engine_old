package Test;

import CorpusProcessing.*;
import Model.MyModel;
import View.RetrievalResultView;
import ViewModel.MyViewModel;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TestsPart1 {

    public static void main(String[] args) throws IOException {
        Parse parser = new Parse(new HashSet<>(), new HashSet<>(), false);
        // parserTest(parser);
        // Model_Test3_2DocsTest();
        //ReadFile_separateFileToDocuments_Test2()
        //parseTest(parser);
        //Parse_parseDocument_Test1_parseDocument(parser);
        //Mapper_Test1();
        //Mapper_Test2_mergeAndSortTwoPostingEntriesLists();
        //Model_MergerThreads_test1();
//        Model_Test2_100DocsTest();
        optimizeRankerWeights();
        //RetrievalResultView_Test1();
        //test30();

    }

    private static void test30() {
        String s = "currentLine.contains(\"71546\")||currentLine.contains(\"72692\")||currentLine.contains(\"72987\")||currentLine.contains(\"94579\")||currentLine.contains(\"94795\")||currentLine.contains(\"118042\")||currentLine.contains(\"118123\")||currentLine.contains(\"118190\")||currentLine.contains(\"118319\")||currentLine.contains(\"131919\") ||currentLine.contains(\"132603\")||currentLine.contains(\"139474\")||currentLine.contains(\"141683\")||currentLine.contains(\"145257\")||currentLine.contains(\"146408\")||currentLine.contains(\"146867\")||currentLine.contains(\"151104\")||currentLine.contains(\"151251\")||currentLine.contains(\"157416\")||currentLine.contains(\"158450\")||currentLine.contains(\"159991\")||currentLine.contains(\"165901\")||currentLine.contains(\"177745\")||currentLine.contains(\"190208\")||currentLine.contains(\"193844\")||currentLine.contains(\"195735\")||currentLine.contains(\"214327\")||currentLine.contains(\"214646\")||currentLine.contains(\"222915\")||currentLine.contains(\"224450\")||currentLine.contains(\"225423\")||currentLine.contains(\"25627\")||currentLine.contains(\"232143\")||currentLine.contains(\"236685\")||currentLine.contains(\"245951\")||currentLine.contains(\"251515\")||currentLine.contains(\"253636\")||currentLine.contains(\"256550\")||currentLine.contains(\"262902\")||currentLine.contains(\"263799\")||currentLine.contains(\"287999\")||currentLine.contains(\"308081\")||currentLine.contains(\"308433\")||currentLine.contains(\"323218\")||currentLine.contains(\"331454\")||currentLine.contains(\"335156\")||currentLine.contains(\"335858\")||currentLine.contains(\"343429\")||currentLine.contains(\"355892\")||currentLine.contains(\"384766\")||currentLine.contains(\"438713\")||currentLine.contains(\"439846\")||currentLine.contains(\"443109\")||currentLine.contains(\"449105\")||currentLine.contains(\"453202\")||currentLine.contains(\"463431\")||currentLine.contains(\"464947\")";
        s = s.replaceAll("currentLine", "documentId");
        System.out.println(s);
    }

    public static void Model_Test3_2DocsTest() {
        String corpusPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Data10";
        String resultPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Result";
        MyModel myModel = new MyModel();
        myModel.start(corpusPath, resultPath);
    }

    private static boolean parserTest(Parse parse) throws IOException {
        boolean result = false;
        String filePath = "C:\\Users\\Merav\\Desktop\\testtt";
        BufferedReader reader = null;
        String query = "";
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line = "";
            while ((line = reader.readLine()) != null) {

                query = query + " " + line;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> stringArrayList = parse.parseQuery(query);
        for (int i = 0; i < stringArrayList.size(); i++) {
            System.out.println(stringArrayList.get(i));
        }


        return result;
    }


    public static void RetrievalResultView_Test1(MyViewModel viewModel) {
        ArrayList<Pair<String, ArrayList<String>>> pairs = new ArrayList<>();
        ArrayList<String> stringArrayList = new ArrayList<>();
        stringArrayList.add("query1-first");
        stringArrayList.add("query1-second");

        Pair<String, ArrayList<String>> newPair = new Pair<>("query1", stringArrayList);
        pairs.add(newPair);
        stringArrayList = new ArrayList<>();
        stringArrayList.add("query2-first");
        stringArrayList.add("query2-second");
        stringArrayList.add("query2-third");


        newPair = new Pair<>("query2", stringArrayList);
        pairs.add(newPair);


        RetrievalResultView.display("this is a title", pairs, viewModel);
    }


    private static void ReadFile_separateFileToDocuments_Test1() {
        boolean result = false;
        String filePath1 = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Data\\corpus";

        File Corpus = new File(filePath1);
        File[] filesToParse = Corpus.listFiles();
        for (File directory : filesToParse) {
            String filePath = directory.listFiles()[0].getAbsolutePath();
            if (Files.isReadable(Paths.get(filePath))) {
                System.out.println("boom");
                ArrayList<Document> documents = CorpusProcessing.ReadFile.separateFileToDocuments(filePath);
            }
        }
    }


    private static void ReadFile_separateFileToDocuments_Test2() {
        String filePath = "C:Users\\Desktop\\corpus\\FB396001\\FB396001";
        ArrayList<Document> documentsList = ReadFile.separateFileToDocuments(filePath);

    }

    private static void parseTest(Parse parser) {
        parse_parseQuery_Test1_generateTokenDollar(parser);
        parse_parseQuery_Test2_generateTokenMonth(parser);
        parse_parseQuery_Test3_Percentage(parser);
        parse_parseQuery_Test4_Thousand(parser);
        parse_parseQuery_Test5_generateTokenLargeNumbers(parser);
        parse_parseQuery_Test6_generateTokenPrice(parser);
        parse_parseQuery_Test7_Fractions(parser);
        parse_parseQuery_Test8_generateTokenSimpleNumber(parser);
        parse_parseQuery_Test9_Between(parser);
        parse_parseQuery_Test10_FirstCustomAdd(parser);
        parse_parseQuery_Test11_generateTokensEntity(parser);
        parse_parseQuery_Test12_stemmer(parser);
    }

    /**
     * Tests generateTokenDollar
     */
    private static void parse_parseQuery_Test1_generateTokenDollar(Parse parser) {
        System.out.println("\ntestDollars");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("$20-Million 20.6m Dollars $100 billion $100 million $450,000,000 $100 391bn Dollars");
        passed = passed && testResults.get(0).equals("20.6 M Dollars");
        passed = passed && testResults.get(1).equals("100000 M Dollars");
        passed = passed && testResults.get(2).equals("100 M Dollars");
        passed = passed && testResults.get(3).equals("450 M Dollars");
        passed = passed && testResults.get(4).equals("100 Dollars");
        passed = passed && testResults.get(5).equals("391000 M Dollars");

        System.out.println("passed: " + passed);
    }

    /**
     * Tests generateTokenMonth and generateTokenDayMonth
     */
    private static void parse_parseQuery_Test2_generateTokenMonth(Parse parser) {
        System.out.println("\ntestMonthDate");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("June 4 4 Aug Aug 24 November 2");
        passed = passed && testResults.get(0).equals("06-04");
        passed = passed && testResults.get(1).equals("08-04");
        passed = passed && testResults.get(2).equals("08-24");
        passed = passed && testResults.get(3).equals("11-02");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests Percentage
     */
    private static void parse_parseQuery_Test3_Percentage(Parse parser) {
        System.out.println("\ntestPercentage");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("6% 6 percent 7 percentage");
        passed = passed && testResults.get(0).equals("6%");
        passed = passed && testResults.get(1).equals("6%");
        passed = passed && testResults.get(2).equals("7%");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests Thousand
     */
    private static void parse_parseQuery_Test4_Thousand(Parse parser) {
        System.out.println("\ntestThousand");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("123 Thousand 125 thousand");
        passed = passed && testResults.get(0).equals("123K");
        passed = passed && testResults.get(1).equals("125K");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests generateTokenLargeNumbers
     */
    private static void parse_parseQuery_Test5_generateTokenLargeNumbers(Parse parser) {
        System.out.println("\ntestgenerateTokenLargeNumbers");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("55 Million 60 million 70 Billion 80 billion 100 billion U.S. dollars 100 million U.S. dollars 100 trillion U.S. dollars");
        passed = passed && testResults.get(0).equals("55M");
        passed = passed && testResults.get(1).equals("60M");
        passed = passed && testResults.get(2).equals("70B");
        passed = passed && testResults.get(3).equals("80B");
        passed = passed && testResults.get(4).equals("100000 M Dollars");
        passed = passed && testResults.get(5).equals("100 M Dollars");
        passed = passed && testResults.get(6).equals("100000000 M Dollars");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests generateTokenPrice
     */
    private static void parse_parseQuery_Test6_generateTokenPrice(Parse parser) {
        System.out.println("\ntestgenerateTokenPrice");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("1.98 Dollars 1,000,000 Dollars 20 Dollars");
        passed = passed && testResults.get(0).equals("1.98 Dollars");
        passed = passed && testResults.get(1).equals("1 M Dollars");
        passed = passed && testResults.get(2).equals("20 Dollars");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests Fractions
     */
    private static void parse_parseQuery_Test7_Fractions(Parse parser) {
        System.out.println("\ntestFractions");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("100 3/4 22 1/2 Dollars 1/2 1/4 Dollars");
        passed = passed && testResults.get(0).equals("100 3/4");
        passed = passed && testResults.get(1).equals("22 1/2 Dollars");
        passed = passed && testResults.get(2).equals("1/2");
        passed = passed && testResults.get(3).equals("1/4 Dollars");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests generateTokenSimpleNumber
     */
    private static void parse_parseQuery_Test8_generateTokenSimpleNumber(Parse parser) {
        System.out.println("\ntestgenerateTokenSimpleNumber");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("123 1010.56 10,123,000 10,123,000,000");
        passed = passed && testResults.get(0).equals("123");
        passed = passed && testResults.get(1).equals("1.01K");
        passed = passed && testResults.get(2).equals("10.123M");
        passed = passed && testResults.get(3).equals("10.123B");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests Between
     */
    private static void parse_parseQuery_Test9_Between(Parse parser) {
        System.out.println("\ntestBetween");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("22-23 Between 10 and 50 between 60 and merav between merav to 60 between to");
        passed = passed && testResults.get(0).equals("22-23") && testResults.get(1).equals("22") && testResults.get(2).equals("23");
        passed = passed && testResults.get(3).equals("10-50") && testResults.get(4).equals("10") && testResults.get(5).equals("50");
        passed = passed && testResults.get(6).equals("between") && testResults.get(7).equals("60") && testResults.get(8).equals("and") && testResults.get(9).equals("merav");
        passed = passed && testResults.get(10).equals("between") && testResults.get(11).equals("merav") && testResults.get(12).equals("to") && testResults.get(13).equals("60");
        passed = passed && testResults.get(14).equals("between");
        passed = passed && testResults.get(15).equals("to");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests the first custom addition <<<Word / word>>>
     */
    private static void parse_parseQuery_Test10_FirstCustomAdd(Parse parser) {
        System.out.println("\ntestFirstCustomAdd");
        boolean passed = true;
        ArrayList<String> testResults = parser.parseQuery("amused/invested/interested");
        passed = passed && testResults.get(0).equals("amused");
        passed = passed && testResults.get(1).equals("invested");
        passed = passed && testResults.get(2).equals("interested");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests generateTokensEntity
     */
    private static void parse_parseQuery_Test11_generateTokensEntity(Parse parser) {
        System.out.println("\ntestGenerateTokensEntity");
        boolean passed = true;

        ArrayList<String> testResults = parser.parseQuery("Merav bamba Merav Shaked bamba Merav Shaked Yiftach bamba Merav Shaked Yiftach Savransky");
        passed = passed && testResults.get(0).equals("MERAV");
        passed = passed && testResults.get(1).equals("bamba");
        passed = passed && testResults.get(2).equals("MERAV SHAKED");
        passed = passed && testResults.get(3).equals("MERAV");
        passed = passed && testResults.get(4).equals("SHAKED");
        passed = passed && testResults.get(5).equals("bamba");
        passed = passed && testResults.get(6).equals("MERAV SHAKED YIFTACH");
        passed = passed && testResults.get(7).equals("MERAV");
        passed = passed && testResults.get(8).equals("SHAKED");
        passed = passed && testResults.get(9).equals("YIFTACH");
        passed = passed && testResults.get(10).equals("bamba");
        passed = passed && testResults.get(11).equals("MERAV SHAKED YIFTACH SAVRANSKY");
        passed = passed && testResults.get(12).equals("MERAV");
        passed = passed && testResults.get(13).equals("SHAKED");
        passed = passed && testResults.get(14).equals("YIFTACH");
        passed = passed && testResults.get(15).equals("SAVRANSKY");
        System.out.println("passed: " + passed);

        ArrayList<String> testResultsCaptial = parser.parseQuery("MERAV bamba MERAV SHAKED bamba MERAV SHAKED YIFTACH   bamba MERAV SHAKED YIFTACH SAVRANSKY");
        passed = passed && testResultsCaptial.get(0).equals("MERAV");
        passed = passed && testResultsCaptial.get(1).equals("bamba");
        passed = passed && testResultsCaptial.get(2).equals("MERAV SHAKED");
        passed = passed && testResultsCaptial.get(3).equals("MERAV");
        passed = passed && testResultsCaptial.get(4).equals("SHAKED");
        passed = passed && testResultsCaptial.get(5).equals("bamba");
        passed = passed && testResultsCaptial.get(6).equals("MERAV SHAKED YIFTACH");
        passed = passed && testResultsCaptial.get(7).equals("MERAV");
        passed = passed && testResultsCaptial.get(8).equals("SHAKED");
        passed = passed && testResultsCaptial.get(9).equals("YIFTACH");
        passed = passed && testResultsCaptial.get(10).equals("bamba");
        passed = passed && testResultsCaptial.get(11).equals("merav");
        passed = passed && testResultsCaptial.get(12).equals("shaked");
        passed = passed && testResultsCaptial.get(13).equals("yiftach");
        passed = passed && testResultsCaptial.get(14).equals("savransky");
        System.out.println("passed: " + passed);

        ArrayList<String> testResultsMix = parser.parseQuery("I am happy to join with you today in what will go down in history as the Greatest Demonstration For Freedom in the history Of-Our nation. I HAVE A DREAM That one day this NATION WILL RISE Up and live out the true meaning of its creed: WE HOLD THESE TRUTH TO BE SELF-EVIDENT, THAT/ALL MEN ARE CREATED EQUAL.");
        System.out.println(testResultsMix);
    }

    private static void parse_parseQuery_Test12_stemmer(Parse parser) {
        System.out.println("\ntestStemmer");
        boolean passed = true;
        parser.setUseStemmer(true);
        ArrayList<String> testResults = parser.parseQuery("sky banana studies students devastation");
        passed = passed && testResults.get(0).equals("sky");
        passed = passed && testResults.get(1).equals("banana");
        passed = passed && testResults.get(2).equals("studi");
        passed = passed && testResults.get(3).equals("student");
        passed = passed && testResults.get(4).equals("devast");
        System.out.println("passed: " + passed);

    }


    /*
    System.out.println("\nsentenceTest");
    ArrayList<String> sentenceTest = parser.parseText(new Document("0","","I am (Merav). I am not amused/invested/interested in this code. I would like to pay (10,000) dollars for someone to replace me!"), false);
    for (int i = 0; i < sentenceTest.size(); i++) {
        System.out.println(sentenceTest.get(i));
    }
*/

    private static boolean Parse_parseDocument_Test1_parseDocument(Parse parser) {
        boolean result = false;
        String filePath = "C:\\Users\\yiftachs\\Data\\ReportFB.txt";
        ArrayList<Document> documentsList = ReadFile.separateFileToDocuments(filePath);
        ArrayList<String> bagOfWords = parser.parseDocument(documentsList.get(0));
        ArrayList<TermDocumentTrio> test = Mapper.processBagOfWords(false, "", "", bagOfWords, "");
        test.sort(new Comparator<TermDocumentTrio>() {
            @Override
            public int compare(TermDocumentTrio o1, TermDocumentTrio o2) {
                return o1.getTerm().compareTo(o2.getTerm());
            }
        });


        for (TermDocumentTrio trio : test) {
            System.out.println(trio.getTerm() + " , " + trio.getFrequency());
        }
        return result;
    }
/*
    private static boolean Mapper_Test1() {
        boolean result = false;
        ArrayList<String> bagOfWords = new ArrayList<String>(Arrays.asList("B", "c", "B", "a", "b", "c", "a", "A"));
        Documenter.start("D:\\Documents\\Studies\\Documents for higher education\\Courses\\Year 3 Semester 1\\אחזור מידע\\TestIREngine", false);
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
        Documenter.start("D:\\Documents\\Studies\\Documents for higher education\\Courses\\Year 3 Semester 1\\אחזור מידע\\TestIREngine", false);
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
        ExecutorService mergersPool = Executors.newFixedThreadPool(4);
        ArrayList<Future<ArrayList<Trio>>> futures = new ArrayList<>();
        ArrayList<String> bagOfWords1 = new ArrayList<String>(Arrays.asList("B", "c", "B", "a", "b", "c", "a", "A"));
        ArrayList<String> bagOfWords2 = new ArrayList<String>(Arrays.asList("B", "b", "d", "q"));
        ArrayList<String> bagOfWords3 = new ArrayList<String>(Arrays.asList("C", "F", "h", "m"));
        Documenter.start("D:\\Documents\\Studies\\Documents for higher education\\Courses\\Year 3 Semester 1\\אחזור מידע\\TestIREngine", false);
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
*/

    public static void Model_Test2_entireCorpus() {
        String corpusPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Data";
        String resultPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Result";

        double startTime = System.currentTimeMillis() / 1000;
        MyModel myModel = new MyModel();
        myModel.setStemming(false);
        myModel.loadDictionary(resultPath);
        double endTime = System.currentTimeMillis() / 1000;
        String timePrint = "StartTime: " + startTime + " EndTime: " + endTime + " Total: " + (endTime - startTime);
        System.out.println(timePrint);

        countNumberTerms(myModel.getDictionary());
        /*final int NUMBEROFDOCUMENTPROCESSORS = 4;
        final int NUMBEROFDOCUMENTPERPARSER = 5;



        Documenter.start(resultPath);
        //initializing the stop words set
        Parse.loadStopWords(corpusPath);
        File Corpus = new File(corpusPath);
        File[] directories = Corpus.listFiles();
        int currentDirectoryIndex = 0;

        //ExecutorService documentProcessorsPool = Executors.newFixedThreadPool(NUMBEROFDOCUMENTPROCESSORS);
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

    public static void Model_Test2_100DocsTest() {
        String corpusPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Data";
        String resultPath = "C:\\Users\\Merav\\Desktop\\SemesterE\\אחזור\\Result";
        MyModel myModel = new MyModel();
        myModel.start(corpusPath, resultPath);
    }


    private static void countNumberTerms(LinkedList<Pair<String, Integer>> dictionary) {
        LinkedList<String> numbers = new LinkedList<>();
        for (Pair<String, Integer> pair : dictionary) {
            String term = pair.getKey();
            if (term.matches("[0-9]{1,13}(\\.[0-9]*)?")) {
                numbers.add(term);
            }
        }
        System.out.println(numbers.size());

    }

    private static int totalPostingEntries(int numberOfFilesPerThread) {
        final int totalFilesInCorpus = 1815;
        int totalPostingEntries = totalFilesInCorpus / numberOfFilesPerThread;
        return totalPostingEntries;
    }


    private static double averageTimePerFile(double[] testsTimes, int numberOfFilesPerThread) {
        int totalFilesInTest = testsTimes.length * numberOfFilesPerThread;
        double totalTestTime = 0;
        for (int i = 0; i < testsTimes.length; i++) {
            totalTestTime = totalTestTime + testsTimes[i];
        }
        double averageTimePerFile = totalTestTime / totalFilesInTest;
        return averageTimePerFile;
    }

    private static void optimizeRankerWeights() {
        MyModel myModel = new MyModel();
        myModel.loadDictionary("C:\\Users\\dor2\\Output");
        System.out.printf("Load Dictionary finished");
        myModel.loadStopWords("C:\\Users\\dor2\\Data\\Data\\data");
        myModel.runQueries("C:\\Users\\dor2\\Data\\Data\\08 Trec_eval\\queries.txt",false);


        double[] maxRecallWeights = new double[6]; //b k WEIGHT_QUERY_BM25 WEIGHT_QUERYDESC_BM25 WEIGHT_HEADER WEIGHT_ENTITIES
        int maxRecallValue = 0;


        double[] b_Range = new double[]{0.6, 0.8};
        double[] k_Range = new double[]{1.6, 1.8};

        double[] WEIGHT_QUERY_BM25_Range = new double[]{1,1.2,1.4};
        double[] WEIGHT_QUERYDESC_BM25_Range = new double[]{ 0.4, 0.6, 0.7};
        double[] WEIGHT_HEADER_Range = new double[]{0.05, 0.1};
        double[] WEIGHT_ENTITIES_Range = new double[]{ 0.1, 0.2};

        for (double b : b_Range) {
            for (double k : k_Range) {
                for (double QUERY_BM25 : WEIGHT_QUERY_BM25_Range) {
                    for (double QUERYDESC_BM25 : WEIGHT_QUERYDESC_BM25_Range) {
                        for (double HEADER : WEIGHT_HEADER_Range) {
                            for (double ENTITIES : WEIGHT_ENTITIES_Range) {
                                System.out.println("--------------------------");
                                //myModel.weightsSetter(new double[]{b,k,QUERY_BM25,QUERYDESC_BM25,HEADER,ENTITIES});

                                ArrayList<Pair<String, ArrayList<String>>> currRetrieval = myModel.runQueries("C:\\Users\\dor2\\Data\\Data\\08 Trec_eval\\queries.txt",false);
                                myModel.saveLatestRetrievalResults("C:\\Users\\dor2\\Data\\Data\\08 Trec_eval\\output\\results.txt");
                                int currRecallValue = getRecall();
                                if(currRecallValue > maxRecallValue){
                                    maxRecallValue = currRecallValue;
                                    maxRecallWeights = new double[]{b,k,QUERY_BM25,QUERYDESC_BM25,HEADER,ENTITIES};
                                }
                                System.out.println("currRecallValue: "+currRecallValue);
                            }
                        }
                    }
                }
            }
        }
        //b k WEIGHT_QUERY_BM25 WEIGHT_QUERYDESC_BM25 WEIGHT_HEADER WEIGHT_ENTITIES
        System.out.println("b: "+maxRecallWeights[0]+"\n"+"k: "+maxRecallWeights[1]+"\n"+"WEIGHT_QUERY_BM25: "+maxRecallWeights[2]+"\n"+"WEIGHT_QUERYDESC_BM25: "+maxRecallWeights[3]+"\n"+"WEIGHT_HEADER: "+maxRecallWeights[4]+"\n"+"WEIGHT_ENTITIES: "+maxRecallWeights[5]);
    }

    private static int getRecall() {
        int recallCount = 0;
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd C:\\Users\\dor2\\Data\\Data\\08 Trec_eval & treceval.exe qrels.txt output\\results.txt > output\\summary.txt");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            /*try {
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            //Process process = Runtime.getRuntime().exec("cd C:\\Users\\dor2\\Data\\Data\\08 Trec_eval & treceval.exe qrels.txt output\\results.txt > output\\summary.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\dor2\\Data\\Data\\08 Trec_eval\\output\\summary.txt"));
            String line = "";
            while ((line = bufferedReader.readLine()) != null){
                if(line.contains("Rel_ret")){
                    line = line.substring(line.indexOf(':')+1);
                    line = line.replaceAll(" ","");
                    recallCount = Integer.parseInt(line);
                    break;
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return recallCount;
    }
}
