package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

/**
 * Step 2 Blinky:
 * its target is always PacMan current tile.
 */
public class Blinky extends AbstractGhost {

    /**
     * Creates Blinky with the step-2 initial position.
     */
    public Blinky(Board board, int startX, int startY) {
        super(board, startX, startY, Direction.LEFT, GhostType.BLINKY);
        this.speed = 0.94;
    }

    /**
     * Blinky target = PacMan current tile.
     */
    @Override
    public TilePosition getTarget() {
        return getBoard().getPacMan().getCurrentTile();
    }
}