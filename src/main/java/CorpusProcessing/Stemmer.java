package CorpusProcessing;

import org.tartarus.martin.*;

public class Stemmer {

    public static String stem( String term )
    {
        org.tartarus.martin.Stemmer porterStemmer = new org.tartarus.martin.Stemmer();
        porterStemmer.add(term.toCharArray() , term.length());
        porterStemmer.stem();
        return porterStemmer.toString();
    }
}
