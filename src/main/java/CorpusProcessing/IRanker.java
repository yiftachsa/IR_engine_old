package CorpusProcessing;

import java.util.ArrayList;
import java.util.HashMap;

public interface IRanker {

    double rankDocument(ArrayList<TermDocumentTrio> query , String documentID, int documentLength , HashMap<String , Integer> documentTerms);

    String[] rankEntities(HashMap<String, Integer> documentEntities, ArrayList<TermDocumentTrio> processedDocumentHeader);

}
