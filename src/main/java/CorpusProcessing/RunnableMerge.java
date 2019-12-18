package CorpusProcessing;

import javafx.util.Pair;

import java.util.*;

/**
 * This class mergers the posting files within a letter directory
 */
public class RunnableMerge implements Runnable {

    private String path;
    private Map<String, Pair<Integer, String>> dictionary;


    public RunnableMerge(String path, Map<String, Pair<Integer, String>> dictionary) {
        this.path = path;
        this.dictionary = dictionary;
    }

    @Override
    public void run() {

        ArrayList<String> listWithoutEntity = Documenter.loadPostingFile2(this.path);
        System.out.println("Finished reading");
        Collections.sort(listWithoutEntity , String.CASE_INSENSITIVE_ORDER);
        System.out.println("Finished Sorting");
        /*
        for (int i = 0; i < stringArrayList.size(); i++) {
            String s = stringArrayList.get(i);
            if(this.dictionary.containsKey(s.substring(0,s.indexOf('!'))));
            {
                listWithoutEntity.add(stringArrayList.get(i));
            }
        }
        */
        System.out.println("Finished put all records without entities");
        for (int i = listWithoutEntity.size()-1; i > 0 ; i--) {
            String firstRecord = listWithoutEntity.get(i);
            String seconedRecord =listWithoutEntity.get(i-1);
            String  firstTerm= firstRecord.substring(0,firstRecord.indexOf("!"));

            if(!(this.dictionary.containsKey(firstTerm)))
            {
                listWithoutEntity.remove(i); //todo: find better!!
                continue;
            }

            String seconedTerm = seconedRecord.substring(0,seconedRecord.indexOf("!"));
            String firstPairs = firstRecord.substring(firstRecord.indexOf("!")+1);
            String seconedPairs = seconedRecord.substring(seconedRecord.indexOf("!")+1);
            if(firstTerm.toLowerCase().equals(seconedTerm.toLowerCase()))
            {
                //case at least one is not in upper case
                if(!(Character.isUpperCase(seconedTerm.charAt(0))&&Character.isUpperCase(firstTerm.charAt(0))))
                {
                    seconedTerm = seconedTerm.toLowerCase() +"!"+ seconedPairs + firstPairs;
                }
                else //both in upper case
                {
                    seconedTerm = seconedTerm +"!"+seconedPairs + "|" + firstPairs;

                }
                listWithoutEntity.remove(i);
                listWithoutEntity.set(i-1,seconedTerm);
            }
        }
        Documenter.deleteAllFilesFromDirectory(this.path);
        Documenter.savePostingFile4(listWithoutEntity, this.path+"\\posting");

    }
}
