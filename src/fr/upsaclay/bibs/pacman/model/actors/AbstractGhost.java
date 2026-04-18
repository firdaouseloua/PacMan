package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.board.Counter;
import fr.upsaclay.bibs.pacman.model.maze.Tile;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

/**
 * Abstract base class shared by all ghosts.
 *
 * Why this class exists:
 * - all ghosts are actors, so they share board, position, direction, speed, etc.
 * - all ghosts also share the same general movement mechanism:
 *   they move according to a target tile and choose directions accordingly
 *
 * In step 2, only Blinky is implemented, but it is still useful to write the
 * common ghost logic here because later ghosts will reuse the same structure.
 *
 * Step 2 simplified ghost behavior:
 * - a ghost has a current direction
 * - it also has an "intention", which is the next direction it plans to take
 * - when the ghost reaches the center of a tile:
 *      1. it applies its current intention as its new direction
 *      2. it computes a new intention for the next tile center
 * - the next intention is chosen by looking for the direction that gets the
 *   ghost closer to its target tile
 * - the ghost is not allowed to reverse direction, unless there is no other choice
 * - on tiles of type NT / ND, the ghost is not allowed to turn UP
 */
public abstract class AbstractGhost extends AbstractActor implements Ghost {

    /** Concrete type of the ghost (BLINKY, PINKY, etc.). */
    protected final GhostType ghostType;

    /**
     * Builds a ghost with a given board, start position, start direction and type.
     *
     * @param board the board on which the ghost is placed
     * @param startX initial x coordinate in pixels
     * @param startY initial y coordinate in pixels
     * @param startDirection initial direction
     * @param ghostType the concrete ghost type
     */
    protected AbstractGhost(Board board, int startX, int startY,
                            Direction startDirection, GhostType ghostType) {
        super(board, startX, startY, startDirection);
        this.ghostType = ghostType;
    }

    /**
     * All ghosts have actor type GHOST.
     *
     * If your ActorType enum uses another constant name, adapt this line.
     */
    @Override
    public ActorType getType() {
        return ActorType.GHOST;
    }

    /**
     * Returns the concrete ghost type.
     */
    @Override
    public GhostType getGhostType() {
        return ghostType;
    }

    /**
     * Initializes ghost state at game start.
     *
     * Why we reset these fields:
     * - intention must be recomputed cleanly from the initial position
     * - blocked should not carry over from previous runs
     * - stopTime should be reset
     * - realX / realY should match the integer coordinates
     *
     * We also initialize the first intention immediately so that tests checking
     * the ghost planned direction can succeed.
     */
    @Override
    public void start() {
        this.blocked = false;
        this.stopTime = 0;
        this.realX = x;
        this.realY = y;

        /*
         * At game start, the ghost already has a current direction,
         * but we also want it to know what its next intended direction is.
         *
         * This is important because some step-2 tests expect the ghost intention
         * to be non-null after movement.
         */
        this.intention = computeNextIntention();
    }

    /**
     * Performs one ghost move.
     *
     * Step 2 ghost movement logic:
     *
     * 1. If the ghost is exactly at the center of a tile:
     *    - if it already has an intention, that intention becomes the new direction
     *    - then it computes a new intention for the next tile center
     *
     * 2. Then it advances according to its current direction.
     *
     * Why this order:
     * - it matches the project statement:
     *   "when a ghost reaches the center of a tile:
     *    it applies its intention and then computes a new one"
     * - this is also the behavior expected by the tests
     */
    @Override
    public void nextMove() {
        if (isAtTileCenter()) {
            /*
             * If an intention was already computed previously,
             * it is now applied as the new current direction.
             */
            if (intention != null) {
                direction = intention;
            }

            /*
             * Once the direction is updated at the current center,
             * we immediately compute the next intended direction
             * for the next center the ghost will reach.
             */
            intention = computeNextIntention();
        }

        /*
         * Then the ghost performs its actual move.
         * The inherited method tryMoveForward() already handles:
         * - continuing forward when possible
         * - blocked state if movement is impossible at tile center
         */
        tryMoveForward();
    }

    protected Direction computeNextIntention() {
    /*
     * Step 2 ghost rule:
     * once the ghost reaches the center of a tile and applies its current intention,
     * it must compute the NEXT intention by looking from the NEXT tile,
     * not from the current tile.
     *
     * In other words:
     * - current tile = where the ghost is now
     * - direction = direction it will now follow
     * - next tile = tile reached if it keeps moving in that direction
     * - from that next tile, we decide what its next intention should be
     */

    TilePosition current = getCurrentTile();

    /*
     * We first determine the tile the ghost is heading to next,
     * using its current direction.
     */
    TilePosition nextTile = getMaze().getNeighbourTilePosition(current, direction);

    /*
     * The target depends on the concrete ghost.
     * For Blinky, target = PacMan tile.
     */
    TilePosition target = getTarget();

    /*
     * The tile from which the next choice is made is the NEXT tile,
     * because that is where the ghost will be at the next tile center.
     */
    Tile nextTileType = getMaze().getTile(nextTile);

    /*
     * Standard deterministic tie-break order.
     */
    Direction[] priorityOrder = {
        Direction.UP,
        Direction.LEFT,
        Direction.DOWN,
        Direction.RIGHT
    };

    Direction bestDirection = null;
    double bestDistance = Double.POSITIVE_INFINITY;

    for (Direction dir : priorityOrder) {

        /*
         * The ghost is not allowed to reverse relative to the direction
         * it is currently following.
         */
        if (dir == direction.reverse()) {
            continue;
        }

        /*
         * If the NEXT tile forbids upward turns for ghosts,
         * then UP cannot be chosen there.
         */
        if (dir == Direction.UP && !nextTileType.ghostCanGoUp()) {
            continue;
        }

        /*
         * We must test movement possibilities FROM the next tile,
         * not from the current one.
         */
        TilePosition candidateTile = getMaze().getNeighbourTilePosition(nextTile, dir);
        Tile candidateType = getMaze().getTile(candidateTile);

        if (candidateType.isWall()) {
            continue;
        }

        /*
         * We measure the distance from the candidate tile to the target.
         */
        double distance = squaredDistance(candidateTile, target);

        if (distance < bestDistance) {
            bestDistance = distance;
            bestDirection = dir;
        }
    }

    /*
     * If we found a valid next intention, use it.
     */
    if (bestDirection != null) {
        return bestDirection;
    }

    /*
     * Fallback: if no other choice exists, keep going in the same direction.
     * This is safer here than returning null.
     */
    return direction;
}

    /**
     * Computes the squared Euclidean distance between two tile positions.
     *
     * Why squared distance instead of the usual Euclidean distance:
     * - sqrt is unnecessary if we only want to compare distances
     * - the ordering is the same
     * - this is simpler and slightly more efficient
     *
     * @param a first tile position
     * @param b second tile position
     * @return squared Euclidean distance
     */
    protected double squaredDistance(TilePosition a, TilePosition b) {
        int dCol = a.getCol() - b.getCol();
        int dLine = a.getLine() - b.getLine();
        return dCol * dCol + dLine * dLine;
    }

    /*********************************************************************/
    /**                    Step 3 / Step 4 stubs                        **/
    /**      These methods are not needed for step 2, so they stay      **/
    /**      simple placeholders for now.                               **/
    /*********************************************************************/

    @Override
    public void setGhostState(GhostState state) {
        // Not used in step 2
    }

    @Override
    public void changeGhostState(GhostState state) {
        // Not used in step 2
    }

    @Override
    public GhostState getGhostState() {
        return null;
    }

    @Override
    public void setGhostPenState(GhostPenState state) {
        // Not used in step 2
    }

    @Override
    public GhostPenState getGhostPenState() {
        return null;
    }

    @Override
    public void setOutOfPenDirection(Direction dir) {
        // Not used in step 2
    }

    @Override
    public Direction getOutOfPenDirection() {
        return null;
    }

    @Override
    public void reverseDirectionIntention() {
        this.intention = direction.reverse();
    }

    @Override
    public boolean hasDotCounter() {
        return false;
    }

    @Override
    public Counter getDotCounter() {
        return null;
    }

    @Override
    public void setElroy(int elroy) {
        // Not used in step 4 yet
    }

    @Override
    public int getElroy() {
        return 0;
    }
}