package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.actors.*;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;
import fr.upsaclay.bibs.pacman.model.maze.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Common implementation shared by all boards.
 *
 * Why this abstract class:
 * - Both TEST and CLASSIC boards need the same generic game logic.
 * - Only initialization differs (maze file, actors present, starting positions).
 *
 * Step 2 responsibilities handled here:
 * - score
 * - board state
 * - list of ghosts
 * - frame evolution
 * - eating dots
 * - collision PacMan / ghosts
 * - end of level detection
 */
public abstract class AbstractBoard implements Board {

    /** Type of game: TEST or CLASSIC. */
    protected GameType gameType;

    /** Maze used by the board. */
    protected Maze maze;

    /** PacMan actor. */
    protected PacMan pacMan;

    /** Ghosts currently present on the board. */
    protected List<Ghost> ghosts;

    /** Current score. */
    protected int score;

    /** Current board state. */
    protected BoardState boardState;

    /**
     * Base constructor for all boards.
     * We initialize fields that are common to every board.
     */
    protected AbstractBoard() {
        this.ghosts = new ArrayList<>();
        this.score = 0;
        this.boardState = BoardState.INITIAL;
    }

    @Override
    public abstract GameType getGameType();

    @Override
    public abstract void initialize() throws PacManException;

    /**
     * Generic actor start logic shared by all boards.
     *
     * Why here:
     * - PacMan and ghosts must all be started together.
     * - The board state must become STARTED at that moment.
     */
    @Override
    public void startActors() {
        if (pacMan != null) {
            pacMan.start();
        }
        for (Ghost ghost : ghosts) {
            ghost.start();
        }
        boardState = BoardState.STARTED;
    }

    @Override
    public Maze getMaze() {
        return maze;
    }

    @Override
    public Actor getPacMan() {
        return pacMan;
    }

    /**
     * Main frame update for step 2.
     *
     * Order chosen:
     * 1. If the game is not started, do nothing
     * 2. Move PacMan
     * 3. Move ghosts
     * 4. Let PacMan eat a dot if present
     * 5. Check collisions PacMan / ghosts
     * 6. Check end of level
     *
     * This order matches the behavior expected by the provided tests.
     */
    @Override
    public void nextFrame() {
        if (boardState == BoardState.LEVEL_OVER || boardState == BoardState.LIFE_OVER) {
            return;
        }

        if (pacMan != null) {
            pacMan.nextFrame();
        }

        for (Ghost ghost : ghosts) {
            updateGhostSpeed(ghost);
            ghost.nextFrame();
            updateGhostSpeed(ghost);
        }

        eatDotIfNeeded();
        checkGhostCollision();
        checkLevelOver();
    }

    @Override
    public int getScore() {
        return score;
    }

    @Override
    public BoardState getBoardState() {
        return boardState;
    }

    @Override
    public Ghost getGhost(GhostType ghostType) {
        for (Ghost ghost : ghosts) {
            if (ghost.getGhostType() == ghostType) {
                return ghost;
            }
        }
        return null;
    }

    @Override
    public List<Ghost> getGhosts() {
        return Collections.unmodifiableList(ghosts);
    }

    /**
     * Step 2 helper:
     * if PacMan is currently on a dot, eat it, update score,
     * update maze tile, and apply stop time.
     *
     * Rules from the statement/tests:
     * - small dot (SD or ND): +10 points, stop 1 frame
     * - big dot (BD): +50 points, stop 3 frames
     *
     * Note:
     * Tile.clearDot() already handles:
     * - SD -> EE
     * - BD -> EE
     * - ND -> NT
     */
    protected void eatDotIfNeeded() {
        if (pacMan == null) {
            return;
        }

        TilePosition pos = pacMan.getCurrentTile();
        Tile tile = maze.getTile(pos);

        if (!tile.hasDot()) {
            return;
        }

        if (tile.hasBigDot()) {
            score += 50;
            pacMan.setStopTime(3);
        } else {
            score += 10;
            pacMan.setStopTime(1);
        }

        maze.setTile(pos, tile.clearDot());
    }

    /**
     * Step 2 helper:
     * if PacMan and a ghost are on the same tile, the life is over.
     */
    protected void checkGhostCollision() {
        if (pacMan == null) {
            return;
        }

        TilePosition pacPos = pacMan.getCurrentTile();
        for (Ghost ghost : ghosts) {
            if (pacPos.equals(ghost.getCurrentTile())) {
                boardState = BoardState.LIFE_OVER;
                return;
            }
        }
    }

    /**
     * Step 2 helper:
     * if all dots are eaten, the level is over.
     *
     * We only switch to LEVEL_OVER if the board is still STARTED.
     * This avoids overwriting LIFE_OVER if a collision already happened.
     */
    protected void checkLevelOver() {
        if (boardState == BoardState.STARTED && maze.getNumberOfDots() == 0) {
            boardState = BoardState.LEVEL_OVER;
        }
    }

    /**
     * Step 2 helper:
     * ghosts are slower on SL tiles.
     *
     * From the statement:
     * - normal ghost speed in step 2 can be around 0.94
     * - slow tunnel speed is 0.5 on SL
     *
     * This method is intentionally simple for step 2.
     */
    protected void updateGhostSpeed(Ghost ghost) {
        Tile tile = maze.getTile(ghost.getCurrentTile());
        if (tile == Tile.SL) {
            ghost.setSpeed(0.5);
        } else {
            ghost.setSpeed(getLevelGhostSpeed());
        }
    }

    /*********************************************************************/
    /**                    Step 3 / Step 4 stubs                        **/
    /**          kept minimal so step 2 can compile cleanly             **/
    /*********************************************************************/

    @Override
    public int getLevel() {
        return 1;
    }

    @Override
    public void initializeNewLevel(int level) throws PacManException {
        throw new UnsupportedOperationException("Not implemented before step 3");
    }

    @Override
    public void setNumberOfLives(int nbLives) {
        throw new UnsupportedOperationException("Not implemented before step 3");
    }

    @Override
    public int getNumberOfLives() {
        return 0;
    }

    @Override
    public void initializeNewLife() {
        throw new UnsupportedOperationException("Not implemented before step 3");
    }

    @Override
    public boolean hasGhost(GhostType ghostType) {
        return getGhost(ghostType) != null;
    }

    @Override
    public void disableGhost(GhostType ghostType) {
        throw new UnsupportedOperationException("Not implemented before step 3");
    }

    @Override
    public void disableStateTime() {
        throw new UnsupportedOperationException("Not implemented before step 3");
    }

    @Override
    public Direction getRandomDirection() {
        return Direction.LEFT;
    }

    @Override
    public TilePosition penEntry() {
        return null;
    }

    @Override
    public int minYPen() {
        return 0;
    }

    @Override
    public int maxYPen() {
        return 0;
    }

    @Override
    public int penGhostXPosition(GhostType type) {
        return 0;
    }

    @Override
    public int penGhostYPosition(GhostType type) {
        return 0;
    }

    @Override
    public int outPenXPosition() {
        return 0;
    }

    @Override
    public int outPenYPosition() {
        return 0;
    }

    @Override
    public Counter noDotCounter() {
        return null;
    }

    @Override
    public Counter specialDotCounter() {
        return null;
    }

    /**
     * PacMan speed for level 1.
     * For your current step-2 tests, speed 1.0 is enough.
     */
    @Override
    public double getLevelPacManSpeed() {
        return 1.0;
    }

    @Override
    public double getFrightPacManSpeed() {
        return 1.0;
    }

    /**
     * Blinky normal speed for level 1 in step 2.
     */
    @Override
    public double getLevelGhostSpeed() {
        return 0.94;
    }

    @Override
    public double getTunnelGhostSpeed() {
        return 0.5;
    }

    @Override
    public double getFrightGhostSpeed() {
        return 0.5;
    }

    @Override
    public double getDeadGhostSpeed() {
        return 1.0;
    }

    @Override
    public void setExtraLifeScore(int score) {
        throw new UnsupportedOperationException("Not implemented before step 4");
    }

    @Override
    public int getExtraLifeScore() {
        return 0;
    }

    @Override
    public Bonus getCurrentBonus() {
        return null;
    }

    @Override
    public BonusType getLevelBonusType(int level) {
        return null;
    }

    @Override
    public void setBonusOnBoard() {
        throw new UnsupportedOperationException("Not implemented before step 4");
    }

    @Override
    public double getElroyGhostSpeed(int elroyNumber) {
        return getLevelGhostSpeed();
    }

    @Override
    public int getElroyDotValue(int elroyNumber) {
        return 0;
    }

    @Override
    public List<BoardEvent> getCurrentEvents() {
        return Collections.emptyList();
    }
}