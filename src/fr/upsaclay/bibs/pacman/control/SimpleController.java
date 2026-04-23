package fr.upsaclay.bibs.pacman.control;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.board.BoardState;

/**
 * Simple controller without view, used for testing or simulation.
 * Implements the Controller interface for SIMPLE mode.
 * 
 *  Main role:
 * - create and initialize the board
 * - receive actions
 * - check whether actions are allowed depending on the current state
 * - forward valid actions to the model
 *
 * Step 3 additions:
 * - handle NEXT_LEVEL
 * - handle NEW_GAME
 * - distinguish LIFE_OVER from GAME_OVER
 */
public class SimpleController implements Controller {

    private GameType gameType;
    private Board board;
    private boolean gameStarted;
    private boolean paused;

    public SimpleController() {
        this.gameType = GameType.CLASSIC;
        this.gameStarted = false;
        this.paused = false;
    }

    @Override
    public void initialize() throws PacManException {
        initializeNewGame();
    }

    @Override
    public void initializeNewGame() throws PacManException {
        try {
            this.board = Board.createBoard(this.gameType);
            this.board.initialize();

            /* Step 3 default values for a fresh game.*/
            this.board.setNumberOfLives(2);
            /*
             * The board returned by createBoard starts at level 1 in our current design.
             * If later needed, this could be made more explicit.
             */
            this.gameStarted = false;
            this.paused = false;

        }
        catch (PacManException e) {
            throw new PacManException("Failed to initialize a new game");
        }
    }

    @Override
    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    @Override
    public GameType getGameType() {
        return this.gameType;
    }

    /**
     * Receives an action and decides whether it is allowed.
     */
    @Override
    public void receiveAction(GameAction action) throws PacManException {
        /*
        * We first protect against the case where the board is not initialized yet.
        */
        if (board == null) {
            throw new ForbiddenActionException(action);
        }

        BoardState state = board.getBoardState();

        /*
         * LEVEL_OVER:
         * only NEXT_LEVEL should be accepted.
         */
        if (state == BoardState.LEVEL_OVER) {
            if (action == GameAction.NEXT_LEVEL) {
                board.initializeNewLevel(board.getLevel()+1);
                gameStarted = true;
                paused = false;
                return;
            }
            throw new ForbiddenActionException(action);
        }

        /*
         * LIFE_OVER:
         * we now restart the game flow with a NEW_GAME action in the visual version,
         * but if there are still lives left, we can also reinitialize the current life.
         */
        if (state == BoardState.LIFE_OVER) {
            if (action == GameAction.NEW_GAME) {
                initializeNewGame();
                return;
            }
            throw new ForbiddenActionException(action);
        }

        /*
         * GAME_OVER:
         * only NEW_GAME is allowed.
         */
        if (state == BoardState.GAME_OVER) {
            if (action == GameAction.NEW_GAME) {
                initializeNewGame();
                return;
            }
            throw new ForbiddenActionException(action);
        }

        /*
        * Normal state machine for gameplay.
        */
        switch (action) {
            case START:
                if (!gameStarted) {
                    gameStarted = true;
                    paused = false;
                    board.startActors();
                } else {
                    throw new ForbiddenActionException(action);
                }
                break;

            case PAUSE:
                if (gameStarted && !paused && state == BoardState.STARTED) {
                    paused = true;
                } else {
                    throw new ForbiddenActionException(action);
                }
                break;

            case RESUME:
                if (gameStarted && paused && state == BoardState.STARTED) {
                    paused = false;
                } else {
                    throw new ForbiddenActionException(action);
                }
                break;

            case NEXT_FRAME:
                if (gameStarted && !paused && state == BoardState.STARTED) {
                    board.nextFrame();
                } else {
                    throw new ForbiddenActionException(action);
                }
                break;

            case UP:
            case LEFT:
            case DOWN:
            case RIGHT:
                if (gameStarted && !paused && state == BoardState.STARTED && board.getPacMan() != null) {
                    board.getPacMan().setIntention(actionToDirection(action));
                } else {
                    throw new ForbiddenActionException(action);
                }
                break;

            default:
                throw new ForbiddenActionException(action);
        }
    }

    private Direction actionToDirection(GameAction action) throws ForbiddenActionException{
        switch (action) {
            case UP: return Direction.UP;
            case LEFT: return Direction.LEFT;
            case DOWN: return Direction.DOWN;
            case RIGHT: return Direction.RIGHT;
            default: throw new ForbiddenActionException(action);
        }
    }

    @Override
    public Board getBoard() {
        return this.board;
    }
}