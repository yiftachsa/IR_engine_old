package ViewModel;

import Model.IModel;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

public class MyViewModel extends Observable implements Observer {

    private IModel model;

    /**
     * Constructor
     * @param model- IModel
     */
    public MyViewModel(IModel model) {
        this.model = model;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o==model){
            setChanged();
            notifyObservers();
        }
    }

    public void stemmingSelection(boolean selected) {
        model.setStemming(selected);
    }

    public boolean loadDictionary(String path) {
        //TODO: load dictionary from path to object
        //TODO: check stemming!!!!
        return model.loadDictionary(path);

    }

    public boolean clear(String path) {
        //TODO: clear the memory,posting,dictionary!
        //TODO"check if its already clear!
        return model.clear(path);
    }

    public boolean getDictionaryStatus() {

        return model.getDictionaryStatus();
    }

    public String getDictionary() {

        return (String)model.getDictionary();
    }
}
