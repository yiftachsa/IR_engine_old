package Model;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import javax.print.DocFlavor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyModel extends Observable implements IModel{

    //Game details
    private Maze maze;
    private Position currentCharacterPosition;
    private Solution solution;

    private Boolean mazeChanged;
    private Boolean characterChanged;
    private Boolean solutionChanged;

    private Boolean characterChangedRight;
    private Boolean characterChangedLeft;


    //Servers
    private Server mazeGeneratingServer;
    private Server solveSearchProblemServer;
    private Boolean solved;


    private ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * Constructor
     */
    public MyModel() {
        mazeChanged = false;
        characterChanged = false;
        solutionChanged = false;
        characterChangedLeft = false;
        characterChangedRight = false;
        solved = false;
    }

    /**
     * Starts the servers.
     */
    public void startServers() {
        mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
        solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
        solveSearchProblemServer.start();
        mazeGeneratingServer.start();
    }

    @Override
    public void shutdown() {
        stopServers();
    }

    /**
     * Stops the servers.
     */
    public void stopServers() {
        mazeGeneratingServer.stop();
        solveSearchProblemServer.stop();
    }

    /**
     * Sets the maze.
     * @param maze - Maze
     */
    public void setMaze(Maze maze) {
        this.maze = maze;
    }

    /**
     * Sets the character position.
     * @param currentCharacterPosition
     */
    public void setCurrentCharacterPosition(Position currentCharacterPosition) {
        this.currentCharacterPosition = currentCharacterPosition;
    }

    @Override
    public void generateMaze(int rows, int columns) {
        solved = false;
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{rows, columns};
                        toServer.writeObject(mazeDimensions);
                        toServer.flush();
                        byte[] compressedMaze = (byte[])((byte[])fromServer.readObject());
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[rows * columns + 24];
                        is.read(decompressedMaze);
                        maze = new Maze(decompressedMaze);

                    } catch (Exception var12) {
                        var12.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
            currentCharacterPosition = maze.getStartPosition();
            characterChanged = true;
            characterChangedRight = true;
            mazeChanged=true;
            solution = null;
            solutionChanged=false;
            setChanged();
            notifyObservers();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean mazeChanged() {
        if(mazeChanged ==true){
            mazeChanged = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean characterPositionChanged() {
        if(characterChanged ==true){
            characterChanged = false;

            return true;
        }
        return false;
    }
    @Override
    public boolean characterPositionChangedRight()
    {
        if(characterChangedRight)
        {
            characterChangedRight=false;
            return true;
        }
        return false;
    }
    @Override
    public boolean characterPositionChangedLeft()
    {
        if(characterChangedLeft)
        {
            characterChangedLeft=false;
            return true;
        }
        return false;
    }

    @Override
    public boolean solutionChanged() {
        if(solutionChanged ==true){
         //   solutionChanged = false;
            return true;
        }
        return false;
    }

    public int[] getMazeDimensions() {
        return new int[]{maze.getRowLength(),maze.getColumnLength()};
    }

    @Override
    public boolean solved() {
        return solved;
    }

    @Override
    public void moveCharacter(int row, int column){
        if(maze.isLegalMove(new Position(row,column))){
            //Left
            if(currentCharacterPosition.getColumnIndex() == column-1)
            {
                characterChangedRight=true;
                characterChangedLeft=false;

            }
            //Right
            else if(currentCharacterPosition.getColumnIndex() == column+1)
            {

                characterChangedRight=false;
                characterChangedLeft=true;
            }
            currentCharacterPosition.setX(row);
            currentCharacterPosition.setY(column);
            characterChanged = true;

            if(solutionChanged)
            {
                solution=null;
                this.getSolution();
            }
            setChanged();
            notifyObservers();
        }
    }



    @Override
    public void moveCharacter(KeyCode movement) {
        Boolean legalMove = false;
        if (currentCharacterPosition!=null) {
            if (!finished()) {
                switch (movement) {
                    case NUMPAD8: { //UP
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex() - 1, currentCharacterPosition.getColumnIndex()))) {
                            currentCharacterPosition.setX(currentCharacterPosition.getRowIndex() - 1);
                            legalMove = true;
                        }
                        break;
                    }
                    case UP: { //UP
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex() - 1, currentCharacterPosition.getColumnIndex()))) {
                            currentCharacterPosition.setX(currentCharacterPosition.getRowIndex() - 1);
                            legalMove = true;
                        }
                        break;
                    }
                    case NUMPAD2: { //DOWN
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex() + 1, currentCharacterPosition.getColumnIndex()))) {
                            currentCharacterPosition.setX(currentCharacterPosition.getRowIndex() + 1);
                            legalMove = true;
                        }
                        break;
                    }
                    case DOWN: { //DOWN
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex() + 1, currentCharacterPosition.getColumnIndex()))) {
                            currentCharacterPosition.setX(currentCharacterPosition.getRowIndex() + 1);
                            legalMove = true;
                        }
                        break;
                    }
                    case NUMPAD6: { //RIGHT
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex(), currentCharacterPosition.getColumnIndex() + 1))) {
                            currentCharacterPosition.setY(currentCharacterPosition.getColumnIndex() + 1);
                            characterChangedRight = true;
                            legalMove = true;
                        }
                        break;
                    }
                    case RIGHT: { //RIGHT
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex(), currentCharacterPosition.getColumnIndex() + 1))) {
                            currentCharacterPosition.setY(currentCharacterPosition.getColumnIndex() + 1);
                            characterChangedRight = true;
                            legalMove = true;
                        }
                        break;
                    }
                    case NUMPAD4: { //LEFT
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex(), currentCharacterPosition.getColumnIndex() - 1))) {
                            currentCharacterPosition.setY(currentCharacterPosition.getColumnIndex() - 1);
                            characterChangedLeft = true;

                            legalMove = true;
                        }
                        break;
                    }
                    case LEFT: { //LEFT
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex(), currentCharacterPosition.getColumnIndex() - 1))) {
                            currentCharacterPosition.setY(currentCharacterPosition.getColumnIndex() - 1);
                            characterChangedLeft = true;

                            legalMove = true;
                        }
                        break;
                    }
                    case NUMPAD7: { //UP-LEFT
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex() - 1, currentCharacterPosition.getColumnIndex() - 1))) {
                            currentCharacterPosition.setX(currentCharacterPosition.getRowIndex() - 1);
                            currentCharacterPosition.setY(currentCharacterPosition.getColumnIndex() - 1);
                            characterChangedLeft = true;

                            legalMove = true;
                        }
                        break;
                    }
                    case NUMPAD9: { //UP-RIGHT
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex() - 1, currentCharacterPosition.getColumnIndex() + 1))) {
                            currentCharacterPosition.setX(currentCharacterPosition.getRowIndex() - 1);
                            currentCharacterPosition.setY(currentCharacterPosition.getColumnIndex() + 1);
                            characterChangedRight = true;

                            legalMove = true;
                        }
                        break;
                    }
                    case NUMPAD1: { //DOWN-LEFT
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex() + 1, currentCharacterPosition.getColumnIndex() - 1))) {
                            currentCharacterPosition.setX(currentCharacterPosition.getRowIndex() + 1);
                            currentCharacterPosition.setY(currentCharacterPosition.getColumnIndex() - 1);
                            characterChangedLeft = true;

                            legalMove = true;
                        }
                        break;
                    }
                    case NUMPAD3: { //DOWN-LEFT
                        if (maze.isLegalMove(new Position(currentCharacterPosition.getRowIndex() + 1, currentCharacterPosition.getColumnIndex() + 1))) {
                            currentCharacterPosition.setX(currentCharacterPosition.getRowIndex() + 1);
                            currentCharacterPosition.setY(currentCharacterPosition.getColumnIndex() + 1);
                            characterChangedLeft = true;
                            legalMove = true;
                        }
                        break;
                    }
                }

                if (legalMove) {

                    if (solutionChanged) {
                        solution = null;
                        this.getSolution();
                    }
                    characterChanged = true;
                    setChanged();
                    notifyObservers();
                }
            }
        }
    }
    @Override
    public void saveGame(File file)
    {
        byte[] mazeByteArray = maze.toByteArray();
        saveMazeToFile(mazeByteArray, file.getAbsolutePath());
    }

    /**
     * Save a Maze to a file.
     * @param decompressedMaze - byte[]
     * @param mazeFileName - String
     */
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
    @Override
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

    /**
     * Checks if the character position equals to the maze goal position.
     * @return - boolean
     */
    public boolean finished(){
        return currentCharacterPosition.equals(maze.getGoalPosition());
    }

    @Override
    public Solution getSolution() {
        if (solution != null)
        {
            return solution;
        }
        solved = true;
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        toServer.writeObject(maze);
                        toServer.flush();
                        solution = (Solution)fromServer.readObject();
                    } catch (Exception var10) {
                        var10.printStackTrace();
                    }
                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException var1) {
            var1.printStackTrace();
        }
        solutionChanged=true;
        setChanged();
        notifyObservers();
        return solution;
    }

    @Override
    public Maze getMaze() {
        return maze;
    }


    @Override
    public Position getCharacterPosition() {
        return currentCharacterPosition;
    }

    @Override
    public boolean CheckInput(String firstInput, String secondInput) {
        if (isValid(firstInput) && isValid(secondInput)){
            System.out.println("Both inputs are valid");
            return true;
        } else {
            System.out.println("Bad Input");
            return false;
        }
    }

    /**
     * Checks if a given input is valid.
     * @param input - String
     * @return - boolean
     */
    private boolean isValid(String input) {
        try {
            int result = Integer.parseInt(input);
            if (result < 5 || result > 1000){
                return false;
            }
        }catch (NumberFormatException exp){
            return false;
        }
        return true;
    }


    @Override
    public boolean isNeighboringPosition(int row, int column)
    {
        //Down
        if(currentCharacterPosition.getRowIndex()==row+1 && currentCharacterPosition.getColumnIndex() == column)
        {
            return true;
        }
        //Right
        else if (currentCharacterPosition.getRowIndex()==row && currentCharacterPosition.getColumnIndex() == column +1)
        {
            return true;
        }
        //Up
        else if (currentCharacterPosition.getRowIndex()==row - 1 && currentCharacterPosition.getColumnIndex() == column)
        {
            return true;
        }
        //Left
        else if (currentCharacterPosition.getRowIndex()==row && currentCharacterPosition.getColumnIndex() == column -1)
        {
            return true;
        }
        //Up-Left
        else if (currentCharacterPosition.getRowIndex()==row-1 && currentCharacterPosition.getColumnIndex() == column -1)
        {
            return true;
        }
        //Up-Right
        else if (currentCharacterPosition.getRowIndex()==row-1 && currentCharacterPosition.getColumnIndex() == column +1)
        {
            return true;
        }
        //Down-Left
        else if (currentCharacterPosition.getRowIndex()==row+1 && currentCharacterPosition.getColumnIndex() == column -1)
        {
            return true;
        }
        //Down-Right
        else if (currentCharacterPosition.getRowIndex()==row+1 && currentCharacterPosition.getColumnIndex() == column +1)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
