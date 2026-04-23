package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.actors.*;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

import java.util.List;

/**
 * Classic game board.
 *
 * Step 2 content:
 * - classic maze
 * - PacMan
 * - one ghost: Blinky
 */
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
     * Initializes the classic board.
     *
     * Why we reset everything here:
     * - score must start at 0
     * - state must start at INITIAL
     * - ghost list must be recreated cleanly
     */
    @Override
    public void initialize() throws PacManException {
        try {
            this.maze = Maze.loadFromFile("resources/maze.txt");
            // Initial PacMan position from the statement/tests
            this.pacMan = new PacMan(this, 112, 211);

            // Reset score/state/ghost list for a fresh game
            this.score = 0;
            this.boardState = BoardState.INITIAL;
            this.ghosts.clear();

            // Step 2: only Blinky is present
            // Initial position from the statement:
            // x = 112, y = 115, direction LEFT, speed 0.94
            this.ghosts.add(new Blinky(this, 112, 115));
            this.ghosts.add(new Pinky(this, 112, 139));
            this.ghosts.add(new Inky(this, 96, 139));
            this.ghosts.add(new Clyde(this, 128, 139));

        } catch (Exception e) {
            throw new PacManException("Error while initializing classic board");
        }
    }

    /**
     * Start the actors
     * Perform all necessary actions to start actors at the beginning of the game
     */
    @Override
    public void startActors() {
        super.startActors();
    }
}
