package CorpusProcessing;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public interface IRanker {

    double rankDocument(ArrayList<TermDocumentTrio> query , String documentID, int documentLength , HashMap<String , Integer> documentTerms , ArrayList<TermDocumentTrio> documentHeader);

    ArrayList<Pair<String, Double>> rankEntities(HashMap<String, Integer> documentEntities, ArrayList<TermDocumentTrio> processedDocumentHeader);

}
