package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

/**
 * Clyde ghost.
 *
 * Clyde alternates between two target styles:
 * - if he is far from PacMan, he targets PacMan directly
 * - if he is close to PacMan, he switches to his scatter corner
 *
 * The usual threshold is distance < 8 tiles.
 */
public class Clyde extends AbstractGhost {

    /**
     * Creates Clyde at its initial position inside the pen.
     */
    public Clyde(Board board, int startX, int startY) {
        super(board, startX, startY, Direction.LEFT, GhostType.CLYDE);
    }

    /**
     * Clyde's target:
     * - PacMan if far away
     * - scatter corner if too close
     */
    @Override
    public TilePosition getTarget() {
        TilePosition pacTile = getBoard().getPacMan().getCurrentTile();
        TilePosition myTile = getCurrentTile();

        int dLine = myTile.getLine() - pacTile.getLine();
        int dCol = myTile.getCol() - pacTile.getCol();
        double squaredDistance = dLine * dLine + dCol * dCol;

        /*
         * Threshold: 8 tiles.
         * Using squared distance avoids sqrt: 8^2 = 64
         */
        if (squaredDistance < 64) {
            return getScatterTarget();
        }

        return pacTile;
    }

    /**
     * Clyde's scatter target is usually the bottom-left corner.
     */
    @Override
    protected TilePosition getScatterTarget() {
        return new TilePosition(getMaze().getHeight() - 1, 0);
    }
}