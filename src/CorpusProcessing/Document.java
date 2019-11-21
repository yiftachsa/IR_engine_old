package CorpusProcessing;

public class Document {

    private String id;
    private String header;
    private String text;

    public Document(String documentID, String header, String text) {
        this.id = documentID;
        this.header = header;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
