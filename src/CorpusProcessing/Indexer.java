package CorpusProcessing;

import java.util.HashMap;
import java.util.Map;

public class Indexer {


    private static Map<String , String> dictionary;

    private static Map<String , String> uniqueDictionary;

    private static Map<String , String> entitiesDictionary;


    private static int postingCount;


    public Indexer() {
        this.dictionary = new HashMap<String,String>();
        this.uniqueDictionary = new HashMap<String, String>();
        this.entitiesDictionary = new HashMap<String, String>();
        this.postingCount = 0;
    }




    public boolean getDictionaryStatus() {
        if(dictionary == null)
        {
            return  false;
        }
        else
        {
            return  true;
        }
    }


    public static boolean doesDictionaryContains(String key){
        return dictionary.containsKey(key);
    }

    public static void addToDictionary(String key, String value){
        dictionary.put(key, value);
    }



    public static Map<String, String> getDictionary() {
        if (dictionary != null){
            return dictionary;
        }
        return null;
    }

    public static void setDictionary(Map<String, String> dictionary) {
        Indexer.dictionary = dictionary;
    }

    public static Map<String, String> getUniqueDictionary() {
        return uniqueDictionary;
    }

    public static void setUniqueDictionary(Map<String, String> uniqueDictionary) {
        Indexer.uniqueDictionary = uniqueDictionary;
    }

    public static Map<String, String> getEntitiesDictionary() {
        return entitiesDictionary;
    }

    public static void setEntitiesDictionary(Map<String, String> entitiesDictionary) {
        Indexer.entitiesDictionary = entitiesDictionary;
    }

    public static int getPostingCount() {
        return postingCount;
    }

    public static void setPostingCount(int postingCount) {
        Indexer.postingCount = postingCount;
    }
}
