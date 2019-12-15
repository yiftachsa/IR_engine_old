package CorpusProcessing;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *  This class mergers the posting files within a letter directory
 */
public class RunnableMerge implements Runnable{

    private String Path;
    private Map<String, Pair<Integer, String>> dictionary;


    public RunnableMerge(String path , Map<String, Pair<Integer, String>> dictionary) {
        this.Path = path;
        this.dictionary = dictionary;
    }


    //merges all the posting within the path
    @Override
    public void run() {
        SortedMap<String, ArrayList<Pair<String, Integer>>> posting = new TreeMap<>();
        int numberOfPostingFiles = Documenter.getInvertedIndexIndex();
        //todo: load the first posting file to posting!
        for (int i = 0; i < numberOfPostingFiles; i++) {
                //TODO: LOAD POSTING FILE -
                //Merge into posting. - need to check about single appearance!



        }
        Documenter.saveInvertedIndex(posting);
    }
}
