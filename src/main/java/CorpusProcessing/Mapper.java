package CorpusProcessing;

import java.util.ArrayList;

/**
 * Responsible for processing the bag of words of each document and deliver an ordered list
 * of posting entries. (trio) also updates the tf for each term.
 */
public class Mapper {

    public static ArrayList<Trio> proceesBagOfWords(String DocNO, ArrayList<String> terms){
        ArrayList<Trio> postingEntries = new ArrayList<Trio>();
        //Sort the List
        terms.sort(String::compareTo);
        //Merge the identical terms and calculate the frequency
        int termFrequency = 1;
        for (int i = 0; i < terms.size()-1; i++) {
            String term = terms.get(0); //ArrayList.get() time complexity is O(1)
            if (term.equals(terms.get(i+1))){
                termFrequency++;
            } else {
                postingEntries.add(new Trio(term,DocNO,termFrequency));
                termFrequency = 1;
            }
        }
        return postingEntries;
    }
}
