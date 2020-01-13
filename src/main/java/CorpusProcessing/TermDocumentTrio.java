package CorpusProcessing;


public class TermDocumentTrio extends Trio {

    private String term;
    private String docid;
    private int frequency;

    /**
     * Constructor
     *
     * @param term      - String
     * @param docId     - String
     * @param frequency - int - term frequency.
     */
    public TermDocumentTrio(String term, String docId, int frequency) {
        this.term = term;
        this.docid = docId;
        this.frequency = frequency;
    }

    /**
     * Returns the term field.
     *
     * @return - String - the term field.
     */
    public String getTerm() {
        return term;
    }

    /**
     * Returns the docID field.
     *
     * @return - String - the docID field.
     */
    public String getDocid() {
        return docid;
    }

    /**
     * Returns the frequency field.
     *
     * @return - int - the frequency field.
     */
    public int getFrequency() {
        return frequency;
    }

    @Override
    public String toString() {
        return "Trio{" +
                "term='" + term + '\'' +
                ", docid='" + docid + '\'' +
                ", frequency=" + frequency +
                '}';
    }


    @Override
    public int compareTo(Trio o) {
        if (this.getTerm().equals(((TermDocumentTrio) o).getTerm())) {
            return this.docid.compareTo(((TermDocumentTrio) o).docid);
        } else {
            return this.term.compareTo(((TermDocumentTrio) o).term);
        }
    }
}

