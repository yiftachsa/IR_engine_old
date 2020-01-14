package Retrieval;

import CorpusProcessing.TermDocumentTrio;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public interface IRanker {

    /**
     * Ranks a document compared to a query based on the given parts of both.
     *
     * @param processedQuery            - ArrayList<TermDocumentTrio> - The query after parsing
     * @param processedQueryDescription - ArrayList<TermDocumentTrio> - The query description after parsing.
     * @param processedExpandedQuery    - ArrayList<TermDocumentTrio> - The expanded query after parsing.
     * @param semanticExpandedTerms     - HashMap<String, Integer> - All the terms and their frequencies from the expanded query.
     * @param documentLength            - int - The length of the document.
     * @param documentTerms             - HashMap<String, Integer> - All the terms and their frequencies from the document.
     * @param documentHeader            - ArrayList<TermDocumentTrio> - The document header after parsing.
     * @param documentEntities          - ArrayList<String> - The document entities.
     * @return double - The rank of the similarity between the query and the document.
     */
    double rankDocument(ArrayList<TermDocumentTrio> processedQuery, ArrayList<TermDocumentTrio> processedQueryDescription, ArrayList<TermDocumentTrio> processedExpandedQuery, HashMap<String, Integer> semanticExpandedTerms, int documentLength, HashMap<String, Integer> documentTerms, ArrayList<TermDocumentTrio> documentHeader, ArrayList<String> documentEntities);

    /**
     * Returns an sorted array of the entities, based on importance.
     *
     * @param documentEntities        - HashMap<String, Integer> - A Map of all the entities in a document.
     * @param processedDocumentHeader - ArrayList<TermDocumentTrio> - The document header after parsing.
     * @return - ArrayList<Pair<String, Double>> - an sorted list of the entities, based on importance.
     */
    ArrayList<Pair<String, Double>> rankEntities(HashMap<String, Integer> documentEntities, ArrayList<TermDocumentTrio> processedDocumentHeader);

}
