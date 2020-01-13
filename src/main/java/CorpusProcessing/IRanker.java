package CorpusProcessing;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public interface IRanker {

    double rankDocument(ArrayList<TermDocumentTrio> processedQuery, ArrayList<TermDocumentTrio> processedQueryDescription, ArrayList<TermDocumentTrio> processedExpandedQuery, HashMap<String, Integer> semanticExpandedTerms, String documentID, int documentLength, HashMap<String, Integer> documentTerms, ArrayList<TermDocumentTrio> documentHeader, ArrayList<String> documentEntities);

    ArrayList<Pair<String, Double>> rankEntities(HashMap<String, Integer> documentEntities, ArrayList<TermDocumentTrio> processedDocumentHeader);

}
