package CorpusProcessing;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ReadFile {

    /**
     * Receives a file path and splits it to the documents in the file.
     * Initializes for each document a new Document object and returns a list of all these objects.
     *
     * @param filePath - String - an absolute path to a file containing documents
     * @return - ArrayList<Document> - a list of all the documents in the file
     */
    public static ArrayList<Document> separateFileToDocuments(String filePath) {
        ArrayList<Document> documents = new ArrayList();
        String documentId = "";
        try {
            List fileText = Files.readAllLines(Paths.get(filePath), StandardCharsets.ISO_8859_1); //TODO: Check
            Iterator<String> iterator = fileText.iterator();
            while (iterator.hasNext()) {
                String currentLine = iterator.next();
                if (currentLine.equals("<DOC>")) {
                    //Document ID extraction
                    documentId = iterator.next();
                    //Removing the tags
                    documentId = documentId.substring(8, documentId.length() - 1);
                    documentId = documentId.substring(0, documentId.length() - 8);
                    //Document header extraction
                    String iterText = "";
                    String documentHeader = "";
                    do {
                        iterText = iterator.next();
                    } while (!(iterText.equals("<HEADER>")) && !(iterText.equals("<TEXT>")) && !(iterText.equals("</DOC>")));

                    if (iterText.equals("</DOC>")) {
                        documents.add(new Document(documentId, documentHeader, ""));
                        continue;
                    }

                    if (iterText.equals("<HEADER>")) {
                        currentLine = iterator.next();
                        while (!currentLine.equals("</HEADER>")) {
                            documentHeader = documentHeader + currentLine + "\n";
                            currentLine = iterator.next();
                        }
                    }
                    //Document text extraction
                    if (!iterText.equals("<TEXT>")) {
                        while (!(iterator.next().equals("<TEXT>"))) {
                        }
                    }

                    String documentText = "";
                    currentLine = iterator.next();
                    while (!currentLine.equals("</TEXT>")) {
//                        if (currentLine.contains("<P>")) {
//                            currentLine = iterator.next();
//                            continue;
//                        }
                        documentText = documentText + currentLine + "\n";
                        currentLine = iterator.next();
                    }
                    //Document creation
                    documents.add(new Document(documentId, documentHeader, documentText));
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); //FIXME: !!
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            System.out.println("documentId: " + documentId);
        }


        return documents;
    }
}
