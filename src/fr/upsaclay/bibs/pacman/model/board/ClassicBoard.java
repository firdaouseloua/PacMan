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

            this.ghosts.clear();
            this.boardState = BoardState.INITIAL;

            if (!disabledGhosts.contains(GhostType.BLINKY)) {
                Blinky blinky = new Blinky(this, 112, 115);
                blinky.setGhostState(GhostState.SCATTER);
                blinky.setOutOfPenDirection(Direction.LEFT);
                blinky.setGhostPenState(GhostPenState.OUT);
                blinky.setSpeed(getLevelGhostSpeed());
                ghosts.add(blinky);
            }

            if (!disabledGhosts.contains(GhostType.PINKY)) {
                Pinky pinky = new Pinky(this, 112, 139);
                pinky.setGhostState(GhostState.SCATTER);
                pinky.setGhostPenState(GhostPenState.IN);
                pinky.setOutOfPenDirection(Direction.LEFT);
                pinky.setSpeed(getTunnelGhostSpeed());
                ghosts.add(pinky);
            }

            if (!disabledGhosts.contains(GhostType.INKY)) {
                Inky inky = new Inky(this, 96, 139);
                inky.setGhostState(GhostState.SCATTER);
                inky.setGhostPenState(GhostPenState.IN);
                inky.setOutOfPenDirection(Direction.LEFT);
                inky.setSpeed(getTunnelGhostSpeed());
                ghosts.add(inky);
            }

            if (!disabledGhosts.contains(GhostType.CLYDE)) {
                Clyde clyde = new Clyde(this, 128, 139);
                clyde.setGhostState(GhostState.SCATTER);
                clyde.setGhostPenState(GhostPenState.IN);
                clyde.setOutOfPenDirection(Direction.LEFT);
                clyde.setSpeed(getTunnelGhostSpeed());
                ghosts.add(clyde);
            }

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
