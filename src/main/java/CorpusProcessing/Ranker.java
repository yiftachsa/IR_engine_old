package CorpusProcessing;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

//todo- maybe we should make the ranker as an abstract class - which every different calc of rank will be class which extend the abstract ranker class
public class Ranker implements IRanker {

    private int corpusSize;
    private double avdl;
    private Indexer indexer;


    //todo: optimize
    private static final double b = 0.75;
    private static final double k = 1.2;


    @Override
    public double rankDocument(ArrayList<TermDocumentTrio> query, String documentID, int documentLength, HashMap<String, Integer> documentTerms) {
        return BM25calculator(query, documentID, documentLength, documentTerms);
    }

    public double BM25calculator(ArrayList<TermDocumentTrio> query , String documentID, int documentLength , HashMap<String , Integer> documentTerms)
    {
        double sigma = 0;
        double documentLengthRatio = documentLength/this.avdl;
        double lengthFactor = k * (1 - b + b * documentLengthRatio);
        for (TermDocumentTrio trio: query)
        {
            String term = trio.getTerm();
            int tfQ = trio.getFrequency();
            int tfD =  documentTerms.get(term);
            double divide = (( k + 1 ) * tfD) / (tfD + lengthFactor);
            int dfT = indexer.getDocumentFrequency(term);
            if(dfT < 0 )
            {
                continue;
            }
            double logCalc = Math.log10(( (corpusSize + 1) / (dfT)));
            sigma = sigma + (tfQ * divide * logCalc);
        }
        return sigma;
    }

    @Override
    public String[] rankEntities(HashMap<String, Integer> documentEntities, ArrayList<TermDocumentTrio> processedDocumentHeader) {
        String[] result = new String[5];

        Pair<Double,String>[] rankedEntities = new Pair[documentEntities.size()];
        int index = 0;

        for (Map.Entry<String, Integer> entry: documentEntities.entrySet()){
            String entity = entry.getKey();
            int docFrequency = entry.getValue();
            int docHeaderFrequency = 0;

            for (TermDocumentTrio trio: processedDocumentHeader){
                if(trio.getTerm().equals(entity)){
                    docHeaderFrequency = trio.getFrequency();
                    break;
                }
            }

            double entityScore = rankEntity(docFrequency, docHeaderFrequency);

            rankedEntities[index++] = new Pair<>(entityScore,entity);
        }


        //todo:take the top five
        for (int i = 0; i < 5; i++) {
            double maxValue = 0;
            int maxIndex = -1;
            for (int j = 0; j < rankedEntities.length; j++) {
                if(rankedEntities[j]!= null){
                    if(rankedEntities[j].getKey() > maxValue){
                        maxValue = rankedEntities[j].getKey();
                        maxIndex = j;
                    }
                }
            }
            if(maxIndex>-1){
                result[i] = rankedEntities[maxIndex].getValue();
            }else{
                result[i] = "null";
            }
        }
        return result;
    }


    private double rankEntity(int docFrequency, int docHeaderFrequency){
        double docFrequencyWeight = 0.5;
        double docHeaderFrequencyWeight = 0.5;

        return (docFrequencyWeight*docFrequency + docHeaderFrequencyWeight*docHeaderFrequency);
    }
}
