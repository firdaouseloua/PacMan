package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.board.Counter;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

/**
 * Pinky ghost.
 *
 * In the original Pac-Man logic, Pinky targets a tile in front of PacMan.
 * For step 3, this class uses the standard target rule:
 * 4 tiles ahead of PacMan in PacMan's current direction.
 */
public class Pinky extends AbstractGhost {
    
    /**
     * Creates Pinky at its initial position inside the pen.
     */
    public Pinky(Board board, int startX, int startY) {
        super(board, startX, startY, Direction.LEFT, GhostType.PINKY);
        this.dotCounter = new Counter(0);
    }

    /**
     * Pinky's target:
     * 4 tiles ahead of PacMan in PacMan's current direction.
     */
    @Override
    public TilePosition getTarget() {
        switch (ghostState) {
            case CHASE:
                TilePosition pacTile = getBoard().getPacMan().getCurrentTile();
                Direction pacDir = getBoard().getPacMan().getDirection();

                int line = pacTile.getLine();
                int col = pacTile.getCol();

                if (pacDir == Direction.UP) {
                    line -= 4;
                    col -= 4;
                } else {
                    line += 4 * pacDir.getDy();
                    col += 4 * pacDir.getDx();
                }

                return new TilePosition(line, col);

            case SCATTER:
                return new TilePosition(0, 2);

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
        return new TilePosition(0, 2);
    }
}
