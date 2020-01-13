package CorpusProcessing;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//todo- maybe we should make the ranker as an abstract class - which every different calc of rank will be class which extend the abstract ranker class
public class Ranker implements IRanker {

    private int corpusSize;
    private double avdl;
    private Indexer indexer;


    //todo: optimize
    private static final double b = 0.7;
    private static final double k = 1.81;

    private static final double WEIGHT_QUERY_BM25 = 1;
    private static final double WEIGHT_QUERYDESC_BM25 = 0.4;
    private static final double WEIGHT_HEADER = 0.1;
    private static final double WEIGHT_ENTITIES = 0.4;

    public Ranker(int corpusSize, double avdl, Indexer indexer) {
        this.corpusSize = corpusSize;
        this.avdl = avdl;
        this.indexer = indexer;
    }

    // rank calculators -
    // 1. BM25
    // 2. Jaccard Similarity between query and documentHeader
    // 3. Entities - number of entities in both / total number of entities in the document
    @Override
    public double rankDocument(ArrayList<TermDocumentTrio> query,ArrayList<TermDocumentTrio> queryDescription , String documentID, int documentLength, HashMap<String, Integer> documentTerms, ArrayList<TermDocumentTrio> documentHeader, ArrayList<String> documentEntities) {

        double queryBM25Rank = BM25calculator(query, documentID, documentLength, documentTerms);
        double queryDescriptionBM25Rank = BM25calculator(queryDescription, documentID, documentLength, documentTerms);



        List<String> queryTerms = extractTerms(query);
        List<String> documentHeaderTerms = extractTerms(documentHeader);
        double headerJaccardRank = JaccardCalculator(queryTerms, documentHeaderTerms);
        double entitiesDSCRank = DSCCalculator(queryTerms, documentEntities);


        double finalRank = ((WEIGHT_QUERY_BM25 * queryBM25Rank) + (WEIGHT_QUERYDESC_BM25*queryDescriptionBM25Rank) + (WEIGHT_HEADER * headerJaccardRank) + (WEIGHT_ENTITIES * entitiesDSCRank));
        return finalRank;
    }

    /**
     * Dice coefficient
     *
     * @param queryTerms
     * @param documentEntities
     * @return
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
     * return list of terms from ArrayList<TermDocumentTrio> documentTrios
     *
     * @param documentTrios
     * @return
     */
    private List<String> extractTerms(ArrayList<TermDocumentTrio> documentTrios) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < documentTrios.size(); i++) {
            result.add(documentTrios.get(i).getTerm());
        }
        return result;
    }

    public double BM25calculator(ArrayList<TermDocumentTrio> query, String documentID, int documentLength, HashMap<String, Integer> documentTerms) {
        double sigma = 0;
        double documentLengthRatio = documentLength / (this.avdl*2);
        double lengthFactor = k * (1 - b + b * documentLengthRatio);
        for (TermDocumentTrio trio : query) {
            String term = trio.getTerm();
            int tfQ = trio.getFrequency();
            int tfD = 0;
            if (documentTerms.containsKey(term)) {
                tfD = documentTerms.get(term);
            }
            double divide = ((k + 1) * tfD) / (tfD + lengthFactor);
            int dfT = indexer.getDocumentFrequency(term);
            if (dfT <= 0) {
                continue;
            }
            double logCalc = Math.log10(((corpusSize + 1) / (dfT)));
            sigma = sigma + (tfQ * divide * logCalc);
        }
        return sigma;
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


        //todo:take the top five
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


    private double rankEntity(int docFrequency, int docHeaderFrequency) {
        double docFrequencyWeight = 0.5;
        double docHeaderFrequencyWeight = 0.5;

        return (docFrequencyWeight * docFrequency + docHeaderFrequencyWeight * docHeaderFrequency);
    }
}
