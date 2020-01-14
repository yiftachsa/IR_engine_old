package Retrieval;

import CorpusProcessing.TermDocumentTrio;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public interface IRanker {

    /**
     *
     * @param processedQuery
     * @param processedQueryDescription
     * @param processedExpandedQuery
     * @param semanticExpandedTerms
     * @param documentID
     * @param documentLength
     * @param documentTerms
     * @param documentHeader
     * @param documentEntities
     * @return
     */
    double rankDocument(ArrayList<TermDocumentTrio> processedQuery, ArrayList<TermDocumentTrio> processedQueryDescription, ArrayList<TermDocumentTrio> processedExpandedQuery, HashMap<String, Integer> semanticExpandedTerms, String documentID, int documentLength, HashMap<String, Integer> documentTerms, ArrayList<TermDocumentTrio> documentHeader, ArrayList<String> documentEntities);

    /**
     * Returns an sorted array of the entities, based on importance. //TODO:Expand!
     * @param documentEntities
     * @param processedDocumentHeader
     * @return
     */
    ArrayList<Pair<String, Double>> rankEntities(HashMap<String, Integer> documentEntities, ArrayList<TermDocumentTrio> processedDocumentHeader);

}
