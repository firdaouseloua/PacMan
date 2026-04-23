package fr.upsaclay.bibs.pacman.control;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;

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
        switch (action) {
            case START:
                if (!gameStarted) {
                    gameStarted = true;
                    paused = false;
                    if (board != null) {
                        board.startActors(); 
                    }
                }
                else{
                    throw new ForbiddenActionException(action);
                }
                break;
            case PAUSE:
                if (gameStarted && !paused) {
                    paused = true;
                } else {
                    throw new ForbiddenActionException(action);
                }
                break;
            case RESUME:
                if (gameStarted && paused) {
                    paused = false;
                } else {
                    throw new ForbiddenActionException(action);
                }
                break;
            case NEXT_FRAME:
                if (gameStarted && !paused) {
                    board.nextFrame();
                } else {
                    throw new ForbiddenActionException(action);
                }
                break;
            case UP:
            case LEFT:
            case DOWN:
            case RIGHT:
                if (gameStarted && !paused && (board.getPacMan() != null)) {
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