package CorpusProcessing;

import java.io.File;
import java.util.HashSet;

public class RunnableParse implements Runnable {

    private HashSet<String> entities;
    private HashSet<String> singleAppearanceEntities;
    private File[] filesToParse;
    private Parse parser = new Parse(entities, singleAppearanceEntities);



    public RunnableParse(HashSet<String> entities, HashSet<String> singleAppearanceEntities) {
        this.entities = entities;
        this.singleAppearanceEntities = singleAppearanceEntities;
    }

    public void setFilesToParse(File[] filesToParse) {
        this.filesToParse = filesToParse;
    }

    @Override
    public void run() {

    }
}
