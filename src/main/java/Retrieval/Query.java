package Retrieval;

/**
 * Represents a query.
 */
public class Query {

    private int number;
    private String title;
    private String description;

    /**
     * Constructor
     *
     * @param number      - int - query number.
     * @param title       - String - The query title, the main part of the query.
     * @param description - String - The query description.
     */
    public Query(int number, String title, String description) {
        this.number = number;
        this.title = title;
        this.description = description;
    }

    /**
     * Setter for the number.
     *
     * @param number - int - query number.
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Setter for the title.
     *
     * @param title - String - The query title, the main part of the query.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Setter for the description.
     *
     * @param description - String - The query description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for the number.
     *
     * @return - int - query number.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Getter for the title.
     *
     * @return - String - The query title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the description.
     *
     * @return - String - The query description.
     */
    public String getDescription() {
        return description;
    }
}
