package CorpusProcessing;

import javafx.util.Pair;

import java.util.*;

public class Searcher {

    private IRanker ranker;
    private static final int RESULTNUMBER = 50;

    public Searcher() {
        this.ranker = new Ranker();
    }

    public ArrayList<String> runQuery(String query, Indexer indexer, Parse parse) {
        ArrayList<TermDocumentTrio> processedQuery = parseQuery("query", query, parse);
        /**
         *  HashMap(DocID ,Pair(Document length , HasMap( Term , Document frequency)))
         */
        HashMap<String, Pair<Integer, HashMap<String, Integer>>> relevantDocumentsDetails = getRelevantDocumentsDetails(processedQuery, indexer);

        //HashMap(DocID ,rankResult)
        PriorityQueue<Pair<Double, String>> relevantDocsAndRankResult = new PriorityQueue<>();
        for (Map.Entry<String, Pair<Integer, HashMap<String, Integer>>> docDetailsPair : relevantDocumentsDetails.entrySet()) {
            String documentID = docDetailsPair.getKey();
            int documentLength = docDetailsPair.getValue().getKey();
            HashMap<String, Integer> documentTerms = docDetailsPair.getValue().getValue();
            double rankResult = ranker.rankDocument(processedQuery, documentID, documentLength, documentTerms);
            relevantDocsAndRankResult.add(new Pair<>(rankResult, documentID));
        }
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < relevantDocsAndRankResult.size() && i < RESULTNUMBER; i++) {
            result.add(relevantDocsAndRankResult.remove().getValue());
        }
        return result;
    }

    private ArrayList<TermDocumentTrio> parseQuery(String DocNO, String query, Parse parse) {

        ArrayList<String> bagOfWords = parse.parseQuery(query);
        ArrayList<TermDocumentTrio> processedQuery = Mapper.processBagOfWords(DocNO, "", bagOfWords, "");
        return processedQuery;
    }

    private HashMap<String, Pair<Integer, HashMap<String, Integer>>> getRelevantDocumentsDetails(ArrayList<TermDocumentTrio> processedQuery, Indexer indexer) {
        HashMap<String, Pair<Integer, HashMap<String, Integer>>> relevantDocumentsDetails = new HashMap<>();
        for (TermDocumentTrio termTrio : processedQuery) {
            String term = termTrio.getTerm();
            ArrayList<Pair<String, Integer>> allPairs = indexer.getTermPosting(term);
            if (allPairs != null) {
                for (int i = 0; i < allPairs.size(); i++) {
                    String documentID = allPairs.get(i).getKey();
                    int df = allPairs.get(i).getValue();
                    HashMap<String, Integer> docTerms = new HashMap<>();
                    if (!relevantDocumentsDetails.containsKey(documentID)) {
                        docTerms.put(term, df);
                        Pair<Integer, HashMap<String, Integer>> pair = new Pair<>(indexer.getDocumentLength(documentID), docTerms);
                        relevantDocumentsDetails.put(documentID, pair);
                    } else {
                        docTerms.putAll(relevantDocumentsDetails.get(documentID).getValue());
                        docTerms.put(term, df);
                        Pair<Integer, HashMap<String, Integer>> pair = new Pair<>(indexer.getDocumentLength(documentID), docTerms);
                        relevantDocumentsDetails.put(documentID, pair);
                    }
                }
            }
        }
        return relevantDocumentsDetails;
    }

    /**
     * Returns an sorted array of the entities, based on importance.
     * @return - String[] - an sorted array of the entities, based on importance.
     * @param documentEntities
     * @param documentHeader
     */
    public String[] rankEntities(HashMap<String, Integer> documentEntities, String documentHeader , Parse parser) {


        return ranker.rankEntities(documentEntities, parseQuery("document",documentHeader,parser));
    }


}
