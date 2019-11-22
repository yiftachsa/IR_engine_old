package Test;

import CorpusProcessing.Document;
import CorpusProcessing.Parse;
import CorpusProcessing.ReadFile;

import java.util.ArrayList;

public class TestsPart1 {

    public static void main(String[] args) {
        //ReadFile_separateFileToDocuments_Test1();
        //ReadFile_separateFileToDocuments_Test2();
        parse_parseDocument_Test1_Dollars();

    }



    private static boolean ReadFile_separateFileToDocuments_Test1() {
        boolean result = false;
        String filePath = "C:\\Users\\Merav\\Desktop\\corpus\\FB396001\\FB396001";
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
       ArrayList<String> testDollars= Parse.parseDocument(new Document("0","","$100 billion $100 million $450,000,000 $100 20.6m Dollars 391bn Dollars"));
        ArrayList<String> testPercentage= Parse.parseDocument(new Document("0","","6% 6 percent 7 percentage"));
        ArrayList<String> testDate = Parse.parseDocument(new Document("0","","14 MAY 14 may 30 Jan"));

        for (int i = 0; i < testDollars.size(); i++) {
            System.out.println(testDollars.get(i));
        }

        for (int i = 0; i < testPercentage.size(); i++) {
            System.out.println(testPercentage.get(i));
        }
        for (int i = 0; i < testDate.size(); i++) {
            System.out.println(testDate.get(i));
        }
    }
}
