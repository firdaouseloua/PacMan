package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.actors.*;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

import java.util.List;


public class ClassicBoard extends AbstractBoard {
    /**
     * Return the type of game of the board
     * Depending on the type, the maze or other initializations might be different
     * @return the game type
     */
    @Override
    public GameType getGameType(){
        return GameType.CLASSIC;
    };

    /**
     * Initialization of the board
     * (loads the maze, create and place the actors, etc.)
     * @throws PacManException in case something went wrong
     */
    @Override
    public void initialize() throws PacManException {
        try {
            this.maze = Maze.loadFromFile("resources/maze.txt");
            this.pacMan = new PacMan(this, 112, 211);
            this.pacMan.start();
        } catch (Exception e) {
            throw new PacManException("Error while initializing");
        }
    }

    /**
     * Start the actors
     * Perform all necessary actions to start actors at the beginning of the game
     */
    @Override
    public void startActors() {
        // Initialize PacMan's state (direction, speed, etc.)
        if (this.pacMan != null) {
            this.pacMan.setIntention(null);
        }
    }
}
