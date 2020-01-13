package Retrieval;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Used for semantic analysis on phrases.
 * Singleton.
 */
public class SemanticAnalyzer {

    private static final int EXPENDEDWORDSPERTERM = 3;

    private static SemanticAnalyzer semanticAnalyzer;
    private Searcher word2VecSearcher;

    /**
     * Private constructor.
     */
    private SemanticAnalyzer() {
        try {
            Word2VecModel word2VecModel = Word2VecModel.fromTextFile(new File("src\\main\\resources\\w2vJAR\\word2vec.c.output.model.txt"));
            this.word2VecSearcher = word2VecModel.forSearch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a SemanticAnalyzer
     *
     * @return - SemanticAnalyzer - this.semanticAnalyzer
     */
    public static SemanticAnalyzer getInstance() {
        if (semanticAnalyzer == null) {
            semanticAnalyzer = new SemanticAnalyzer();
        }
        return semanticAnalyzer;
    }

    /**
     * Receives a term and expands it using semantic analysis of the terms within it.
     * for each term in the query finds a list of words with close semantic meaning.
     *
     * @param queryToExpand - String - query to expand
     * @return - String - expanded query
     */
    public String expandQuery(String queryToExpand) {
        String[] queryTerms = queryToExpand.split(" ");

        String result = "";

        HashSet<String> expandedQuery = new HashSet<>();

        for (int i = 0; i < queryTerms.length; i++) {
            expandedQuery.addAll(expandTerm(queryTerms[i].toLowerCase()));
        }

        for (int i = 0; i < queryTerms.length; i++) {
            if (expandedQuery.contains(queryTerms[i])) {
                expandedQuery.remove(queryTerms[i]);
            }
        }

        for (String term : expandedQuery) {
            result = result + term + " ";
        }
        return result;
    }

    /**
     * Receives a term and finds other words with close semantic meaning.
     *
     * @param queryTerm - String - a word to expand
     * @return - ArrayList<String> - words with close semantic meaning to queryTerm
     */
    private ArrayList<String> expandTerm(String queryTerm) {
        ArrayList<String> similarTerms = new ArrayList<>();
        try {
            List<Searcher.Match> matches = this.word2VecSearcher.getMatches(queryTerm, EXPENDEDWORDSPERTERM);
            for (Searcher.Match match : matches) {
                similarTerms.add(match.match());
            }
        } catch (Searcher.UnknownWordException e) {
            //THE GIVEN queryTerm is not known to the model
        }
        return similarTerms;
    }


}
