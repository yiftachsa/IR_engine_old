package CorpusProcessing;

public class Query {

    private int number;
    private String title;
    private String description;

    public Query(int number, String title, String description) {
        this.number = number;
        this.title = title;
        this.description = description;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
