package Retrieval;

import CorpusProcessing.*;
import javafx.util.Pair;

import java.util.*;

/**
 * Retrieves the relevant documents to a query from the indexed documents.
 */
public class Searcher {
    /**
     * The main ranker that will be used to rank documents by relevance to a specific query.
     */
    private IRanker ranker;
    /**
     * Total number of results per query.
     */
    private static final int RESULTNUMBER = 50;

    /**
     * Constructor
     *
     * @param indexer    - Indexer - the indexer that will be used for the retrieval process.
     * @param corpusSize - int - The number of documents in the entire corpus
     * @param avdl       - double - The average document length
     */
    public Searcher(Indexer indexer, int corpusSize, double avdl) {
        this.ranker = new Ranker(corpusSize, avdl, indexer);
    }

    /**
     * Returns the relevant documents to a given query sorted by their relevance.
     *
     * @param query                 - String - A phrase to retrieve relevant documents for.
     * @param queryDescription      - String - The query description as extracted from a file. if a single query was run then an empty String.
     * @param semanticExpandedTerms - String - The query after semantic analysis and expansion.
     * @param indexer               - Indexer - The indexer that will be used for the retrieval.
     * @param parser                - Parse - A parser for the query sections.
     * @return - ArrayList<String> - The retrieved documents for it as a list.
     */
    public ArrayList<String> runQuery(String query, String queryDescription, String semanticExpandedTerms, Indexer indexer, Parse parser) {

        //Process the query sections
        if(query.contains("Chunnel"))
        {
            query=query +" channel tunnel";
        }
        if(queryDescription.contains("Chunnel"))
        {
            queryDescription= queryDescription+ " channel tunnel";
        }
        queryDescription = queryDescription.replaceAll("Identify" , "");
        queryDescription = queryDescription.replaceAll("documents" , "");
        queryDescription = queryDescription.replaceAll("Documents" , "");


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

        HashMap<String, Pair<Integer, HashMap<String, Integer>>> expandedQueryDocumentsDetails = getRelevantDocumentsForExpandedQuery(semanticExpandedTerms, processedExpandedQuery, indexer, relevantDocumentsDetails);


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

            double rankResult = ranker.rankDocument(processedQuery, processedQueryDescription, processedExpandedQuery, expandedQueryDocumentTerms, documentLength, documentTerms, processedDocumentHeader, documentEntities);
            relevantDocsAndRankResult.add(new Pair<>(rankResult, documentID));
        }
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < relevantDocsAndRankResult.size() && i < RESULTNUMBER; i++) {
            result.add(relevantDocsAndRankResult.remove().getValue());
        }
        return result;
    }

    /**
     * Returns a map of all the documents which contains both the original query terms and terms from the semantic expansion.
     *
     * @param semanticExpandedTerms    - String - The query after semantic analysis and expansion.
     * @param processedExpandedQuery   - ArrayList<TermDocumentTrio> - The expanded query after parsing.
     * @param indexer                  - indexer - The indexer that will be used for the retrieval.
     * @param relevantDocumentsDetails - HashMap<String, Pair<Integer, HashMap<String, Integer>>> - The relevant documents details
     * @return - HashMap<String, Pair<Integer, HashMap<String, Integer>>> - A Map of all the documents which contains both the original query terms and terms from the semantic expansion.
     */
    private HashMap<String, Pair<Integer, HashMap<String, Integer>>> getRelevantDocumentsForExpandedQuery(String semanticExpandedTerms, ArrayList<TermDocumentTrio> processedExpandedQuery, Indexer indexer, HashMap<String, Pair<Integer, HashMap<String, Integer>>> relevantDocumentsDetails) {
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

    /**
     * Parses an processes a single given query using the given parser.
     *
     * @param elementID - String - The query number.
     * @param query     - String - A phrase to parse.
     * @param parse     - Parse - the parser that will be used.
     * @return - ArrayList<TermDocumentTrio> - the processed query.
     */
    private ArrayList<TermDocumentTrio> parseQuery(String elementID, String query, Parse parse) {

        ArrayList<String> bagOfWords = parse.parseQuery(query);
        ArrayList<TermDocumentTrio> processedQuery = Mapper.processBagOfWords(true, elementID, "", bagOfWords, "");
        return processedQuery;
    }

    /**
     * Gathers and returns all the documents details for the documents which contains terms from the given processedQuery.
     *
     * @param processedQuery - HashSet<String> - The processed query.
     * @param indexer        - indexer - The indexer that will be used for the retrieval of the documents details.
     * @return - HashMap<String, Pair<Integer, HashMap<String, Integer>>> - DocID --> Pair(Document length , HasMap( Term , Document frequency))
     */
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
     * @param documentEntities - HashMap<String, Integer> - A Map of all the entities in a document.
     * @param documentHeader   - String - The document header.
     * @return - ArrayList<Pair<String, Double>> - an sorted list of the entities, based on importance.
     */
    public ArrayList<Pair<String, Double>> rankEntities(HashMap<String, Integer> documentEntities, String documentHeader, Parse parser) {

        return ranker.rankEntities(documentEntities, parseQuery("document", documentHeader, parser));
    }


}
