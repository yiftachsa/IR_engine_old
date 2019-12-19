package  CorpusProcessing;

public class Document {

    private String id;
    private String header;
    private String text;
    private String date;

    /**
     * Constructor
     * @param documentID - String - DocNO {Primary Key}
     * @param header - String - The header of the document
     * @param text - String - The text in the body of the document
     */
    public Document(String documentID, String header,String date, String text) {
        this.id = documentID;
        this.header = header;
        this.text = text;
        this.date = date;
    }

    /**
     * Getter for the id
     * @return - String - DocNO {Primary Key}
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for the id
     * @param id - String - DocNO {Primary Key}
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for the header
     * @return - String - The header of the document
     */
    public String getHeader() {
        return header;
    }

    /**
     * Setter for the header
     * @param header - String - The header of the document
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Getter for the text
     * @return - String - The text in the body of the document
     */
    public String getText() {
        return text;
    }

    /**
     * Getter for the date
     * @return - String - the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Setter for the text
     * @param text - String - The text in the body of the document
     */
    public void setText(String text) {
        this.text = text;
    }
}
