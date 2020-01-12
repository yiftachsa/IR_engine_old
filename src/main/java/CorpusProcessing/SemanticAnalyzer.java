package CorpusProcessing;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;
import sun.plugin.javascript.navig.Link;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SemanticAnalyzer {

    private static final int EXPENDEDWORDSPERTERM = 3;

    private static SemanticAnalyzer semanticAnalyzer;
    private Searcher word2VecSearcher; //FIXME: Check if a new searcher is needed for every search


    private SemanticAnalyzer() {
        try {
            Word2VecModel word2VecModel = Word2VecModel.fromTextFile(new File("w2vJAR/word2vec.c.output.model.txt"));
            this.word2VecSearcher = word2VecModel.forSearch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SemanticAnalyzer getInstance() {
        if (semanticAnalyzer == null) {
            semanticAnalyzer = new SemanticAnalyzer();
        }
        return semanticAnalyzer;
    }

    /**
     * Receives a term and expands it using semantic analysis of the terms within it.
     * for each term in the query finds a list of words with close semantic meaning.
     * @param queryToExpand - String - query to expand
     * @return - ArrayList<String> - expanded query
     */
    public ArrayList<String> expandQuery(String queryToExpand) {
        String[] queryTerms = queryToExpand.split(" ");
        String result = "";

        ArrayList<String> expandedQuery = new ArrayList<>();
        for (int i = 0; i < queryTerms.length; i++) {
            //expandedQuery.add(queryTerms[i]);
            expandedQuery.addAll(expandTerm(queryTerms[i].toLowerCase()));

//            for (String term : expandedQuery) {
//                result = result + term + " ";
//            }
        }
        return expandedQuery;
    }

    /**
     * Receives a term and finds other words with close semantic meaning.
     * @param queryTerm - String - a word to expand
     * @return - ArrayList<String> - words with close semantic meaning to queryTerm
     */
    private ArrayList<String> expandTerm(String queryTerm) {
        ArrayList<String> similarTerms = new ArrayList<>();
        try {
            List<Searcher.Match> matches = this.word2VecSearcher.getMatches(queryTerm, EXPENDEDWORDSPERTERM);
            for (Searcher.Match match:matches){
                similarTerms.add(match.match());
            }
        } catch (Searcher.UnknownWordException e) {
            //THE GIVEN queryTerm is not known to the model
        }
        return similarTerms;
    }


}
