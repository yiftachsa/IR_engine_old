package CorpusProcessing;

import java.util.ArrayList;
import java.util.concurrent.Callable;


public class CallableMerge implements Callable<ArrayList<Trio>> {
    private ArrayList<Trio> postingEntriesList1;
    private ArrayList<Trio> postingEntriesList2;

    public CallableMerge(ArrayList<Trio> postingEntriesList1,ArrayList<Trio> postingEntriesList2) {
        this.postingEntriesList1 = postingEntriesList1;
        this.postingEntriesList2 = postingEntriesList2;

    }
    @Override
    public ArrayList<Trio> call() throws Exception {
        return Mapper.mergeAndSortTwoPostingEntriesLists(postingEntriesList1,postingEntriesList2);
    }

}
