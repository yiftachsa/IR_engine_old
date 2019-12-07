package CorpusProcessing;

import java.io.Serializable;

public class Trio implements Comparable<Trio> , Serializable {

    private String term;
    private String docid;
    private int frequency;

    public Trio(String term, String docId, int frequency) {
        this.term = term;
        this.docid = docId;
        this.frequency = frequency;
    }

    public String getTerm() {
        return term;
    }

    public void incrementFrequency(){
        frequency++;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDocid() {
        return docid;
    }

    public void setDocid(String docid) {
        this.docid = docid;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
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
        if(this.getTerm().equals(o.getTerm()))
        {
            return this.docid.compareTo(o.docid);
        }
        else
        {
            return this.term.compareTo(o.term);
        }
    }
}

