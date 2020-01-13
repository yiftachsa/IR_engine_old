package CorpusProcessing;

public class DictionaryEntryTrio extends Trio{

    private int documentFrequency;
    private int cumulativeFrequency;
    private String postingIndex;

    /**
     * Constructor
     * @param documentFrequency
     * @param cumulativeFrequency
     * @param postingIndex
     */
    public DictionaryEntryTrio(int documentFrequency, int cumulativeFrequency, String postingIndex) {
        this.documentFrequency = documentFrequency;
        this.cumulativeFrequency = cumulativeFrequency;
        this.postingIndex = postingIndex;
    }

    /**
     * Returns the documentFrequency field
     * @return - int - the documentFrequency field
     */
    public int getDocumentFrequency() {
        return documentFrequency;
    }

    /**
     * Returns the cumulativeFrequency field
     * @return - int - the cumulativeFrequency field
     */
    public int getCumulativeFrequency() {
        return cumulativeFrequency;
    }

    /**
     * Returns the postingIndex field
     * @return - String - the postingIndex field
     */
    public String getPostingIndex() {
        return postingIndex;
    }

    @Override
    public int compareTo(Trio o) {
        return this.compareTo(o);
    }
}
