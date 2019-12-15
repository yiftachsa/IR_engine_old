package CorpusProcessing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class HorizontalMerger {

    private static final int MERGERSPOOLSIZE = 2;
    private static final int POSTINGLINESPERPOSTINGPORTION = (int) Math.pow(2, 10);


    public static void mergeAllPostingEntries() {
        //ExecutorService documentLoadersPool = Executors.newFixedThreadPool(LOADERSPOOLSIZE);
        int longestPostingEntriesFile = Documenter.getLongestPostingEntriesFile();
        int postingEntriesCount = Documenter.getPostingEntriesIndex();


        ExecutorService mergersPool = Executors.newFixedThreadPool(MERGERSPOOLSIZE);

        //ArrayList<Future<ArrayList<Trio>>> futureLoaders = new ArrayList<>();
        ArrayList<Future<ArrayList<Trio>>> futuresMerge = new ArrayList<>();

        ArrayList<ArrayList<Trio>> allPostingEntriesPortions = new ArrayList<>();

        int numberOfPostingPortions = longestPostingEntriesFile / POSTINGLINESPERPOSTINGPORTION;
//לא ברור לי איך השימוש בnumberOfPostinPortion בא לידי ביטוי
            //If there is 1000 postingEntries, will be 998 threads that will wait - is that ok? memory complexity!!!!
            int currentPostingEntreeIndex = 0;
            while (currentPostingEntreeIndex < postingEntriesCount) {
                //FIXME: The read takes a lot of time. Maybe return the use of CallableRead
                ArrayList<Trio> currentPostingEntries = readPostingEntree(currentPostingEntreeIndex);
                allPostingEntriesPortions.add(currentPostingEntries);

                //Check if a merge call is done
                if (futuresMerge.size() > 0) {
                    if (futuresMerge.get(0).isDone()) {
                        Future<ArrayList<Trio>> futureMerge = futuresMerge.remove(0);
                        try {
                            allPostingEntriesPortions.add(futureMerge.get());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                //MERGE
                if (allPostingEntriesPortions.size() > 1) {
                    Future<ArrayList<Trio>> futureMerge = mergersPool.submit(new CallableMerge(allPostingEntriesPortions.remove(0), allPostingEntriesPortions.remove(0))); //FIXME:: Race condition - remove here instead of inside the thread
                    futuresMerge.add(futureMerge);
                }


            for (Future<ArrayList<Trio>> future : futuresMerge) {
                while (!future.isDone()) {
                    try {
                        Thread.sleep(100);//TODO:CHECK!!!!!!!
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    allPostingEntriesPortions.add(future.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //take a lot of time
            while (allPostingEntriesPortions.size() > 1) {
                allPostingEntriesPortions.add(Mapper.mergeAndSortTwoPostingEntriesLists(allPostingEntriesPortions.remove(0), allPostingEntriesPortions.remove(0)));
            }
            //take time

            //iterationNumber++;
            //CallableRead.setIterationNumber(iterationNumber);
                currentPostingEntreeIndex++;
            }
        mergersPool.shutdown();
        Documenter.savePostingPortions(allPostingEntriesPortions.get(0),0);

    }

    /**
     * Reads POSTINGLINESPERPOSTINGPORTION number of lines from an individual posting entries list
     *
     * @param postingEntreeIndex
     * @return
     */
    //TODO: RENAME FUNCTION
    private static ArrayList<Trio> readPostingEntree(int postingEntreeIndex) {

        ArrayList<Trio> trioArrayList = Documenter.loadPostingEntree(postingEntreeIndex);

        return trioArrayList;
    }


}
