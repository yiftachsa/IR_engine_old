package CorpusProcessing;

import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class mergers the posting files within a letter directory
 */
public class RunnableMerge implements Runnable {

    private String path;
    private Map<String, DictionaryEntryTrio> dictionary;
    private ReentrantLock dictionaryMutex;

    /**
     * Constructor
     *
     * @param path       - String
     * @param dictionary - Map<String, DictionaryEntryTrio>
     */
    public RunnableMerge(String path, Map<String, DictionaryEntryTrio> dictionary, ReentrantLock reentrantLock) {
        this.path = path;
        this.dictionary = dictionary;
        this.dictionaryMutex = reentrantLock;
    }


    //merges all the posting within the path
    @Override
    public void run() {

        ArrayList<String> stringArrayList = Documenter.loadPostingFile(this.path);
        ArrayList<String> listWithoutEntity = new ArrayList<>();


        Collections.sort(stringArrayList, String.CASE_INSENSITIVE_ORDER);


        for (int i = 0; i < stringArrayList.size(); i++) {
            String s = stringArrayList.get(i);
            if(Character.isLowerCase(s.substring(0, s.indexOf('!')).charAt(0)))
            {
                listWithoutEntity.add(stringArrayList.get(i));
            }
            else if (this.dictionary.containsKey(s.substring(0, s.indexOf('!')))) {
                listWithoutEntity.add(stringArrayList.get(i));
            }
        }


        for (int i = listWithoutEntity.size() - 1; i > 0; i--) {
            String firstRecord = listWithoutEntity.get(i);
            String secondRecord = listWithoutEntity.get(i - 1);
            String firstTerm = firstRecord.substring(0, firstRecord.indexOf("!"));
            String secondTerm = secondRecord.substring(0, secondRecord.indexOf("!"));
            String firstPairs = firstRecord.substring(firstRecord.indexOf("!") + 1);
            String secondPairs = secondRecord.substring(secondRecord.indexOf("!") + 1);
            if (firstTerm.toLowerCase().equals(secondTerm.toLowerCase())) {
                //case at least one is not in upper case
                if (!(Character.isUpperCase(secondTerm.charAt(0)) && Character.isUpperCase(firstTerm.charAt(0)))) {

                    if (Character.isUpperCase(firstTerm.charAt(0))) {
                        if (this.dictionary.containsKey(firstTerm)) {
                            DictionaryEntryTrio newTermTrio;
                            if (this.dictionary.containsKey(secondTerm)) {
                                DictionaryEntryTrio firstTermTrio = this.dictionary.get(firstTerm);
                                DictionaryEntryTrio secondTermTrio = this.dictionary.get(secondTerm);
                                int totalDF = firstTermTrio.getDocumentFrequency() + secondTermTrio.getDocumentFrequency();
                                int totalCF = firstTermTrio.getCumulativeFrequency() + secondTermTrio.getCumulativeFrequency();
                                newTermTrio = new DictionaryEntryTrio(totalDF, totalCF, firstTermTrio.getPostingIndex());
                            } else {
                                newTermTrio = this.dictionary.get(firstTerm);
                            }
                            //delete from dictionary the first term - which is upper case
                            dictionaryMutex.lock();
                            this.dictionary.remove(firstTerm);
                            this.dictionary.put(secondTerm, newTermTrio);
                            dictionaryMutex.unlock();
                        }
                    } else if (Character.isUpperCase(secondTerm.charAt(0))) {
                        if (this.dictionary.containsKey(secondTerm)) {
                            DictionaryEntryTrio newTermTrio;
                            if (this.dictionary.containsKey(firstTerm)) {
                                DictionaryEntryTrio firstTermTrio = this.dictionary.get(firstTerm);
                                DictionaryEntryTrio secondTermTrio = this.dictionary.get(secondTerm);
                                int totalDF = firstTermTrio.getDocumentFrequency() + secondTermTrio.getDocumentFrequency();
                                int totalCF = firstTermTrio.getCumulativeFrequency() + secondTermTrio.getCumulativeFrequency();
                                newTermTrio = new DictionaryEntryTrio(totalDF, totalCF, firstTermTrio.getPostingIndex());
                            } else {
                                newTermTrio = this.dictionary.get(secondTerm);
                            }
                            //delete from dictionary the second term - which is upper case
                            dictionaryMutex.lock();
                            this.dictionary.remove(secondTerm);
                            this.dictionary.put(firstTerm, newTermTrio);
                            dictionaryMutex.unlock();
                        }

                    }

                    secondTerm = secondTerm.toLowerCase() + "!" + secondPairs + firstPairs;
                } else //both in upper case
                {
                    secondTerm = secondTerm + "!" + secondPairs + firstPairs;

                }
                listWithoutEntity.remove(i);
                listWithoutEntity.set(i - 1, secondTerm);
            }
        }
        Documenter.deleteAllFilesFromDirectory(this.path);
        Documenter.saveFinalPostingFile(listWithoutEntity, this.path + "\\posting");
    }
}
