package CorpusProcessing;

import java.util.ArrayList;

/**
 * Responsible for processing the bag of words of each document and deliver an ordered list
 * of posting entries. (trio) also updates the tf for each term.
 */
public class Mapper {

    public static ArrayList<Trio> processBagOfWords(String DocNO, ArrayList<String> terms){
        ArrayList<Trio> postingEntries = new ArrayList<Trio>();
        int maxTermFrequency = 1;

        //Sort the List
        terms.sort(String::compareTo);
        //Merge the identical terms and calculate the frequency
        int termFrequency = 1;
        for (int i = 0; i < terms.size()-1; i++) {
            String term = terms.get(i); //ArrayList.get() time complexity is O(1)
            if (term.equals(terms.get(i+1))){
                termFrequency++;
            } else {
                postingEntries.add(new Trio(term,DocNO,termFrequency));
                if(termFrequency>maxTermFrequency){
                    maxTermFrequency = termFrequency;
                }
                termFrequency = 1;
            }
        }
        postingEntries.add(new Trio(terms.get(terms.size()-1),DocNO,termFrequency));
        if(termFrequency>maxTermFrequency){
            maxTermFrequency = termFrequency;
        }
        Documenter.saveDocumentDetails(DocNO, maxTermFrequency,postingEntries.size(), terms.size());

        return postingEntries;
    }


    public static ArrayList<Trio> mergeAndSortTwoPostingEntriesLists(ArrayList<Trio> list1 , ArrayList<Trio> list2)
    {
        //TODO: check time complexity in compression to a simple merge sort geek to geek
        ArrayList<Trio> mergedList = new ArrayList<>();
        while (list1.size() > 0 && list2.size() > 0) {

            if(list1.get(0).compareTo(list2.get(0))<0)
            {
                mergedList.add(list1.remove(0));
            }
            else
            {
                mergedList.add(list2.remove(0));
            }
        }
        if(list1.size() == 0)
        {
            mergedList.addAll(list2);
        }
        else if(list2.size() == 0)
        {
            mergedList.addAll(list1);
        }
        return  mergedList;
    }

}
