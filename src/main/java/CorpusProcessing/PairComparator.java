package CorpusProcessing;

import javafx.util.Pair;

import java.util.Comparator;

/**
 * Comparator for Pair<String,Integer>
 */
public class PairComparator implements Comparator<Pair<String,Integer>> {

    @Override
    public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
        return o1.getKey().compareTo(o2.getKey());
    }
}
