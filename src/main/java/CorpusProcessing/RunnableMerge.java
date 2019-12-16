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


    //merges all the posting within the path
    @Override
    public void run() {
        Map<String, PriorityQueue<Pair<String, Integer>>> mainPosting = Documenter.loadPostingFile(this.path +"\\postingFile"+ '0');
        int numberOfPostingFiles = Documenter.getInvertedIndexIndex();

        //todo: load the first posting file to posting!

        for (int i = 1; i < numberOfPostingFiles; i++) {
            //Merge into posting. - need to check about single appearance!
            Map<String, PriorityQueue<Pair<String, Integer>>> currentPosting = Documenter.loadPostingFile(this.path +"\\postingFile"+ i);
            for(Map.Entry<String, PriorityQueue<Pair<String, Integer>>> postingLine : currentPosting.entrySet()){
                String term = postingLine.getKey();
                //removing all single appearance terms
                if(!this.dictionary.containsKey(term)){
                    continue;
                }

                PriorityQueue<Pair<String, Integer>> pairs = postingLine.getValue();
                if(mainPosting.containsKey(term))
                {
                    PriorityQueue<Pair<String, Integer>> newQueuePairs = mainPosting.get(term);
                    newQueuePairs.addAll(pairs);
                    mainPosting.put(term ,newQueuePairs);
                }
                else
                {
                    mainPosting.put(term ,pairs);
                }
            }
        }


        //Delete all the partial posting files
        Documenter.deleteAllFilesFromDirectory(this.path);
        //Save the main posting file
        Documenter.savePostingFile(mainPosting, this.path+"\\posting");
    }
}
