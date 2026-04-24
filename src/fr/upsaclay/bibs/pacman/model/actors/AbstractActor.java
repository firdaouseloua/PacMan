package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.Tile;

import java.util.Objects;


/**
 * Base implementation shared by all board actors.
 *
 * We introduced this abstract class because several pieces of logic are common
 * to all actors of the game: storing the board, storing the pixel position,
 * retrieving the current tile, keeping track of the current direction and
 * intended direction, and managing the blocked state.
 *
 * This avoids duplicating the same code later in PacMan, ghosts and bonuses.
 */
public abstract class AbstractActor implements Actor {

    /** Board on which the actor evolves. */
    protected final Board board;

    /** Current x coordinate in pixels. */
    protected int x;

    /** Current y coordinate in pixels. */
    protected int y;

    /** Current moving direction. */
    protected Direction direction;

    /** Direction the actor would like to take when possible. */
    protected Direction intention;

    /** True when movement is currently blocked by a wall. */
    protected boolean blocked;

    /** Reserved for step 2. */
    protected double speed;

    /** Reserved for step 2. */
    protected double realX;

    /** Reserved for step 2. */
    protected double realY;

    /** Reserved for step 2. */
    protected int stopTime;

    /**
     * Builds an actor with an initial position and direction.
     *
     * @param board the board containing the actor
     * @param startX initial x position in pixels
     * @param startY initial y position in pixels
     * @param startDirection initial moving direction
     */
    protected AbstractActor(Board board, int startX, int startY, Direction startDirection) {
        this.board = Objects.requireNonNull(board, "board");
        this.x = startX;
        this.y = startY;
        this.realX = startX;
        this.realY = startY;
        this.direction = Objects.requireNonNull(startDirection, "startDirection");
        this.intention = null;
        this.blocked = false;
        this.speed = 1.0;
        this.stopTime = 0;
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        this.realX = x;
        this.realY = y;
    }

    @Override
    public TilePosition getCurrentTile() {
        int centerX = x + (Maze.TILE_WIDTH / 2);
        int centerY = y + (Maze.TILE_WIDTH / 2);
        return getMaze().getTilePosition(centerX, centerY);
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = Objects.requireNonNull(direction, "direction");
    }

    @Override
    public void setIntention(Direction direction) {
        this.intention = direction;
    }

    @Override
    public Direction getIntention() {
        return intention;
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    /**
     * For step 1, a frame simply triggers the next move.
     * Stop management is kept for later steps.
     */
    @Override
    public void nextFrame() {
        if (stopTime > 0) {
            stopTime--;
            return;
        }
        nextMove();
    }

    @Override
    public void setSpeed(double speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("speed must be >= 0");
        }
        this.speed = speed;
    }

    @Override
    public double getSpeed() {
        return speed;
    }

    @Override
    public double getRealX() {
        return realX;
    }

    @Override
    public double getRealY() {
        return realY;
    }

    @Override
    public void setStopTime(int nbFrames) {
        if (nbFrames < 0) {
            throw new IllegalArgumentException("nbFrames must be >= 0");
        }
        this.stopTime = nbFrames;
    }

    /**
     * Convenience accessor to the maze.
     * This keeps all board-to-maze access centralized in one place.
     */
    protected Maze getMaze() {
        return board.getMaze();
    }

    /**
     * Returns true when the actor is exactly at the center pixel of its tile.
     * In step 1, turning is only checked there.
     */
    protected boolean isAtTileCenter() {
        return x % Maze.TILE_WIDTH == Maze.TITLE_CENTER_X
                && y % Maze.TILE_HEIGHT == Maze.TITLE_CENTER_Y;
    }

    /**
     * Returns the local x position inside the current tile, using real coordinates.
     */
    protected double getLocalRealX() {
        double value = realX % Maze.TILE_WIDTH;
        if (value < 0) value += Maze.TILE_WIDTH;
        return value;
    }

    /**
     * Returns the local y position inside the current tile, using real coordinates.
     */
    protected double getLocalRealY() {
        double value = realY % Maze.TILE_HEIGHT;
        if (value < 0) value += Maze.TILE_HEIGHT;
        return value;
    }

    /**
     * Returns true when the actor has crossed the tile center during this frame.
     * This is needed for speeds > 1.
     */
    protected boolean hasCrossedCenter(Direction dir, double oldRealX, double oldRealY) {
        double oldLocalX = oldRealX % Maze.TILE_WIDTH;
        double oldLocalY = oldRealY % Maze.TILE_HEIGHT;

        if (oldLocalX < 0) oldLocalX += Maze.TILE_WIDTH;
        if (oldLocalY < 0) oldLocalY += Maze.TILE_HEIGHT;

        double newLocalX = getLocalRealX();
        double newLocalY = getLocalRealY();

        return switch (dir) {
            case LEFT -> oldLocalX > Maze.TITLE_CENTER_X && newLocalX < Maze.TITLE_CENTER_X;
            case RIGHT -> oldLocalX < Maze.TITLE_CENTER_X && newLocalX > Maze.TITLE_CENTER_X;
            case UP -> oldLocalY > Maze.TITLE_CENTER_Y && newLocalY < Maze.TITLE_CENTER_Y;
            case DOWN -> oldLocalY < Maze.TITLE_CENTER_Y && newLocalY > Maze.TITLE_CENTER_Y;
        };
    }

    /**
     * Snaps the actor to the exact center line after crossing a center with speed > 1.
     */
    protected void snapToTileCenter(Direction dir, double oldRealX, double oldRealY) {
        int currentCol = getCurrentTile().getCol();
        int currentLine = getCurrentTile().getLine();

        if (dir == Direction.LEFT || dir == Direction.RIGHT) {
            realX = currentCol * Maze.TILE_WIDTH + Maze.TITLE_CENTER_X;
            realY = currentLine * Maze.TILE_HEIGHT + Maze.TITLE_CENTER_Y;
        } else {
            realX = currentCol * Maze.TILE_WIDTH + Maze.TITLE_CENTER_X;
            realY = currentLine * Maze.TILE_HEIGHT + Maze.TITLE_CENTER_Y;
        }

        x = (int) realX;
        y = (int) realY;
    }

    /**
     * Returns true when the actor has not yet reached the center of the tile,
     * relative to its current moving direction.
     */
    protected boolean isBeforeCenter(Direction dir) {
        int localX = x % Maze.TILE_WIDTH;
        int localY = y % Maze.TILE_HEIGHT;

        return switch (dir) {
            case LEFT -> localX > Maze.TITLE_CENTER_X;
            case RIGHT -> localX < Maze.TITLE_CENTER_X;
            case UP -> localY > Maze.TITLE_CENTER_Y;
            case DOWN -> localY < Maze.TITLE_CENTER_Y;
        };
    }

    /**
     * Returns true when the actor has already passed the center of the tile,
     * relative to its current moving direction.
     */
    protected boolean isAfterCenter(Direction dir) {
        int localX = x % Maze.TILE_WIDTH;
        int localY = y % Maze.TILE_HEIGHT;

        return switch (dir) {
            case LEFT -> localX < Maze.TITLE_CENTER_X;
            case RIGHT -> localX > Maze.TITLE_CENTER_X;
            case UP -> localY < Maze.TITLE_CENTER_Y;
            case DOWN -> localY > Maze.TITLE_CENTER_Y;
        };
    }

    /**
     * Checks whether the neighbouring tile in the given direction is traversable.
     */
    protected boolean canMove(Direction dir) {
        Tile nextTile = getMaze().getNeighbourTile(getCurrentTile(), dir);
        return !nextTile.isWall();
    }

    /**
     * Moves the actor according to its current speed.
     *
     * Step 2 rule:
     * - the real position is stored as a double
     * - the displayed integer position is the truncated value
     *
     * Example: if realX = 34.5 then x = 34
     */
    protected void moveOnePixel(Direction dir) {
        double oldRealX = realX;
        double oldRealY = realY;

        realX += dir.getDx() * speed;
        realY += dir.getDy() * speed;

        double pixelWidth = getMaze().getPixelWidth();

        /*
        * Horizontal wrap-around must be applied on the real position too.
        */
        if (realX < 0) {
            realX = pixelWidth - 1;
        } else if (realX >= pixelWidth) {
            realX = 0;
        }

        /*
        * If speed is greater than 1, the actor may jump over the center.
        * We snap to the exact center when that happens.
        */
        if (speed > 1.0 && hasCrossedCenter(dir, oldRealX, oldRealY)) {
            snapToTileCenter(dir, oldRealX, oldRealY);
            return;
        }

        x = (int) realX;
        y = (int) realY;
    }

    /**
     * Tries to continue in the current direction.
     *
     * @return true if the move succeeded
     */
    protected boolean tryMoveForward() {
        if (canMove(direction) || !isAtTileCenter()) {
            moveOnePixel(direction);
            blocked = false;
            return true;
        }

        blocked = true;
        return false;
        }
}
