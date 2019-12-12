package CorpusProcessing;

import java.util.ArrayList;
import java.util.concurrent.Callable;


public class CallableMerge implements Callable<ArrayList<Trio>> {
    private final ArrayList<ArrayList<Trio>> allPostingEntriesLists;

    public CallableMerge(ArrayList<ArrayList<Trio>> allPostingEntriesLists) {
        this.allPostingEntriesLists = allPostingEntriesLists;
    }
    @Override
    public ArrayList<Trio> call() throws Exception {
        return Mapper.mergeAndSortTwoPostingEntriesLists(allPostingEntriesLists.remove(0), allPostingEntriesLists.remove(0));
    }

}
