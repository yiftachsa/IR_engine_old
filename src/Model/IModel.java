package Model;

public interface IModel {

    void setStemming(boolean selected);

    boolean loadDictionary(String path);

    boolean clear(String path);

    boolean getDictionaryStatus();

    Object getDictionary();
}
