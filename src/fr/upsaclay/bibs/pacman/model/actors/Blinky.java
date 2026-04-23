package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

/**
 * Blinky:
 * Blinky targets PacMan directly in CHASE mode.
 * In SCATTER mode, he targets the top-right corner.
 */
public class Blinky extends AbstractGhost {

    /**
     * Creates Blinky with the step-2 initial position.
     */
    public Blinky(Board board, int startX, int startY) {
        super(board, startX, startY, Direction.LEFT, GhostType.BLINKY);
    }

    /**
     * Blinky target = PacMan current tile.
     */
    @Override
    public TilePosition getTarget() {
        return getBoard().getPacMan().getCurrentTile();
    }

    @Override
    protected TilePosition getScatterTarget() {
        return new TilePosition(0, getMaze().getWidth() - 1);
    }
}