package  main.java.Test;

import  main.java.CorpusProcessing.Document;
import  main.java.CorpusProcessing.Parse;
import  main.java.CorpusProcessing.ReadFile;

import java.util.ArrayList;

public class TestsPart1 {

    public static void main(String[] args) {
        //ReadFile_separateFileToDocuments_Test1();
        //ReadFile_separateFileToDocuments_Test2();
        parse_parseDocument_Test1_Dollars();
        //Parse_parseDocument_Test2_parseDocument();

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


    private static void parse_parseDocument_Test1_Dollars() {
/*
        System.out.println("\ntestDollars");
        ArrayList<String> testDollars= Parse.parseDocument(new Document("0","","$100 billion $100 million $450,000,000 $100 20.6m Dollars 391bn Dollars"), false);
        for (int i = 0; i < testDollars.size(); i++) {
            System.out.println(testDollars.get(i));
        }
        System.out.println("\ntestPercentage");
        ArrayList<String> testPercentage= Parse.parseDocument(new Document("0","","6% 6 percent 7 percentage"), false);
        for (int i = 0; i < testPercentage.size(); i++) {
            System.out.println(testPercentage.get(i));
        }
        System.out.println("\ntestDate");
        ArrayList<String> testDate = Parse.parseDocument(new Document("0","","14 MAY 14 may 30 Jan"), false);
        for (int i = 0; i < testDate.size(); i++) {
            System.out.println(testDate.get(i));
        }
        System.out.println("\ntestThousand");
        ArrayList<String> testThousand = Parse.parseDocument(new Document("0","","123 Thousand 125 thousand"), false);
        for (int i = 0; i < testThousand.size(); i++) {
            System.out.println(testThousand.get(i));
        }
        System.out.println("\ntestMillionBillionNumber");
        ArrayList<String> testMillionBillionNumber = Parse.parseDocument(new Document("0","","55 Million 60 million 70 Billion 80 billion"), false);
        for (int i = 0; i < testMillionBillionNumber.size(); i++) {
            System.out.println(testMillionBillionNumber.get(i));
        }
        System.out.println("\ntestPriceDollars");
        ArrayList<String> testPriceDollars = Parse.parseDocument(new Document("0","","1.98 Dollars 1,000,000 Dollars"), false);
        for (int i = 0; i < testPriceDollars.size(); i++) {
            System.out.println(testPriceDollars.get(i));
        }
        System.out.println("\ntestMillionBillionUSDollars");
        ArrayList<String> testMillionBillionUSDollars = Parse.parseDocument(new Document("0","","100 billion U.S. dollars 100 million U.S. dollars 100 trillion U.S. dollars"), false);
        for (int i = 0; i < testMillionBillionUSDollars.size(); i++) {
            System.out.println(testMillionBillionUSDollars.get(i));
        }
        System.out.println("\ntestFractions");
        ArrayList<String> testFractions = Parse.parseDocument(new Document("0","","100 3/4 22 1/2 Dollars 112 1/3 dollars 1/2 1/4 Dollars"), false);
        for (int i = 0; i < testFractions.size(); i++) {
            System.out.println(testFractions.get(i));
        }
        System.out.println("\ntestSimpleNumbers");
        ArrayList<String> testSimpleNumbers = Parse.parseDocument(new Document("0","","98 10,123 1010.56 10,123,000 10,123,000,000"), false);
        for (int i = 0; i < testSimpleNumbers.size(); i++) {
            System.out.println(testSimpleNumbers.get(i));
        }*/
        System.out.println("\ntestMonthDate");
        ArrayList<String> testMonthDate = Parse.parseDocument(new Document("0","","June 4 4 Aug"), false);
        for (int i = 0; i < testMonthDate.size(); i++) {
            System.out.println(testMonthDate.get(i));
        }/*
        System.out.println("\nbetweenTest");
        ArrayList<String> betweenTest = Parse.parseDocument(new Document("0","","22-23  Between 10 and 50 between 70 and 50 between 60 and merav between 40 to 30 between to ty between"), false);
        for (int i = 0; i < betweenTest.size(); i++) {
            System.out.println(betweenTest.get(i));
        }

        System.out.println("\nsentenceTest");
        ArrayList<String> sentenceTest = Parse.parseDocument(new Document("0","","I am (Merav). I am not amused/invested/interested in this code. I would like to pay (10,000) dollars for someone to replace me!"), false);
        for (int i = 0; i < sentenceTest.size(); i++) {
            System.out.println(sentenceTest.get(i));
        }

 */
    }

    private static boolean Parse_parseDocument_Test2_parseDocument() {
        boolean result = false;
        String filePath = "C:\\scripts\\Courses_Scripts\\Information_Retrieval\\IR_Engine\\Data\\corpus\\corpus\\FB396001\\FB396001";
        ArrayList<Document> documentsList = ReadFile.separateFileToDocuments(filePath);
        ArrayList<String> bagOfWords = Parse.parseDocument(documentsList.get(0),false);
        for (String term: bagOfWords) {
            System.out.println(term);
        }
        return result;
    }



}
