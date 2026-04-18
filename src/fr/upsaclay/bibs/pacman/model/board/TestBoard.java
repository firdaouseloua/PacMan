package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.actors.PacMan;
import fr.upsaclay.bibs.pacman.model.maze.Maze;

public class TestBoard extends AbstractBoard {
    /**
     * Return the type of game of the board
     * Depending on the type, the maze or other initializations might be different
     * @return the game type
     */
    @Override
    public GameType getGameType(){
        return GameType.TEST;
    };

    /**
     * Initialization of the board
     * (loads the maze, create and place the actors, etc.)
     * @throws PacManException in case something went wrong
     */
    @Override
    public void initialize() throws PacManException {
        try {
            this.maze = Maze.loadFromFile("resources/test.txt");
            this.pacMan = new PacMan(this, 35, 75);
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
