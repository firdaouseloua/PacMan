package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.board.Counter;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

/**
 * Inky ghost.
 *
 * Inky's classical target depends on both PacMan and Blinky:
 * 1. take the tile 2 tiles ahead of PacMan
 * 2. build the vector from Blinky to that tile
 * 3. double that vector
 */
public class Inky extends AbstractGhost {

    /**
     * Creates Inky at its initial position inside the pen.
     */
    public Inky(Board board, int startX, int startY) {
        super(board, startX, startY, Direction.LEFT, GhostType.INKY);
        int limit = (board.getLevel() == 1) ? 30 : 0;
        this.dotCounter = new Counter(limit);
    }

    /**
     * Inky's target:
     * computed from PacMan position/direction and Blinky position.
     */
    @Override
    public TilePosition getTarget() {
        switch (ghostState) {
            case CHASE:
                TilePosition pacTile = getBoard().getPacMan().getCurrentTile();
                Direction pacDir = getBoard().getPacMan().getDirection();

                int intermediateLine = pacTile.getLine();
                int intermediateCol = pacTile.getCol();

                if (pacDir == Direction.UP) {
                    intermediateLine -= 2;
                    intermediateCol -= 2;
                } else {
                    intermediateLine += 2 * pacDir.getDy();
                    intermediateCol += 2 * pacDir.getDx();
                }

                Ghost blinky = getBoard().getGhost(GhostType.BLINKY);
                if (blinky == null) {
                    return new TilePosition(intermediateLine, intermediateCol);
                }

                TilePosition blinkyTile = blinky.getCurrentTile();

                int targetLine = intermediateLine + (intermediateLine - blinkyTile.getLine());
                int targetCol = intermediateCol + (intermediateCol - blinkyTile.getCol());

                return new TilePosition(targetLine, targetCol);

            case SCATTER:
                return new TilePosition(34, 27);

            case FRIGHTENED:
            case FRIGHTENED_END:
                return null;

            case DEAD:
                return getBoard().penEntry();

            default:
                return null;
        }
    }

    @Override
    protected TilePosition getScatterTarget() {
        return new TilePosition(34, 27);
    }
}