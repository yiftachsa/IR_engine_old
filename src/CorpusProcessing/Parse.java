package CorpusProcessing;


import Model.MyModel;

import java.util.HashMap;
import java.util.Map;

public class Parse {

    //private Map<String , String> dictionary;

    public Parse() {

    }

    public static String[] parseDocument(Document document) {

        String [] tokens = document.getText().split(" ");
        MyModel.addToDictionary("token","posting file number");
        return tokens;


    }
}
