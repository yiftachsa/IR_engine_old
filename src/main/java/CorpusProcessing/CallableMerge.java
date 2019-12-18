package CorpusProcessing;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * A Callable Class for merging two lists
 */
public class CallableMerge implements Callable<ArrayList<Trio>> {
    private ArrayList<Trio> postingEntriesList1;
    private ArrayList<Trio> postingEntriesList2;

    /**
     * Constructor
     * @param list1 - ArrayList<Trio>
     * @param list2 - ArrayList<Trio>
     */
    public CallableMerge(ArrayList<Trio> list1,ArrayList<Trio> list2) {
        this.postingEntriesList1 = list1;
        this.postingEntriesList2 = list2;
    }

    @Override
    public ArrayList<Trio> call() throws Exception {
        return Mapper.mergeAndSortTwoPostingEntriesLists(postingEntriesList1,postingEntriesList2);
    }

}
