package Retrieval;

import CorpusProcessing.Indexer;
import CorpusProcessing.TermDocumentTrio;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ranker implements IRanker {

    private int corpusSize;
    private double avdl;
    private Indexer indexer;


    private static final double b = 0.7;
    private static final double k = 1.81;

    private static double WEIGHT_QUERY_BM25 = 1;
    private static double WEIGHT_QUERYDESC_BM25 = 1.5;
    private static double WEIGHT_HEADER = 0.05;
    private static double WEIGHT_ENTITIES = 0.2;

    /**
     * Constructor
     *
     * @param corpusSize - int - The corpus size
     * @param avdl       - double - average document length
     * @param indexer    - Indexer - indexer
     */
    public Ranker(int corpusSize, double avdl, Indexer indexer) {
        this.corpusSize = corpusSize;
        this.avdl = avdl;
        this.indexer = indexer;
    }

    // rank calculators -
    // 1. BM25
    // 2. Jaccard Similarity between the query and documentHeader.
    // 3. DSC (Dice) between the query and the document entities.
    @Override
    public double rankDocument(ArrayList<TermDocumentTrio> query, ArrayList<TermDocumentTrio> queryDescription, ArrayList<TermDocumentTrio> expandedQuery, HashMap<String, Integer> semanticExpandedTerms, int documentLength, HashMap<String, Integer> documentTerms, ArrayList<TermDocumentTrio> documentHeader, ArrayList<String> documentEntities) {

        boolean shortQuery = false;
        if (query.size() <= 2) {
            shortQuery = true;
            WEIGHT_QUERYDESC_BM25 = WEIGHT_QUERYDESC_BM25 * 2;
        }
        double queryBM25Rank;
        if (semanticExpandedTerms != null && semanticExpandedTerms != null) { //use semantics
            ArrayList<TermDocumentTrio> mergedQuery = mergeQueryAndExpandedQueryTrios(query, expandedQuery);
            HashMap<String, Integer> mergedDocumentTerms = mergeQueryAndExpandedQueryDocumentsTerms(semanticExpandedTerms, documentTerms);
            queryBM25Rank = BM25calculator(mergedQuery, documentLength, mergedDocumentTerms);
        } else {
            queryBM25Rank = BM25calculator(query, documentLength, documentTerms);
        }
        double queryDescriptionBM25Rank = 0;
        if(queryDescription!=null && !queryDescription.isEmpty()){
            queryDescriptionBM25Rank = BM25calculator(queryDescription, documentLength, documentTerms);
        }


        List<String> queryTerms = extractTerms(query);
        List<String> documentHeaderTerms = extractTerms(documentHeader);
        double headerJaccardRank = JaccardCalculator(queryTerms, documentHeaderTerms);
        double entitiesDSCRank = DSCCalculator(queryTerms, documentEntities);

        double finalRank = ((WEIGHT_QUERY_BM25 * queryBM25Rank) + (WEIGHT_QUERYDESC_BM25 * queryDescriptionBM25Rank) + (WEIGHT_HEADER * headerJaccardRank) + (WEIGHT_ENTITIES * entitiesDSCRank));
        if (shortQuery) {
            WEIGHT_QUERYDESC_BM25 = WEIGHT_QUERYDESC_BM25 / 2;
        }

        return finalRank;
    }


    /**
     * Computes BM25 between a given query and a given document.
     * https://en.wikipedia.org/wiki/Okapi_BM25
     *
     * @param query          - ArrayList<TermDocumentTrio> - The query after parsing
     * @param documentLength - int - The length of the document.
     * @param documentTerms  - HashMap<String, Integer> - All the terms and their frequencies from the document.
     * @return - double - BM25 rank between the given query and the given document.
     */

    public double BM25calculator(ArrayList<TermDocumentTrio> query, int documentLength, HashMap<String, Integer> documentTerms) {
        double sigma = 0;
        double documentLengthRatio = documentLength / (this.avdl * 2);
        double lengthFactor = k * (1 - b + b * documentLengthRatio);
        for (TermDocumentTrio trio : query) {
            String term = trio.getTerm();
            double tfQ = trio.getFrequency();
            double tfD = 0;
            if (documentTerms.containsKey(term)) {
                tfD = documentTerms.get(term);
            }
            double divide = ((k + 1) * tfD) / (tfD + lengthFactor);
            double dfT = indexer.getDocumentFrequency(term);
            if (dfT <= 0) {
                continue;
            }
            double logCalc = Math.log10(((corpusSize + 1) / (dfT)));
            sigma = sigma + (tfQ * divide * logCalc);
        }
        return sigma;
    }


    /**
     * Computes dice coefficient
     * https://en.wikipedia.org/wiki/S%C3%B8rensen%E2%80%93Dice_coefficient
     *
     * @param queryTerms       - List<String> - A list of the terms in the query.
     * @param documentEntities - ArrayList<String> - A list of the document entities.
     * @return - double - DSC rank between the given queryTerms and the given documentEntities.
     */
    private double DSCCalculator(List<String> queryTerms, ArrayList<String> documentEntities) {
        int total = queryTerms.size() + documentEntities.size();
        if (documentEntities.size() == 0 || total == 0) {
            return 0;
        }
        int totalInCommon = 0;
        for (String termFromQuery : queryTerms) {
            for (String entity : documentEntities) {
                if (termFromQuery.equals(entity)) {
                    totalInCommon++;
                }
            }
        }

        double DSC = (2 * totalInCommon) / total;
        return DSC;
    }

    /**
     * Computes Jaccard similarity.
     * https://en.wikipedia.org/wiki/Jaccard_index
     *
     * @param queryTerms     - List<String> - A list of the terms in the query.
     * @param documentHeader - List<String> - A list of the terms in the document header.
     * @return - double - Jaccard similarity rank between the given queryTerms and the given documentHeader.
     */
    private double JaccardCalculator(List<String> queryTerms, List<String> documentHeader) {
        int total = queryTerms.size() + documentHeader.size();
        int totalInCommon = 0;
        for (String termFromQuery : queryTerms) {
            for (String termFromHeader : documentHeader) {
                if (termFromQuery.equals(termFromHeader)) {
                    totalInCommon++;
                }
            }
        }
        if (total == totalInCommon) {
            return 0;
        }
        return totalInCommon / (total - totalInCommon);
    }

    /**
     * Receives two hash maps and merges them.
     *
     * @param semanticExpandedTerms - HashMap<String, Integer> - A map to merge.
     * @param documentTerms         - HashMap<String, Integer> - A map to merge.
     * @return - HashMap<String, Integer> - A merged map.
     */
    private HashMap<String, Integer> mergeQueryAndExpandedQueryDocumentsTerms(HashMap<String, Integer> semanticExpandedTerms, HashMap<String, Integer> documentTerms) {
        HashMap<String, Integer> mergedDocumentsTerms = new HashMap<>();
        mergedDocumentsTerms.putAll(semanticExpandedTerms);
        mergedDocumentsTerms.putAll(documentTerms);
        return mergedDocumentsTerms;
    }

    /**
     * Receives two array lists and merges them.
     *
     * @param query         - ArrayList<TermDocumentTrio> - A list to merge.
     * @param expandedQuery - ArrayList<TermDocumentTrio> - A list to merge.
     * @return - ArrayList<TermDocumentTrio> - A merged list.
     */
    private ArrayList<TermDocumentTrio> mergeQueryAndExpandedQueryTrios(ArrayList<TermDocumentTrio> query, ArrayList<TermDocumentTrio> expandedQuery) {
        ArrayList<TermDocumentTrio> mergedQuery = new ArrayList<>();
        mergedQuery.addAll(query);
        mergedQuery.addAll(expandedQuery);
        return mergedQuery;
    }

    /**
     * return list of terms from ArrayList<TermDocumentTrio> documentTrios
     *
     * @param documentTrios - ArrayList<TermDocumentTrio> - A list to extract terms from.
     * @return - List<String> - All the terms in the given list.
     */
    private List<String> extractTerms(ArrayList<TermDocumentTrio> documentTrios) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < documentTrios.size(); i++) {
            result.add(documentTrios.get(i).getTerm());
        }
        return result;
    }


    @Override
    public ArrayList<Pair<String, Double>> rankEntities(HashMap<String, Integer> documentEntities, ArrayList<TermDocumentTrio> processedDocumentHeader) {
        ArrayList<Pair<String, Double>> result = new ArrayList<>();

        Pair<Double, String>[] rankedEntities = new Pair[documentEntities.size()];
        int index = 0;

        for (Map.Entry<String, Integer> entry : documentEntities.entrySet()) {
            String entity = entry.getKey();
            int docFrequency = entry.getValue();
            int docHeaderFrequency = 0;

            for (TermDocumentTrio trio : processedDocumentHeader) {
                if (trio.getTerm().equals(entity)) {
                    docHeaderFrequency = trio.getFrequency();
                    break;
                }
            }

            double entityScore = rankEntity(docFrequency, docHeaderFrequency);

            rankedEntities[index++] = new Pair<>(entityScore, entity);
        }


        //take the top five
        for (int i = 0; i < 5; i++) {
            double maxValue = 0;
            int maxIndex = -1;
            for (int j = 0; j < rankedEntities.length; j++) {
                if (rankedEntities[j] != null) {
                    if (rankedEntities[j].getKey() > maxValue) {
                        maxValue = rankedEntities[j].getKey();
                        maxIndex = j;
                    }
                }
            }
            if (maxIndex > -1) {
                result.add(new Pair<>(rankedEntities[maxIndex].getValue(), maxValue));
                rankedEntities[maxIndex] = null;
            } else {
                result.add(new Pair<>("null", (double) -1));
            }
        }
        return result;
    }

    /**
     * Ranks a single entity based on its Frequency in the document and in the document header.
     *
     * @param docFrequency       - int - The number of times that the entity appears in the documents.
     * @param docHeaderFrequency - int - The number of times that the entity appears in the document header.
     * @return
     */
    private double rankEntity(int docFrequency, int docHeaderFrequency) {
        double docFrequencyWeight = 0.3;
        double docHeaderFrequencyWeight = 0.7;

        return (docFrequencyWeight * docFrequency + docHeaderFrequencyWeight * docHeaderFrequency);
    }
}
