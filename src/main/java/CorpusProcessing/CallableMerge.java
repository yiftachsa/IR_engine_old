package CorpusProcessing;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * A Callable Class for merging two lists
 */
public class CallableMerge implements Callable<ArrayList<TermDocumentTrio>> {
    private ArrayList<TermDocumentTrio> postingEntriesList1;
    private ArrayList<TermDocumentTrio> postingEntriesList2;

    /**
     * Constructor
     * @param list1 - ArrayList<Trio>
     * @param list2 - ArrayList<Trio>
     */
    public CallableMerge(ArrayList<TermDocumentTrio> list1, ArrayList<TermDocumentTrio> list2) {
        this.postingEntriesList1 = list1;
        this.postingEntriesList2 = list2;
    }

    @Override
    public ArrayList<TermDocumentTrio> call() throws Exception {
        return Mapper.mergeAndSortTwoPostingEntriesLists(postingEntriesList1,postingEntriesList2);
    }

}
