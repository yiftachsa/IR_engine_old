package Test;

import CorpusProcessing.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;

public class TestsPart1 {

    public static void main(String[] args) {
        //ReadFile_separateFileToDocuments_Test1();
        //ReadFile_separateFileToDocuments_Test2();
        //parseTest();
        //Parse_parseDocument_Test1_parseDocument();
        //Mapper_Test1();
        //Mapper_Test2_mergeAndSortTwoPostingEntriesLists();
        Model_MergerThreads_test1();
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

    private static void parseTest() {
        parse_parseQuery_Test1_generateTokenDollar();
        parse_parseQuery_Test2_generateTokenMonth();
        parse_parseQuery_Test3_Percentage();
        parse_parseQuery_Test4_Thousand();
        parse_parseQuery_Test5_generateTokenLargeNumbers();
        parse_parseQuery_Test6_generateTokenPrice();
        parse_parseQuery_Test7_Fractions();
        parse_parseQuery_Test8_generateTokenSimpleNumber();
        parse_parseQuery_Test9_Between();
        parse_parseQuery_Test10_FirstCustomAdd();
        parse_parseQuery_Test11_generateTokensEntity();
        parse_parseQuery_Test12_stemmer();
    }

    /**
     * Tests generateTokenDollar
     */
    private static void parse_parseQuery_Test1_generateTokenDollar() {
        System.out.println("\ntestDollars");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("20.6m Dollars $100 billion $100 million $450,000,000 $100 391bn Dollars", false);
        passed = passed && testResults.get(0).equals("20.6 M Dollars"); //TODO: Replace with Junit and assert
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
    private static void parse_parseQuery_Test2_generateTokenMonth() {
        System.out.println("\ntestMonthDate");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("June 4 4 Aug Aug 24 November 2", false);
        passed = passed && testResults.get(0).equals("06-04");
        passed = passed && testResults.get(1).equals("08-04");
        passed = passed && testResults.get(2).equals("08-24");
        passed = passed && testResults.get(3).equals("11-02");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests Percentage
     */
    private static void parse_parseQuery_Test3_Percentage() {
        System.out.println("\ntestPercentage");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("6% 6 percent 7 percentage", false);
        passed = passed && testResults.get(0).equals("6%");
        passed = passed && testResults.get(1).equals("6%");
        passed = passed && testResults.get(2).equals("7%");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests Thousand
     */
    private static void parse_parseQuery_Test4_Thousand() {
        System.out.println("\ntestThousand");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("123 Thousand 125 thousand", false);
        passed = passed && testResults.get(0).equals("123K");
        passed = passed && testResults.get(1).equals("125K");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests generateTokenLargeNumbers
     */
    private static void parse_parseQuery_Test5_generateTokenLargeNumbers() {
        System.out.println("\ntestgenerateTokenLargeNumbers");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("55 Million 60 million 70 Billion 80 billion 100 billion U.S. dollars 100 million U.S. dollars 100 trillion U.S. dollars", false);
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
    private static void parse_parseQuery_Test6_generateTokenPrice() {
        System.out.println("\ntestgenerateTokenPrice");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("1.98 Dollars 1,000,000 Dollars 20 Dollars", false);
        passed = passed && testResults.get(0).equals("1.98 Dollars");
        passed = passed && testResults.get(1).equals("1 M Dollars");
        passed = passed && testResults.get(2).equals("20 Dollars");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests Fractions
     */
    private static void parse_parseQuery_Test7_Fractions() {
        System.out.println("\ntestFractions");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("100 3/4 22 1/2 Dollars 1/2 1/4 Dollars", false);
        passed = passed && testResults.get(0).equals("100 3/4");
        passed = passed && testResults.get(1).equals("22 1/2 Dollars");
        passed = passed && testResults.get(2).equals("1/2");
        passed = passed && testResults.get(3).equals("1/4 Dollars");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests generateTokenSimpleNumber
     */
    private static void parse_parseQuery_Test8_generateTokenSimpleNumber() {
        System.out.println("\ntestgenerateTokenSimpleNumber");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("123 1010.56 10,123,000 10,123,000,000", false);
        passed = passed && testResults.get(0).equals("123");
        passed = passed && testResults.get(1).equals("1.01K");
        passed = passed && testResults.get(2).equals("10.123M");
        passed = passed && testResults.get(3).equals("10.123B");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests Between
     */
    private static void parse_parseQuery_Test9_Between() {
        System.out.println("\ntestBetween");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("22-23 Between 10 and 50 between 60 and merav between merav to 60 between to", false);
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
    private static void parse_parseQuery_Test10_FirstCustomAdd() {
        System.out.println("\ntestFirstCustomAdd");
        boolean passed = true;
        ArrayList<String> testResults = Parse.parseQuery("amused/invested/interested", false);
        passed = passed && testResults.get(0).equals("amused");
        passed = passed && testResults.get(1).equals("invested");
        passed = passed && testResults.get(2).equals("interested");
        System.out.println("passed: " + passed);
    }

    /**
     * Tests generateTokensEntity
     */
    private static void parse_parseQuery_Test11_generateTokensEntity() {
        System.out.println("\ntestGenerateTokensEntity");
        boolean passed = true;

        ArrayList<String> testResults = Parse.parseQuery("Merav bamba Merav Shaked bamba Merav Shaked Yiftach bamba Merav Shaked Yiftach Savransky", false);
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
        //TODO: add tests for AllCAps

        ArrayList<String> testResultsCaptial = Parse.parseQuery("MERAV bamba MERAV SHAKED bamba MERAV SHAKED YIFTACH  bamba MERAV SHAKED YIFTACH SAVRANSKY", false);
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
    }

    private static void parse_parseQuery_Test12_stemmer() {
        System.out.println("\ntestStemmer");
        boolean passed = true;

        ArrayList<String> testResults = Parse.parseQuery("sky banana studies students devastation", true);
        passed = passed && testResults.get(0).equals("sky");
        passed = passed && testResults.get(1).equals("banana");
        passed = passed && testResults.get(2).equals("studi");
        passed = passed && testResults.get(3).equals("student");
        passed = passed && testResults.get(4).equals("devast");
        System.out.println("passed: " + passed);

    }

    /*
    System.out.println("\nsentenceTest");
    ArrayList<String> sentenceTest = Parse.parseText(new Document("0","","I am (Merav). I am not amused/invested/interested in this code. I would like to pay (10,000) dollars for someone to replace me!"), false);
    for (int i = 0; i < sentenceTest.size(); i++) {
        System.out.println(sentenceTest.get(i));
    }
*/

    private static boolean Parse_parseDocument_Test1_parseDocument() {
        boolean result = false;
        String filePath = "C:\\scripts\\Courses_Scripts\\Information_Retrieval\\IR_Engine\\Data\\corpus\\corpus\\FB396001\\FB396001";
        ArrayList<Document> documentsList = ReadFile.separateFileToDocuments(filePath);
        ArrayList<String> bagOfWords = Parse.parseDocument(documentsList.get(0), false);
        for (String term : bagOfWords) {
            System.out.println(term);
        }
        return result;
    }

    private static boolean Mapper_Test1() {
        boolean result = false;
        ArrayList<String> bagOfWords = new ArrayList<String>(Arrays.asList("B", "c", "B", "a", "b", "c", "a", "A"));
        Documenter.setPath("D:\\Documents\\Studies\\Documents for higher education\\Courses\\Year 3 Semester 1\\אחזור מידע\\TestIREngine");
        ArrayList<Trio> postingsEntries = Mapper.proceedBagOfWords("Doc1", bagOfWords);
        for (Trio trio : postingsEntries) {
            System.out.println(trio);
        }

        return result;
    }

    private static boolean Mapper_Test2_mergeAndSortTwoPostingEntriesLists() {
        boolean result = false;
        ArrayList<String> bagOfWords1 = new ArrayList<String>(Arrays.asList("B", "c", "B", "a", "b", "c", "a", "A"));
        ArrayList<String> bagOfWords2 = new ArrayList<String>(Arrays.asList("B", "b", "d", "q"));
        Documenter.setPath("D:\\Documents\\Studies\\Documents for higher education\\Courses\\Year 3 Semester 1\\אחזור מידע\\TestIREngine");
        ArrayList<Trio> postingsEntries1 = Mapper.proceedBagOfWords("Doc1", bagOfWords1);
        ArrayList<Trio> postingsEntries2 = Mapper.proceedBagOfWords("Doc2", bagOfWords2);
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
        Documenter.setPath("D:\\Documents\\Studies\\Documents for higher education\\Courses\\Year 3 Semester 1\\אחזור מידע\\TestIREngine");
        ArrayList<Trio> postingsEntries1 = Mapper.proceedBagOfWords("Doc1", bagOfWords1);
        ArrayList<Trio> postingsEntries2 = Mapper.proceedBagOfWords("Doc2", bagOfWords2);
        ArrayList<Trio> postingsEntries3 = Mapper.proceedBagOfWords("Doc3", bagOfWords3);

        allPostingEntriesLists.add(postingsEntries1);
        allPostingEntriesLists.add(postingsEntries2);
        allPostingEntriesLists.add(postingsEntries3);

        if (allPostingEntriesLists.size() >= 2) {
            Future<ArrayList<Trio>> future = mergersPool.submit(new CallableMerge(allPostingEntriesLists));
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

        for (Trio trio : allPostingEntriesLists.get(0)) {
            System.out.println(trio);
        }
        System.out.println(allPostingEntriesLists.size());

        return false;
    }

}
