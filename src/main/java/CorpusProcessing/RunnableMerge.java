package CorpusProcessing;

import javafx.util.Pair;

import java.util.*;

/**
 * This class mergers the posting files within a letter directory
 */
public class RunnableMerge implements Runnable {

    private String path;
    private Map<String, DictionaryEntryTrio> dictionary;

    /**
     * Constructor
     * @param path - String
     * @param dictionary - Map<String, DictionaryEntryTrio>
     */
    public RunnableMerge(String path, Map<String,DictionaryEntryTrio> dictionary) {
        this.path = path;
        this.dictionary = dictionary;
    }


    //merges all the posting within the path
    @Override
    public void run() {

        ArrayList<String> stringArrayList = Documenter.loadPostingFile(this.path);
        ArrayList<String> listWithoutEntity =  new ArrayList<>();


        Collections.sort(stringArrayList , String.CASE_INSENSITIVE_ORDER);


        for (int i = 0; i < stringArrayList.size(); i++) {
            String s = stringArrayList.get(i);
            if(this.dictionary.containsKey(s.substring(0,s.indexOf('!'))))
            {
                listWithoutEntity.add(stringArrayList.get(i));
            }
        }
        for (int i = listWithoutEntity.size()-1; i > 0 ; i--) {
            String firstRecord = listWithoutEntity.get(i);
            String seconedRecord =listWithoutEntity.get(i-1);
            String firstTerm = firstRecord.substring(0,firstRecord.indexOf("!"));
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
        Documenter.saveFinalPostingFile(listWithoutEntity, this.path+"\\posting");
    }
}
