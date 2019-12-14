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
    private static final int POSTINGLINESPERPOSTINGPORTION = (int) Math.pow(2, 5);


    public static void mergeAllPostingEntries() {
        //ExecutorService documentLoadersPool = Executors.newFixedThreadPool(LOADERSPOOLSIZE);
        int longestPostingEntriesFile = Documenter.getLongestPostingEntriesFile();
        int postingEntriesCount = Documenter.getPostingEntriesIndex();


        ExecutorService mergersPool = Executors.newFixedThreadPool(MERGERSPOOLSIZE);

        //ArrayList<Future<ArrayList<Trio>>> futureLoaders = new ArrayList<>();
        ArrayList<Future<ArrayList<Trio>>> futuresMerge = new ArrayList<>();

        ArrayList<ArrayList<Trio>> allPostingEntriesPortions = new ArrayList<>();

        int numberOfPostingPortions = longestPostingEntriesFile / POSTINGLINESPERPOSTINGPORTION;

        for (int i = 0; i < numberOfPostingPortions; i++) {
            //If there is 1000 postingEntries, will be 998 threads that will wait - is that ok? memory complexity!!!!
            int currentPostingEntreeIndex = 0;
            while (currentPostingEntreeIndex < postingEntriesCount) {
                //FIXME: The read takes a lot of time. Maybe return the use of CallableRead
                ArrayList<Trio> currentPostingEntries = readPostingEntree(currentPostingEntreeIndex, i);
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
                System.out.println("PostingPortion: " + i + " current PostingEntries file: " + currentPostingEntreeIndex);
                currentPostingEntreeIndex++;
            }
            //Waiting for all the files to be loaded and merged
//            while (futuresMerge.size() > 0) {
//                int finishedFuture = -1;
//                for (int j = 0; j < futuresMerge.size(); j++) {
//                    if (futuresMerge.get(i).isDone()) {
//                        finishedFuture = i;
//                    }
//                }
//                if (finishedFuture >= 0){
//                    Future<ArrayList<Trio>> futureMerge = futuresMerge.remove(finishedFuture);
//                    try {
//                        allPostingEntriesPortions.add(futureMerge.get());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }

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

            while (allPostingEntriesPortions.size() > 1) {
                allPostingEntriesPortions.add(Mapper.mergeAndSortTwoPostingEntriesLists(allPostingEntriesPortions.remove(0), allPostingEntriesPortions.remove(0)));
            }

            Documenter.savePostingPortions(allPostingEntriesPortions.get(0), i);

            //iterationNumber++;
            //CallableRead.setIterationNumber(iterationNumber);
        }


        mergersPool.shutdown();
    }

    /**
     * Reads POSTINGLINESPERPOSTINGPORTION number of lines from an individual posting entries list
     *
     * @param postingEntreeIndex
     * @param iterationNumber
     * @return
     */
    //TODO: RENAME FUNCTION
    private static ArrayList<Trio> readPostingEntree(int postingEntreeIndex, int iterationNumber) {

        ArrayList<Trio> trioArrayList = Documenter.loadPostingEntree(postingEntreeIndex);

        if (POSTINGLINESPERPOSTINGPORTION * (iterationNumber + 1) < trioArrayList.size()) {
            trioArrayList.subList(POSTINGLINESPERPOSTINGPORTION * iterationNumber, POSTINGLINESPERPOSTINGPORTION * (iterationNumber + 1));
        } else if (POSTINGLINESPERPOSTINGPORTION * iterationNumber < trioArrayList.size()) {
            trioArrayList.subList(POSTINGLINESPERPOSTINGPORTION * iterationNumber, trioArrayList.size() - 1);
        } else {
            trioArrayList = new ArrayList<>();
        }

        return trioArrayList;
    }


}
