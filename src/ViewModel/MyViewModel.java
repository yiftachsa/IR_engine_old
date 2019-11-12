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

    /**
     * Generates a new maze using the given row and column count
     * @param rows
     * @param columns
     */
    public void generateMaze(int rows, int columns){
        model.generateMaze(rows, columns);
    }

    /**
     * Moves the character based on a given KeyCode.
     * @param movement - KeyCode
     */
    public void moveCharacter(KeyCode movement){
        model.moveCharacter(movement);
    }

    /**
     * Moves the character based to a given row and column indexes.
     * @param row - int
     * @param column - int
     */
    public void moveCharacter(int row, int column){
        model.moveCharacter(row, column);
    }

    /**
     * Returns the current maze.
     * @return - Maze
     */
    public Maze getMaze() {
        return model.getMaze();
    }

    /**
     * Returns the current character position.
     * @return - Position
     */
    public Position getCharacterPosition() {
        return model.getCharacterPosition();
    }

    /**
     * Check if the given inputs are legal.
     * @param firstInput - TextField
     * @param secondInput - TextField
     * @return - boolean
     */
    public boolean CheckInput(TextField firstInput, TextField secondInput) {
        if (firstInput != null && secondInput != null) {
            return model.CheckInput(firstInput.getText(), secondInput.getText());
        }
        return false;
    }


    @Override
    public void update(Observable o, Object arg) {
        if (o==model){
            setChanged();
            notifyObservers();
        }
    }

    /**
     * Returns True if the maze changed - for drawing.
     * @return - boolean
     */
    public boolean mazeChanged() {
        return model.mazeChanged();
    }

    /**
     * Returns True if the character position changed - for drawing.
     * @return - boolean
     */
    public boolean characterPositionChanged() {
        return model.characterPositionChanged();
    }

    /**
     * Returns True if the character position changed to right - for drawing.
     * @return - boolean
     */
    public boolean characterPositionChangedRight() {
        return model.characterPositionChangedRight();
    }

    /**
     * Returns True if the character position changed to left - for drawing.
     * @return - boolean
     */
    public boolean characterPositionChangedLeft() {
        return model.characterPositionChangedLeft();
    }

    /**
     * Returns True if the solution changed - for drawing.
     * @return - boolean
     */
    public boolean solutionChanged() {
        return model.solutionChanged();
    }

    /**
     * Returns a solution for the maze.
     * @return - Solution
     */
    public Solution getSolution() {
        return model.getSolution();
    }

    /**
     * Stops the servers.
     */
    public void shutdown() {
        model.shutdown();
    }

    /**
     * Returns True if the maze was solved already.
     * @return - boolean
     */
    public boolean Solved() {
        return model.solved();
    }

    /**
     * Returns True if the game was finished.
     * @return - boolean
     */
    public Boolean finished() {
        return model.finished();
    }

    /**
     * Saves a game to a given file.
     * @param file - File
     */
    public void saveGame(File file) {
        model.saveGame(file);
    }

    /**
     * Loads a game from a given file.
     * @param file - File
     */
    public void loadGame(File file) {
        model.loadGame(file);
    }

    /**
     * Returns the maze dimensions.
     * @return - int[] - row,column
     */
    public int[] getMazeDimensions() {
        return model.getMazeDimensions();
    }

    /**
     * Receives a row and column index and check if they represent
     * a neighboring position to the current character position.
     * @param row - int
     * @param column - int
     * @return - boolean
     */
    public boolean isNeighborhingPosition(int row, int column)
    {
        return model.isNeighboringPosition(row , column);
    }

}
