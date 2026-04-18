package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.actors.*;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

import java.util.List;


public abstract class AbstractBoard implements Board {
    protected GameType gameType;
    protected Maze maze;

    /**
     * Return the type of game of the board
     * Depending on the type, the maze or other initializations might be different
     * @return the game type
     */
    public abstract GameType getGameType();

    /**
     * Initialization of the board
     * (loads the maze, create and place the actors, etc.)
     * @throws PacManException in case something went wrong
     */
    public abstract void initialize() throws PacManException;

    /**
     * Start the actors
     * Perform all necessary actions to start actors at the beginning of the game
     */
    public abstract void startActors();

    /** Return the maze
     * @return the maze
     */
    public abstract Maze getMaze();

    /**
     * Return PacMan
     * @return the PacMan actor
     */
    public abstract Actor getPacMan();

    /**
     * Perform all necessary actions for the next game frame
     * This might require to move the actors,
     * perform some checks, etc.
     */
    public abstract void nextFrame();

    /**
     * Create a board depending on the game type
     * @param type a game type
     * @return the board
     */
    public static Board createBoard(GameType type) {
        switch (type) {
            case CLASSIC:
                return new ClassicBoard();
            case TEST:
                return new TestBoard();
            default:
                throw new IllegalArgumentException("Unknown game type : " + type);
        }
    }
}
