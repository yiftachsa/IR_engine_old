package CorpusProcessing;

import java.io.FileInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class CallableRead implements Callable<ArrayList<Trio>> {

    private  static AtomicInteger indexDoc = new AtomicInteger(0);
    private final int numberOfLines = Documenter.getNUMBEROFPOSTINGLINES();
    private static int iterationNumber = Documenter.getIterationNumber(); //Fixme


    public static void setIterationNumber(int iterationNumber) {
        iterationNumber = iterationNumber;
    }

    public static AtomicInteger getIndexDoc() {
        return indexDoc;
    }

    @Override
    public ArrayList<Trio> call() throws Exception {

        int docIndex = indexDoc.getAndIncrement();
        FileInputStream fileInputStream = new FileInputStream( Documenter.getFilePathToPostingEntries()+"\\"+docIndex);
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        ArrayList<Trio> trioArrayList = (ArrayList<Trio>) objectInputStream.readObject();
        if(numberOfLines*(iterationNumber+1) < trioArrayList.size()) {
            trioArrayList.subList(numberOfLines * iterationNumber, numberOfLines * (iterationNumber + 1));
        }
        else if(numberOfLines * iterationNumber < trioArrayList.size())
        {
            trioArrayList.subList(numberOfLines * iterationNumber, trioArrayList.size()-1);
        }
        else
        {
            trioArrayList = new ArrayList<>();
        }
        return trioArrayList;
    }
}
