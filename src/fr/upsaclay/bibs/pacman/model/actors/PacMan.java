package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;

/**
 * Concrete PacMan actor for step 1.
 *
 * PacMan moves pixel by pixel on the board and follows the simplified rules
 * given in the project statement:
 * - it starts with a default direction
 * - it keeps moving straight if no change is possible
 * - a reverse intention is applied immediately
 * - a blocked PacMan can be unblocked immediately by a valid intention
 * - otherwise a turn is only checked at the center of a tile
 */
public class PacMan extends AbstractActor {

    /**
     * Creates PacMan at a given position.
     * The board decides the correct start coordinates for classic/test mode.
     *
     * @param board the game board
     * @param startX initial x coordinate in pixels
     * @param startY initial y coordinate in pixels
     */
    public PacMan(Board board, int startX, int startY) {
        super(board, startX, startY, Direction.LEFT);
    }

    @Override
    public ActorType getType() {
        return ActorType.PACMAN;
    }

    /**
     * Initializes PacMan state at game start.
     * The default project direction is LEFT.
     */
    @Override
    public void start() {
        this.direction = Direction.LEFT;
        this.intention = null;
        this.blocked = false;
        this.speed = 1.0;
        this.stopTime = 0;
        this.realX = x;
        this.realY = y;
    }

    /**
     * Implements the simplified step 1 PacMan movement rules.
     */
    @Override
    public void nextMove() {
        applyImmediateIntentionIfNeeded();

        if (isBeforeCenter(direction)) {
            tryMoveForward();
            return;
        }

        if (isAtTileCenter()) {
            handleCenterBehavior();
            return;
        }

        if (isAfterCenter(direction)) {
            tryMoveForward();
        }
    }

    /**
     * Applies an intention immediately in the two situations required by the spec:
     * - reversing current direction
     * - unblocking PacMan if it is currently stuck
     */
    private void applyImmediateIntentionIfNeeded() {
        if (intention == null) {
            return;
        }

        if (intention == direction.reverse()) {
            direction = intention;
            intention = null;
            blocked = false;
            return;
        }

        if (blocked && canMove(intention)) {
            direction = intention;
            intention = null;
            blocked = false;
        }
    }

    /**
     * At tile center, PacMan may try to turn.
     * If the intended turn is impossible here, the simplified step 1 rule cancels it.
     * PacMan then attempts to continue forward.
     */
    private void handleCenterBehavior() {
        if (intention != null) {
            if (canMove(intention)) {
                direction = intention;
                intention = null;
                blocked = false;
            } else {
                intention = null;
            }
        }

        tryMoveForward();
    }
}