package Test;

import CorpusProcessing.Document;
import CorpusProcessing.Parse;
import CorpusProcessing.ReadFile;

import java.util.ArrayList;

public class TestsPart1 {

    public static void main(String[] args) {
        //ReadFile_separateFileToDocuments_Test1();
        //ReadFile_separateFileToDocuments_Test2();
        parse_parseDocument_Test1();


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

    private static void parse_parseDocument_Test1() {
        boolean result = false;
        String filePath = "C:\\Users\\Merav\\Desktop\\corpus\\FB396001\\FB396001";
        ArrayList<Document> documentsList = ReadFile.separateFileToDocuments(filePath);
        String [] tokens = Parse.parseDocument(documentsList.get(0));
        for (int i = 0; i < tokens.length; i++) {

            System.out.println(tokens[i]);

        }
    }
}
