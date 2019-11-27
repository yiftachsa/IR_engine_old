package Model;
import CorpusProcessing.Document;
import CorpusProcessing.Indexer;
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

    private Indexer indexer;

/*  setChanged();
    notifyObservers();*/

    /**
     * Constructor
     */
    public MyModel() {
        stemming=false;
        indexer = new Indexer();
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
        return indexer.getDictionaryStatus();
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
                    ArrayList<String> bagOfWords = Parse.parseDocument(document, stemming);


                }
            }

        }

    }

    /**
     * Verifies that the path is of a reachable directory.
     * @param folderPath - String - an absolute path
     * @return - boolean - true if the solderPath is of a reachable directory, else false
     */
    private boolean testPath(String folderPath) {
        boolean isDirectory;
        try{
            File directory = new File(folderPath);
            isDirectory = directory.isDirectory();

        }catch (Exception e){
            isDirectory = false;
        }
        return  isDirectory;
    }


    public boolean isStemming() {
        return stemming;
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
