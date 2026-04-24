package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.board.Counter;
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
        int level = board.getLevel();
        int limit;
        if (level == 1) {
            limit = 60;
        } else if (level == 2) {
            limit = 50;
        } else {
            limit = 0;
        }
        this.dotCounter = new Counter(limit);
    }

    /**
     * Clyde's target:
     * - PacMan if far away
     * - scatter corner if too close
     */
    @Override
    public TilePosition getTarget() {
        switch (ghostState) {
            case CHASE:
                TilePosition pacTile = getBoard().getPacMan().getCurrentTile();
                TilePosition myTile = getCurrentTile();

                int dLine = myTile.getLine() - pacTile.getLine();
                int dCol = myTile.getCol() - pacTile.getCol();
                int squaredDistance = dLine * dLine + dCol * dCol;

                if (squaredDistance <= 64) {
                    return new TilePosition(34, 0);
                }

                return pacTile;

            case SCATTER:
                return new TilePosition(34, 0);

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
        return new TilePosition(34, 0);
    }
}