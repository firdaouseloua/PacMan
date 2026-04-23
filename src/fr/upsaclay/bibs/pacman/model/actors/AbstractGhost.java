package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.board.Counter;
import fr.upsaclay.bibs.pacman.model.maze.Tile;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base class shared by all ghosts.
 *
 * Why this class exists:
 * - all ghosts are actors, so they share board, position, direction, speed, etc.
 * - all ghosts also share the same general movement mechanism based on Tile.
 */
public abstract class AbstractGhost extends AbstractActor implements Ghost {

    /** Concrete type of the ghost (BLINKY, PINKY, etc.). */
    protected final GhostType ghostType;
    protected GhostState ghostState;
    protected GhostPenState ghostPenState;
    protected Direction outOfPenDirection;
    protected int elroy;

    /**
     * Builds a ghost with a given board, start position, start direction and type.
     *
     * @param board the board on which the ghost is placed
     * @param startX initial x coordinate in pixels
     * @param startY initial y coordinate in pixels
     * @param startDirection initial direction
     * @param ghostType the concrete ghost type
     */
    protected AbstractGhost(Board board, int startX, int startY, Direction startDirection, GhostType ghostType) {
        super(board, startX, startY, startDirection);
        this.ghostType = ghostType;
        this.ghostState = GhostState.CHASE;
        this.ghostPenState = GhostPenState.OUT;
        this.outOfPenDirection = Direction.LEFT;
        this.elroy = 0;
    }

    /**
     * All ghosts have actor type GHOST.
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
     * We reset:
     * - blocked state
     * - stop time
     * - real coordinates
     * - first intention
     *
     * The initial ghost state remains whatever was assigned by the board.
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
         */
        this.intention = computeNextIntention();
    }

    /**
     * Performs one ghost move.
     * General logic:
     * - if the ghost is at tile center:
     *      1. apply current intention as the new direction
     *      2. compute the next intention
     * - then move forward
     */
    @Override
    public void nextMove() {
        /* ghosts inside the pen do not use the normal pathfinding yet.*/

        if (ghostPenState == GhostPenState.IN) {
            return;
        }

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

    /**
     * Computes the next intended direction for the ghost.
     *
     * The behavior depends on the current ghost state:
     * - CHASE: use the ghost's normal target
     * - SCATTER: use the ghost's corner target
     * - FRIGHTENED / FRIGHTENED_END: choose a random valid direction
     * - DEAD: target the pen entry
     *
     * @return next intended direction
     */
    protected Direction computeNextIntention() {
        /*
        * Frightened ghosts move randomly, still respecting movement constraints.
        */
        if (ghostState == GhostState.FRIGHTENED || ghostState == GhostState.FRIGHTENED_END) {
            return computeRandomIntention();
        }
        /*
         * Standard deterministic target-based pathfinding.
         */
        TilePosition current = getCurrentTile();
        TilePosition nextTile = getMaze().getNeighbourTilePosition(current, direction); 
        Tile nextTileType = getMaze().getTile(nextTile);
        TilePosition target = getEffectiveTarget();
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
            * A ghost should not reverse direction unless there is no other choice.
            */
            if (dir == direction.reverse()) {
                continue;
            }
            /*
            * On ND / NT tiles, ghosts are not allowed to choose UP.
            */
            if (dir == Direction.UP && !nextTileType.ghostCanGoUp()) {
                continue;
            }

            /*
                * We must test movement possibilities from the next tile, not from the current one.
                */
            TilePosition candidateTile = getMaze().getNeighbourTilePosition(nextTile, dir);
            Tile candidateType = getMaze().getTile(candidateTile);

            if (candidateType.isWall()) {
                continue;
            }    
            double distance = squaredDistance(candidateTile, target);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestDirection = dir;
            }
        }


        /*
        * If we found a valid next direction, use it.
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
     * Computes an effective target depending on the current ghost state.
     *
     * CHASE:
     *   use the ghost's normal target
     *
     * SCATTER:
     *   use the ghost's scatter corner
     *
     * DEAD:
     *   target the pen entry
     *
     * @return effective target tile
     */
    protected TilePosition getEffectiveTarget() {
        switch (ghostState) {
            case SCATTER:
                return getScatterTarget();

            case DEAD:
                return getBoard().penEntry();

            case CHASE:
            default:
                return getTarget();
        }
    }

    /**
     * Computes a random valid next intention for frightened ghosts.
     *
     * Constraints:
     * - cannot go backward
     * - cannot go UP on tiles forbidding upward turns
     * - must avoid walls
     *
     * @return a random valid direction, or current direction as fallback
     */
    protected Direction computeRandomIntention() {
        TilePosition current = getCurrentTile();
        TilePosition nextTile = getMaze().getNeighbourTilePosition(current, direction);
        Tile nextTileType = getMaze().getTile(nextTile);

        List<Direction> candidates = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            if (dir == direction.reverse()) {
                continue;
            }

            if (dir == Direction.UP && !nextTileType.ghostCanGoUp()) {
                continue;
            }

            TilePosition candidateTile = getMaze().getNeighbourTilePosition(nextTile, dir);
            Tile candidateType = getMaze().getTile(candidateTile);

            if (!candidateType.isWall()) {
                candidates.add(dir);
            }
        }

        if (candidates.isEmpty()) {
            return direction;
        }

        int index = (int) (Math.random() * candidates.size());
        return candidates.get(index);
    }

    /**
     * Returns the scatter corner target of the ghost.
     *
     * Each concrete ghost uses its own scatter corner.
     * This default implementation gives a generic top-right corner,
     * but each ghost should override it.
     *
     * @return scatter target tile
     */
    protected TilePosition getScatterTarget() {
        return new TilePosition(0, getMaze().getWidth() - 1);
    }

    /**
     * Computes squared Euclidean distance between two tile positions.
     *
     * Squared distance is enough because we only compare distances.
     */
    protected double squaredDistance(TilePosition a, TilePosition b) {
        int dCol = a.getCol() - b.getCol();
        int dLine = a.getLine() - b.getLine();
        return dCol * dCol + dLine * dLine;
    }

    /*********************************************************************/
    /**                    Step 3 / Step 4 getters/setters              **/
    /*********************************************************************/


    @Override
    public void setGhostState(GhostState state) {
        this.ghostState = state;
    }

    /**
     * Changes the ghost state.
     *
     * when switching between CHASE and SCATTER, the ghost should prepare
     * a reverse direction intention.
     *
     * This simulates the "reversal of fortune" described in Pac-Man.
     */
    @Override
    public void changeGhostState(GhostState state) {
        GhostState previous = this.ghostState;
        this.ghostState = state;
        /*
         * Reversal is required when changing between CHASE and SCATTER.
         */
        boolean chaseScatterSwitch =
                (previous == GhostState.CHASE && state == GhostState.SCATTER) || (previous == GhostState.SCATTER && state == GhostState.CHASE);
        if (chaseScatterSwitch) {
            reverseDirectionIntention();
        }
    }

    @Override
    public GhostState getGhostState() {
        return this.ghostState;
    }

    @Override
    public void setGhostPenState(GhostPenState state) {
        this.ghostPenState = state;
    }

    @Override
    public GhostPenState getGhostPenState() {
        return ghostPenState;
    }

    @Override
    public void setOutOfPenDirection(Direction dir) {
        this.outOfPenDirection = dir;
    }

    @Override
    public Direction getOutOfPenDirection() {
        return outOfPenDirection;
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
        this.elroy = elroy;
    }

    @Override
    public int getElroy() {
        return this.elroy;
    }
}