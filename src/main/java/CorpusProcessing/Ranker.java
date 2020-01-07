package CorpusProcessing;

import java.util.ArrayList;
import java.util.HashMap;

//todo- maybe we should make the ranker as an abstract class - which every different calc of rank will be class which extend the abstract ranker class
public class Ranker implements IRanker {

    private int corpusSize;
    private double avdl;
    private Indexer indexer;


    //todo: optimize
    private static final double b = 0.75;
    private static final double k = 1.2;

    public double BM25calculator(ArrayList<TermDocumentTrio> query , String documentID, int documentLength ,HashMap<String , Integer> documentTerms)
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

}
