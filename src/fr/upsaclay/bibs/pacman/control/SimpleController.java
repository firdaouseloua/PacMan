package fr.upsaclay.bibs.pacman.control;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.board.BoardState;

/**
 * Simple controller without view, used for testing or simulation.
 * Implements the Controller interface for SIMPLE mode.
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
        * Actions allowed when the level is over: only NEXT_LEVEL should be accepted.
        */
        if (state == BoardState.LEVEL_OVER) {
            if (action == GameAction.NEXT_LEVEL) {
                initialize();
                gameStarted = false;
                paused = false;
                return;
            }
            throw new ForbiddenActionException(action);
        }

        /*
        * Actions allowed when a life is over: only NEW_GAME should be accepted in step 2.
        */
        if (state == BoardState.LIFE_OVER) {
            if (action == GameAction.NEW_GAME) {
                initialize();
                gameStarted = false;
                paused = false;
                return;
            }
            throw new ForbiddenActionException(action);
        }

        /*
        * Normal state machine for INITIAL / STARTED gameplay.
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