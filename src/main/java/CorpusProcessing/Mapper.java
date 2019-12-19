package CorpusProcessing;

import java.util.ArrayList;

/**
 * Responsible for processing the bag of words of each document and deliver an ordered list
 * of posting entries. (trio) also updates the tf for each term.
 */
public class Mapper {



    /**
     * Processes a given bag of words and deliver an ordered list of posting entries (trio).
     * Updates the tf for each term in the bag of words.
     * Saves all the relevant details os the document to memory.
     *
     * @param DocNO - String - the id of the document which contains the terms.
     * @param terms - ArrayList<String> - the terms to be converted into trios
     * @return - ArrayList<Trio> - ordered list of posting entries
     */
    public static ArrayList<TermDocumentTrio> processBagOfWords(String DocNO, String documentDate, ArrayList<String> terms) {
        ArrayList<TermDocumentTrio> postingEntries = new ArrayList<TermDocumentTrio>();
        int maxTermFrequency = 1;

        //Sort the List
        terms.sort(String::compareTo);
        //Merge the identical terms and calculate the frequency
        int termFrequency = 1;
        for (int i = 0; i < terms.size() - 1; i++) {
            String term = terms.get(i); //ArrayList.get() time complexity is O(1)
            if (term.equals(terms.get(i + 1))) {
                termFrequency++;
            } else {
                postingEntries.add(new TermDocumentTrio(term, DocNO, termFrequency));
                if (termFrequency > maxTermFrequency) {
                    maxTermFrequency = termFrequency;
                }
                termFrequency = 1;
            }
        }
        if (terms.size() > 0) {
            postingEntries.add(new TermDocumentTrio(terms.get(terms.size() - 1), DocNO, termFrequency));
            if (termFrequency > maxTermFrequency) {
                maxTermFrequency = termFrequency;
            }
        }

        Documenter.saveDocumentDetails(DocNO, maxTermFrequency, postingEntries.size(), terms.size(), documentDate);

        return postingEntries;
    }

    /**
     * Receives two Trios lists, merges and sort them.
     *
     * @param list1 - ArrayList<Trio> - the first list to be merged
     * @param list2 - ArrayList<Trio> - the second list to be merged
     * @return - ArrayList<Trio> - the merged list
     */
    public static ArrayList<TermDocumentTrio> mergeAndSortTwoPostingEntriesLists(ArrayList<TermDocumentTrio> list1, ArrayList<TermDocumentTrio> list2) {
        ArrayList<TermDocumentTrio> mergedList = new ArrayList<>();
        while (list1.size() > 0 && list2.size() > 0) {

            if (list1.get(0).compareTo(list2.get(0)) < 0) {
                mergedList.add(list1.remove(0));
            } else {
                mergedList.add(list2.remove(0));
            }
        }
        if (list1.size() == 0) {
            mergedList.addAll(list2);
        } else if (list2.size() == 0) {
            mergedList.addAll(list1);
        }
        return mergedList;
    }



}
