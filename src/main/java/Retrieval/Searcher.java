package Retrieval;

import CorpusProcessing.*;
import javafx.util.Pair;

import java.util.*;

public class Searcher {

    private IRanker ranker;
    private static final int RESULTNUMBER = 50;

    public Searcher(Indexer indexer, int corpusSize, double avdl) {
        this.ranker = new Ranker(corpusSize, avdl, indexer);
    }

    public ArrayList<String> runQuery(String query, String queryDescription, String semanticExpandedTerms, Indexer indexer, Parse parser) {

        //Process the query sections
        ArrayList<TermDocumentTrio> processedQuery = parseQuery("query", query, parser);
        ArrayList<TermDocumentTrio> processedQueryDescription = null;
        if (!queryDescription.isEmpty()) {
            processedQueryDescription = parseQuery("query", queryDescription, parser);
        }
        ArrayList<TermDocumentTrio> processedExpandedQuery = null;
        if (!semanticExpandedTerms.isEmpty()) { //use semantics
            processedExpandedQuery = parseQuery("query", semanticExpandedTerms, parser);
        }

        //Aggregating all the terms associated with the query
        HashSet<String> queryAssociatedTerms = mergeLists(processedQuery, processedQueryDescription);

        /**
         *  HashMap(DocID ,Pair(Document length , HasMap( Term , Document frequency)))
         */
        HashMap<String, Pair<Integer, HashMap<String, Integer>>> relevantDocumentsDetails = getRelevantDocumentsDetails(queryAssociatedTerms, indexer);

        HashMap<String, Pair<Integer, HashMap<String, Integer>>> expandedQueryDocumentsDetails = getRelevantDocumentsForExpandedQuery(semanticExpandedTerms,processedExpandedQuery ,indexer, parser, relevantDocumentsDetails);


        //HashMap(rankResult , DocID)
        PriorityQueue<Pair<Double, String>> relevantDocsAndRankResult = new PriorityQueue<>((o1, o2) -> (int) (o2.getKey() - o1.getKey()));

        //Cycling through every document and ranking it compared to the query
        for (Map.Entry<String, Pair<Integer, HashMap<String, Integer>>> docDetailsPair : relevantDocumentsDetails.entrySet()) {
            String documentID = docDetailsPair.getKey();
            int documentLength = docDetailsPair.getValue().getKey();
            HashMap<String, Integer> documentTerms = docDetailsPair.getValue().getValue();

            String documentHeader = indexer.getDocumentHeader(documentID);
            ArrayList<TermDocumentTrio> processedDocumentHeader = parseQuery("header", documentHeader, parser);

            ArrayList<String> documentEntities = indexer.getDocumentEntitiesList(documentID);

            HashMap<String, Integer> expandedQueryDocumentTerms = null;
            if (expandedQueryDocumentsDetails != null) {
                if (expandedQueryDocumentsDetails.containsKey(documentID)) {
                    expandedQueryDocumentTerms = expandedQueryDocumentsDetails.get(documentID).getValue();
                }
            }

            double rankResult = ranker.rankDocument(processedQuery, processedQueryDescription,processedExpandedQuery ,expandedQueryDocumentTerms, documentID, documentLength, documentTerms, processedDocumentHeader, documentEntities);
            relevantDocsAndRankResult.add(new Pair<>(rankResult, documentID));
        }
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < relevantDocsAndRankResult.size() && i < RESULTNUMBER; i++) {
            result.add(relevantDocsAndRankResult.remove().getValue());
        }
        return result;
    }

    /**
     * Returns a map of all the documents which contains both the original query terms and terms from the semantic expansion
     *
     * @param semanticExpandedTerms
     * @param processedExpandedQuery
     * @param indexer
     * @param parser
     * @param relevantDocumentsDetails
     * @return
     */
    private HashMap<String, Pair<Integer, HashMap<String, Integer>>> getRelevantDocumentsForExpandedQuery(String semanticExpandedTerms, ArrayList<TermDocumentTrio> processedExpandedQuery, Indexer indexer, Parse parser, HashMap<String, Pair<Integer, HashMap<String, Integer>>> relevantDocumentsDetails) {
        HashMap<String, Pair<Integer, HashMap<String, Integer>>> expandedQueryDocumentsDetails = null;

        if (!semanticExpandedTerms.isEmpty()) { //use semantics
            //get relevant documents details
            HashSet<String> expandedQueryTerms = new HashSet<>();
            for (TermDocumentTrio trio : processedExpandedQuery) {
                expandedQueryTerms.add(trio.getTerm());
            }
            expandedQueryDocumentsDetails = getRelevantDocumentsDetails(expandedQueryTerms, indexer);


            ArrayList<String> toRemove = new ArrayList<>();

            for (Map.Entry<String, Pair<Integer, HashMap<String, Integer>>> entry : expandedQueryDocumentsDetails.entrySet()) {
                String documentId = entry.getKey();
                if (!relevantDocumentsDetails.containsKey(documentId)) {
                    toRemove.add(documentId);
                }
            }

            for (String documentIDToRemove : toRemove) {
                expandedQueryDocumentsDetails.remove(documentIDToRemove);
            }
        }
        return expandedQueryDocumentsDetails;
    }

    /**
     * Merges all the given Lists to create an aggregated HashSet
     * of all the terms in them.
     *
     * @param secondList - ArrayList<TermDocumentTrio> - a list of TermDocumentTrio to merge all the terms in it.
     * @param thirdList  - ArrayList<TermDocumentTrio> - a list of TermDocumentTrio to merge all the terms in it.
     * @return - HashSet<String> - all the terms in the given Lists.
     */
    private HashSet<String> mergeLists(ArrayList<TermDocumentTrio> secondList, ArrayList<TermDocumentTrio> thirdList) {
        HashSet<String> queryAssociatedTerms = new HashSet<>();
        if (secondList != null) {
            for (TermDocumentTrio trio : secondList) {
                queryAssociatedTerms.add(trio.getTerm());
            }
        }
        if (thirdList != null) {
            for (TermDocumentTrio trio : thirdList) {
                queryAssociatedTerms.add(trio.getTerm());
            }
        }
        return queryAssociatedTerms;
    }

    private ArrayList<TermDocumentTrio> parseQuery(String DocNO, String query, Parse parse) {

        ArrayList<String> bagOfWords = parse.parseQuery(query);
        ArrayList<TermDocumentTrio> processedQuery = Mapper.processBagOfWords(true, DocNO, "", bagOfWords, "");
        return processedQuery;
    }

    // HashMap(DocID ,Pair(Document length , HasMap( Term , Document frequency)))
    private HashMap<String, Pair<Integer, HashMap<String, Integer>>> getRelevantDocumentsDetails(HashSet<String> processedQuery, Indexer indexer) {
        HashMap<String, Pair<Integer, HashMap<String, Integer>>> relevantDocumentsDetails = new HashMap<>();
        for (String term : processedQuery) {
            ArrayList<Pair<String, Integer>> allPairs = indexer.getTermPosting(term);
            if (allPairs != null) {
                for (int i = 0; i < allPairs.size(); i++) {
                    String documentID = allPairs.get(i).getKey();
                    int tf = allPairs.get(i).getValue();
                    HashMap<String, Integer> docTerms = new HashMap<>();
                    if (!relevantDocumentsDetails.containsKey(documentID)) {
                        docTerms.put(term, tf);
                        Pair<Integer, HashMap<String, Integer>> pair = new Pair<>(indexer.getDocumentLength(documentID), docTerms);
                        relevantDocumentsDetails.put(documentID, pair);
                    } else {
                        docTerms = relevantDocumentsDetails.get(documentID).getValue();
                        int previousTF = 0;
                        if (docTerms.containsKey(term)) {
                            previousTF = docTerms.get(term);
                        }
                        //docTerms.putAll(relevantDocumentsDetails.get(documentID).getValue());
                        docTerms.put(term, tf + previousTF);
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
     *
     * @param documentEntities
     * @param documentHeader
     * @return - String[] - an sorted array of the entities, based on importance.
     */
    public ArrayList<Pair<String, Double>> rankEntities(HashMap<String, Integer> documentEntities, String documentHeader, Parse parser) {

        return ranker.rankEntities(documentEntities, parseQuery("document", documentHeader, parser));
    }


}
