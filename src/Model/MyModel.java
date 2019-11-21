package Model;
import CorpusProcessing.Document;
import CorpusProcessing.Parse;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

//import IR_engine.CorpusProcessing;

public class MyModel extends Observable implements IModel{

    private boolean stemming;

    private static Map<String , String> dictionary;

    private static int postingCount;


/*  setChanged();
    notifyObservers();*/

    /**
     * Constructor
     */
    public MyModel() {
        stemming=false;
        this.dictionary = new HashMap<String,String>();
        this.postingCount = 0;
    }

    @Override
    public void setStemming(boolean selected) {
        stemming=selected;

    }

    @Override
    public boolean loadDictionary(String path) {
        return false;
    }

    @Override
    public boolean clear(String path) {
        return false;
    }

    @Override
    public boolean getDictionaryStatus() {
        if(dictionary == null)
        {
            return  false;
        }
        else
        {
            return  true;
        }
    }

    @Override
    public Map<String, String> getDictionary() {
        if (dictionary != null){
            return dictionary;
        }
        return null;
    }

    @Override
    public void start(String corpusPath, String resultPath) {

        if(!testPath(corpusPath) || !testPath(resultPath))
        {
            setChanged();
            notifyObservers("Bad input"); //TODO: Maybe replace with enum
        }
        //From now on the paths are assumed to be valid
        File Corpus = new File(corpusPath);
        File[] directories = Corpus.listFiles();
        for (File directory : directories){
            String filePath = directory.listFiles()[0].getAbsolutePath();
            if(Files.isReadable(Paths.get(filePath))){
                ArrayList<Document> documents = CorpusProcessing.ReadFile.separateFileToDocuments(filePath);

                for(Document document : documents){
                    Parse.parseDocument(document);
                }
            }

        }

    }

    private boolean testPath(String corpusPath) {
        boolean isDirectory;
        try{
            File directory = new File(corpusPath);
            isDirectory = directory.isDirectory();

        }catch (Exception e){
            isDirectory = false;
        }
        return  isDirectory;
    }

    public boolean isStemming() {
        return stemming;
    }

    public static boolean doesDictionaryContains(String key){
        return dictionary.containsKey(key);
    }

    public static void addToDictionary(String key, String value){
        dictionary.put(key, value);
    }


/*
    @Override
    public void saveGame(File file)
    {
        byte[] mazeByteArray = maze.toByteArray();
        saveMazeToFile(mazeByteArray, file.getAbsolutePath());
    }
    */
    /*

      Save a Maze to a file.
      @param decompressedMaze - byte[]
      @param mazeFileName - String

    private void saveMazeToFile(byte[] decompressedMaze, String mazeFileName) {
        // save maze to a file
        try {
            OutputStream fileOutputStream = new FileOutputStream(mazeFileName);
            OutputStream myCompressorOutputStream = new MyCompressorOutputStream(fileOutputStream);
            myCompressorOutputStream.write(decompressedMaze);
            fileOutputStream.close();
            myCompressorOutputStream.flush();
            myCompressorOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
   /*
    public void loadGame(File file)
    {
        //read maze from file

        try{
            byte savedMazeBytes[] = new byte[500000];
            InputStream fileInputStream = new FileInputStream( file.getAbsolutePath());
            InputStream myDecompressorInputStream = new MyDecompressorInputStream(fileInputStream);
            myDecompressorInputStream.read(savedMazeBytes);
            maze = new Maze(savedMazeBytes);
            currentCharacterPosition = maze.getStartPosition();
            myDecompressorInputStream.close();
            fileInputStream.close();
            solution=null;
            mazeChanged = true;
            characterChanged = true;
            solutionChanged=false;
            setChanged();
            notifyObservers();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
*/


}
