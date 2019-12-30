package CorpusProcessing;

import java.io.IOException;
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
            List fileText = Files.readAllLines(Paths.get(filePath), StandardCharsets.ISO_8859_1);
            Iterator<String> iterator = fileText.iterator();
            while (iterator.hasNext()) {
                String currentLine = iterator.next();

                if (currentLine.equals("<DOC>")) {
                    //Document ID extraction
                    documentId = iterator.next();
                    //Removing the tags
                    documentId = documentId.substring(documentId.indexOf(">") + 1, documentId.length() - 1);
                    documentId = documentId.substring(0, documentId.indexOf("<"));
                    documentId = documentId.replaceAll(" ", "");
                    //Document header extraction
                    //String iterText = "";
                    String documentHeader = "";
                    String documentDate = "";


                    do {
                        currentLine = iterator.next();
                    } while (!(currentLine.equals("<HEADER>")) && !(currentLine.equals("<DATE>")) && !(currentLine.equals("<TEXT>")) && !(currentLine.equals("</DOC>")) && !(currentLine.contains("<DATE>")));


                    if (currentLine.equals("</DOC>")) {
                        documents.add(new Document(documentId, documentHeader, "", ""));
                        continue;
                    } else if (currentLine.equals("<DATE>")) { //LA
                        iterator.next(); //<P>
                        documentDate = iterator.next();
                        //HEADLINE EXTRACTION
                        do {
                            currentLine = iterator.next();
                        } while (!currentLine.equals("<HEADLINE>") && !currentLine.equals("</DOC>"));
                        if (currentLine.equals("</DOC>")) {
                            documents.add(new Document(documentId, documentHeader, documentDate, ""));
                            continue;
                        }
                        currentLine = iterator.next();
                        while (!currentLine.equals("</HEADLINE>")) {
                            if (!currentLine.equals("<P>") && !currentLine.equals("</P>")) {
                                documentHeader = documentHeader + currentLine + " \n ";
                            }
                            currentLine = iterator.next();
                        }
                    } else if (currentLine.equals("<HEADER>")) { //FB
                        currentLine = iterator.next();
                        while (!currentLine.equals("</HEADER>")) {
                            documentHeader = documentHeader + currentLine + " \n ";
                            if (currentLine.contains("<DATE1>")) {
                                documentDate = currentLine.substring(9, currentLine.length() - 9);
                            }
                            currentLine = iterator.next();
                        }
                    } else if (currentLine.contains("<DATE>")) { //FT
                        documentDate = currentLine.substring(6);

                        //HEADLINE EXTRACTION
                        do {
                            currentLine = iterator.next();
                        } while (!currentLine.equals("<HEADLINE>") && !currentLine.equals("<TEXT>"));
                        if(!currentLine.equals("<TEXT>")) {
                            currentLine = iterator.next();
                            while (!currentLine.equals("</HEADLINE>")) {
                                documentHeader = documentHeader + currentLine + " \n ";
                                currentLine = iterator.next();
                            }
                        }
                    }

                    //Document text extraction
                    if (!currentLine.equals("<TEXT>")) {
                        do {
                            currentLine = iterator.next();
                        }
                        while (!(currentLine.equals("<TEXT>")) && !(currentLine.equals("</DOC>")));

                    }
                    //&& !(iterator.next().equals("</DOC>")
                    String documentText = "";
                    if (!currentLine.equals("</DOC>")) {
                        currentLine = iterator.next();
                        while (!currentLine.equals("</TEXT>") && !currentLine.equals("</DOC>")) {
                            if (!currentLine.equals("<P>") && !currentLine.equals("</P>")) {
                                documentText = documentText + currentLine + " \n ";
                            }
                            currentLine = iterator.next();
                        }
                    }
                    //Document creation
                    documents.add(new Document(documentId, documentHeader, documentDate, documentText));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            System.out.println("documentId: " + documentId);
        }


        return documents;
    }
}
