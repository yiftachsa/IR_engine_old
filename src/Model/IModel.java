package Model;

import java.util.Map;

public interface IModel {

    void setStemming(boolean selected);

    boolean loadDictionary(String path);

    boolean clear(String path);

    boolean getDictionaryStatus();

    void start(String corpusPath, String resultPath);
}
