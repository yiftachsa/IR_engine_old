package CorpusProcessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Responsible to write to Disk.
 * Receive the information about specific document(Doc ID , Max tf , number of uniq words)
 */
public class Documenter {

    private static final int NUMBEROFDOCUMENTSPERFILE = 1;
    private static int fileIndex = 0;
    private static ArrayList<String> documentsDetails = new ArrayList<>();
    private static String filesPath;


    public static void setPath(String path) {
        filesPath = path+"";
    }

    public static void saveDocumentDetails(String docId, int maxTermFrequency, int uniqTermsCount) {
        if(filesPath!=null) {
            documentsDetails.add(docId + "," + maxTermFrequency + "," + uniqTermsCount);
            if (documentsDetails.size() >= NUMBEROFDOCUMENTSPERFILE) {
                //WRITE TO DISK! 
                BufferedWriter writer = null;
                try {
                    if(fileIndex == 0)
                    {
                        new File(filesPath + "\\DocumentsDetails").mkdir();
                    }
                    writer = new BufferedWriter(new FileWriter(filesPath +"\\DocumentsDetails\\" + fileIndex));
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


}
