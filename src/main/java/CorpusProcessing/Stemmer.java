package CorpusProcessing;

/**
 * Applies stemming using Porter's stemmer.
 */
public class Stemmer {

    /**
     * Receives a lower case string and stems it.
     *
     * @param term - String - a lower case word.
     * @return - String - a stemmed word.
     */
    public static String stem(String term) {
        org.tartarus.martin.Stemmer porterStemmer = new org.tartarus.martin.Stemmer();
        porterStemmer.add(term.toCharArray(), term.length());
        porterStemmer.stem();
        return porterStemmer.toString();
    }
}
