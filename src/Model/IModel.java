package Model;

import javafx.scene.input.KeyCode;

import java.io.File;

public interface IModel {
    /**
     * Generate a Maze and sets the maze field.
     * @param rows - int
     * @param columns - int
     */
    void generateMaze(int rows, int columns);

    /**
     * Moves the character to a given row and column.
     * @param row - int
     * @param column - int
     */
    void moveCharacter(int row, int column);

    /**
     * Moves the character based on a given KeyCode.
     * @param movement - KeyCode
     */
    void moveCharacter(KeyCode movement);

    /**
     * returns the maze.
     * @return - Maze
     */
    Maze getMaze();

    /**
     * Returns the current character position.
     * @return - Position
     */
    Position getCharacterPosition();

    /**
     * Receives two inputs and returns True if the inputs are legal.
     * @param text - first input
     * @param text1 - second input
     * @return - boolean
     */
    boolean CheckInput(String text, String text1);

    /**
     * Returns True if the maze changed - for drawing.
     * @return - boolean
     */
    boolean mazeChanged();

    /**
     * Returns True if the character position changed - for drawing.
     * @return - boolean
     */
    boolean characterPositionChanged();

    /**
     * Returns True if the character position changed to right - for drawing.
     * @return - boolean
     */
    boolean characterPositionChangedRight();

    /**
     * Returns True if the character position changed to left - for drawing.
     * @return - boolean
     */
    boolean characterPositionChangedLeft();

    /**
     * Returns True if the solution changed - for drawing.
     * @return - boolean
     */
    boolean solutionChanged();

    /**
     * Returns a solution for the maze.
     * @return - Solution
     */
    Solution getSolution();

    /**
     * Stops the servers.
     */
    void shutdown();

    /**
     * Returns True if the maze was solved already.
     * @return - boolean
     */
    boolean solved();

    /**
     * Returns True if the game was finished.
     * @return - boolean
     */
    boolean finished();

    /**
     * Saves a game to a given file.
     * @param file - File
     */
    void saveGame(File file);

    /**
     * Loads a game from a given file.
     * @param file - File
     */
    void loadGame(File file);

    /**
     * Returns the maze dimensions.
     * @return - int[] - row,column
     */
    int[] getMazeDimensions();

    /**
     * Receives a row and column index and check if they represent
     * a neighboring position to the current character position.
     * @param row - int
     * @param column - int
     * @return - boolean
     */
    boolean isNeighboringPosition(int row, int column);
}
