package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.actors.*;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;
import fr.upsaclay.bibs.pacman.model.maze.Tile;
import fr.upsaclay.bibs.pacman.bonus.SoundPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * Common implementation shared by all boards.
 *
 * Main responsibilities:
 * - store the maze
 * - store PacMan and the ghosts
 * - store game score, level and lives
 * - evolve the game frame by frame
 * - detect dot eating, collisions and end-of-level situations
 *
 * Step 3.5 additions:
 * - CHASE / SCATTER timed alternation
 * - frightened mode after eating a big dot
 * - ghosts can be eaten by PacMan while frightened
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

    /**Current level. */
    protected int level;

    /** Remaining extra lives (step 3). */
    protected int numberOfLives;

    /** Random generator used by the board. */
    protected Random random;

    /** Whether ghost timed state switching is disabled. */
    protected boolean stateTimeDisabled;

    /** Ghost types disabled before initialization. */
    protected List<GhostType> disabledGhosts;

    /** Counter for frames without dots eaten. */
    protected Counter noDotCounter;

    /** Special counter for ghost pen mechanics. */
    protected Counter specialDotCounter;

    /*********************************************************************/
    /** Step 3.5: ghost global mode timing                              **/
    /*********************************************************************/

    /** Global alternating state used for ghosts outside frightened mode. */
    protected GhostState globalGhostState;

    /** Number of frames spent in the current global ghost state. */
    protected int ghostStateFrameCount;

    /** Number of frames remaining in frightened mode. */
    protected int frightenedFramesRemaining;

    /** Number of ghosts eaten during the current frightened sequence. */
    protected int frightenedGhostsEaten;

    protected SoundPlayer soundPlayer;

    protected int extraLifeScore;
    protected boolean extraLifeGiven;
    protected List<BoardEvent> currentEvents;


    protected Bonus currentBonus;
    protected int nextBonusScore;
    protected int bonusDurationFrames;
    protected int bonusSpawnIndex;

    /**
     * Base constructor for all boards.
     *
     * We initialize:
     * - score at 0
     * - state at INITIAL
     * - level at 1
     * - number of extra lives at 2
     * - counters with a very large limit for now
     *
     * Why Integer.MAX_VALUE:
     * your Counter class requires a limit in the constructor,
     * but at this stage we mostly use the counters as simple counters.
     */
    protected AbstractBoard() {
        this.ghosts = new ArrayList<>();
        this.score = 0;
        this.boardState = BoardState.INITIAL;
        this.level = 1;
        this.numberOfLives = 2;
        this.extraLifeScore = 10000;
        this.extraLifeGiven = false;
        this.currentBonus = null;
        this.currentEvents = new ArrayList<>();
        this.random = new Random();
        this.stateTimeDisabled = false;
        this.disabledGhosts = new ArrayList<>();
        this.noDotCounter = new Counter(getNoDotCounterLimit());
        this.specialDotCounter = new Counter(Integer.MAX_VALUE);
        this.soundPlayer = new SoundPlayer();
        this.globalGhostState = GhostState.CHASE;
        this.ghostStateFrameCount = 0;
        this.frightenedFramesRemaining = 0;
        this.frightenedGhostsEaten = 0;
        this.nextBonusScore = 700;
        this.bonusDurationFrames = 0;
        this.bonusSpawnIndex = 0;
    }

    @Override
    public SoundPlayer getSoundPlayer(){
        return soundPlayer;
    };


    @Override
    public abstract GameType getGameType();

    @Override
    public abstract void initialize() throws PacManException;

    /**
     * Starts all actors and switches board state to STARTED.
     */
    @Override
    public void startActors() {
        if (pacMan != null) {
            pacMan.start();
            pacMan.setSpeed(getLevelPacManSpeed());
        }
        for (Ghost ghost : ghosts) {
            ghost.start();
        }

        
        ghostStateFrameCount = 0;
        frightenedFramesRemaining = 0;
        frightenedGhostsEaten = 0;

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
     *  4. Eat dot if needed
     * 5. Check collisions
     * 6. Check level over
     *
     * This order matches the behavior expected by the provided tests!!! 
     */
    @Override
    public void nextFrame() {
        currentEvents.clear();
        if (boardState != BoardState.STARTED) {
            return;
        }

        updateGhostStatesOverTime();

        if (pacMan != null) {
            if (frightenedFramesRemaining > 0) {
                pacMan.setSpeed(getFrightPacManSpeed());
            } else {
                pacMan.setSpeed(getLevelPacManSpeed());
            }
            pacMan.nextFrame();
        }

        for (Ghost ghost : ghosts) {
            updateGhostSpeed(ghost);
            ghost.nextFrame();
            updateGhostSpeed(ghost);
        }

        eatDotIfNeeded();
        updateBonus();
        eatBonusIfNeeded();
        updateNoDotCounterRelease();
        checkGhostCollision();
        checkLevelOver();
        updateElroy();
    }

    protected void updateBonus() {
        if (currentBonus == null && score >= nextBonusScore) {
            setBonusOnBoard();
            nextBonusScore += 1000;
            return;
        }

        if (currentBonus != null) {
            bonusDurationFrames--;

            if (bonusDurationFrames <= 0) {
                currentBonus = null;
            }
        }
    }

    protected void updateElroy() {
        Ghost blinky = getGhost(GhostType.BLINKY);
        if (blinky == null) {
            return;
        }

        int remainingDots = maze.getNumberOfDots();

        if (remainingDots <= getElroyDotValue(2)) {
            blinky.setElroy(2);
        } else if (remainingDots <= getElroyDotValue(1)) {
            blinky.setElroy(1);
        } else {
            blinky.setElroy(0);
        }
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
     * if PacMan is currently on a dot, eat it, update score,
     * update maze tile, and apply stop time.
     *
     * Important:
     * - your Counter class uses inc(), not increment()
     * - SD / ND = small dot = +10, stop 1 frame
     * - BD = big dot = +50, stop 3 frames
     *
     */
    protected void eatDotIfNeeded() {
        if (pacMan == null) {
            return;
        }

        TilePosition pos = pacMan.getCurrentTile();
        Tile tile = maze.getTile(pos);

        /*
         * If no dot was eaten on this frame, we increase the "no dot" counter.
         */
        if (!tile.hasDot()) {
            noDotCounter.inc();
            return;
        }

        /*
         * Eating a big dot gives more points and a longer stop time.
         */
        if (tile.hasBigDot()) {
            score += 50;
            currentEvents.add(new BoardEvent(BoardEventType.EAT_BIG_DOT, pos, 50));
            checkExtraLife();
            pacMan.setStopTime(3);
            /*
             * Big dot triggers frightened mode.
             */
            activateFrightenedMode();
        } else {
            score += 10;
            currentEvents.add(new BoardEvent(BoardEventType.EAT_SMALL_DOT, pos, 10));
            checkExtraLife();
            pacMan.setStopTime(1);
        }

        /*
         * Remove the dot from the maze and update counters.
         */
        maze.setTile(pos, tile.clearDot());
        noDotCounter.reset();
        specialDotCounter.inc();
        updateGhostReleaseCountersAfterDot();
    }

    /**
     * Activates frightened mode for all ghosts that are not dead.
     *
     * This is a simplified  version:
     * - every living ghost switches to FRIGHTENED
     * - frightened duration depends on level
     * - score chain resets to 0 eaten ghosts
     */
    protected void activateFrightenedMode() {
        int duration = getFrightenedDurationFrames();
        frightenedGhostsEaten = 0;
        if (soundPlayer != null) {
            soundPlayer.stop(); 
            soundPlayer.play("resources/sounds/capybara.wav");
        }
        if (duration <= 0) {
            frightenedFramesRemaining = 0;
            for (Ghost ghost : ghosts) {
                if (ghost.getGhostState() != GhostState.DEAD) {
                    ghost.changeGhostState(globalGhostState);
                }
            }
            return;
        }

        frightenedFramesRemaining = duration;

        for (Ghost ghost : ghosts) {
            if (ghost.getGhostState() != GhostState.DEAD) {
                ghost.setGhostState(GhostState.FRIGHTENED); 
                ghost.reverseDirectionIntention(); 
                updateGhostSpeed(ghost);
            }
        }
    }


    /**
     * Updates ghost mode timing.
     *
     * Two cases:
     * 1. Frightened is active:
     *    - count it down
     *    - when it ends, surviving ghosts return to the global state
     *
     * 2. Otherwise:
     *    - if timed switching is enabled, alternate CHASE / SCATTER
     */
    protected void updateGhostStatesOverTime() {
        /*
         * If state timing is disabled, we do not alternate CHASE/SCATTER.
         * However frightened mode still counts down if active.
         */
        if (frightenedFramesRemaining > 0) {
            frightenedFramesRemaining--;

            /*
             * Optional simplified blinking phase:
             * near the end, switch frightened ghosts to FRIGHTENED_END.
             */
            if (frightenedFramesRemaining <= getFrightenedEndFrames()) {
                for (Ghost ghost : ghosts) {
                    if (ghost.getGhostState() == GhostState.FRIGHTENED) {
                        ghost.setGhostState(GhostState.FRIGHTENED_END); 
                    }
                }
            }

            /*
            * End of frightened mode:
            * all non-dead ghosts go back to the current global state.
            */
            if (frightenedFramesRemaining == 0) {
                if (soundPlayer != null) {
                    soundPlayer.stop(); 
                    soundPlayer.play("resources/sounds/game_main.wav"); 
                }
                for (Ghost ghost : ghosts) {
                    if (ghost.getGhostState() != GhostState.DEAD) {
                        ghost.changeGhostState(globalGhostState);
                    }
                }
            }

            return;
        }

        /*
         * If timed state switching is disabled, stop here.
         */
        if (stateTimeDisabled) {
            return;
        }

        ghostStateFrameCount++;

        /*
         * Alternate CHASE / SCATTER using simplified timing.
         */
        int duration = getCurrentGlobalGhostStateDurationFrames();
        if (ghostStateFrameCount >= duration) {
            ghostStateFrameCount = 0;

            if (globalGhostState == GhostState.CHASE) {
                globalGhostState = GhostState.SCATTER;
            } else {
                globalGhostState = GhostState.CHASE;
            }

            /*
             * Apply the new state to all ghosts that are not frightened or dead.
             */
            for (Ghost ghost : ghosts) {
                GhostState state = ghost.getGhostState();
                if (state != GhostState.FRIGHTENED
                        && state != GhostState.FRIGHTENED_END
                        && state != GhostState.DEAD) {
                    ghost.changeGhostState(globalGhostState);
                }
            }
        }
    }


    /**
     * Checks collision PacMan / ghost.
     *
     * Cases:
     * - ghost frightened -> ghost is eaten by PacMan, becomes DEAD, score bonus
     * - ghost normal -> PacMan loses a life or the game ends
     */
    protected void checkGhostCollision() {
        if (pacMan == null) {
            return;
        }

        TilePosition pacPos = pacMan.getCurrentTile();
        for (Ghost ghost : ghosts) {
            if (!pacPos.equals(ghost.getCurrentTile())) {
                    continue;
                }

            GhostState ghostState = ghost.getGhostState();
            

            if (ghostState == GhostState.DEAD) {
            continue;
        }

            /*
            * If the ghost is frightened, PacMan eats it.
            */
            if (ghostState == GhostState.FRIGHTENED || ghostState == GhostState.FRIGHTENED_END) {
                ghost.setGhostState(GhostState.DEAD);
                frightenedGhostsEaten++;

                soundPlayer.stop();
                soundPlayer.play("resources/sounds/fah.wav");
                soundPlayer.playLoop("resources/sounds/capybara.wav");
                /*
                * Classic Pac-Man frightened scores:
                */
                int gained = (int) (200 * Math.pow(2, Math.min(frightenedGhostsEaten - 1, 3)));
                score += gained;
                currentEvents.add(new BoardEvent(BoardEventType.EAT_GHOST, ghost.getCurrentTile(), gained));
                checkExtraLife();

                continue;
            }

            /*
            * Otherwise PacMan is caught.
            */
            if (numberOfLives > 0) {
                boardState = BoardState.LIFE_OVER;
            } else {
                boardState = BoardState.GAME_OVER;
            }
            return;
        }
    }

    /**
     * If there are no more dots, the level is over.
     */
    protected void checkLevelOver() {
        if (boardState == BoardState.STARTED && maze.getNumberOfDots() == 0) {
            boardState = BoardState.LEVEL_OVER;
        }
    }

    /**
     * Updates ghost speed depending on state and tile.
     *
     * Priority:
     * - DEAD -> dead speed
     * - frightened on tunnel tile -> tunnel speed
     * - frightened -> frightened speed
     * - inside pen / tunnel -> tunnel speed
     * - otherwise -> normal ghost speed
     */
    protected void updateGhostSpeed(Ghost ghost) {
        /*
        * Ghosts moving inside / into / out of the pen always use tunnel speed.
        */
        if (ghost.getGhostPenState() == GhostPenState.GET_IN
                || ghost.getGhostPenState() == GhostPenState.GET_OUT
                || ghost.getGhostPenState() == GhostPenState.IN) {
            ghost.setSpeed(getTunnelGhostSpeed());
            return;
        }

        /*
        * Dead ghosts use dead speed while returning to the pen entry.
        */
        if (ghost.getGhostState() == GhostState.DEAD) {
            ghost.setSpeed(getDeadGhostSpeed());
            return;
        }
        Tile tile = maze.getTile(ghost.getCurrentTile());

        /*
        * Tunnel / slow tiles always force tunnel speed.
        */
        if (tile == Tile.SL) {
            ghost.setSpeed(getTunnelGhostSpeed());
            return;
        }

        /*
        * Frightened ghosts use frightened speed.
        */
        if (ghost.getGhostState() == GhostState.FRIGHTENED
                || ghost.getGhostState() == GhostState.FRIGHTENED_END) {
            ghost.setSpeed(getFrightGhostSpeed());
            return;
        }

        if (ghost.getGhostType() == GhostType.BLINKY && ghost.getElroy() > 0) {
            ghost.setSpeed(getElroyGhostSpeed(ghost.getElroy()));
            return;
        }
        /*
        * Otherwise ghosts use their normal level speed.
        */
        ghost.setSpeed(getLevelGhostSpeed());
    }

    /*********************************************************************/
    /**                    Step 3 / Step 4 stubs                        **/
    /**                                                                 **/
    /*********************************************************************/

    protected int getNoDotCounterLimit() {
        if (level <= 4) {
            return 4 * 60;
        }
        return 3 * 60;
    }

    @Override
    public int getLevel() {
        return level;
    }

    /**
     * Initializes a new level:
     * - keeps current score
     * - reloads the maze
     * - resets actors to their initial positions
     * - updates the level
     */
    @Override
    public void initializeNewLevel(int level) throws PacManException {
        int oldScore = this.score;
        int oldLives = this.numberOfLives;


        this.level = level;
        initialize();

        pacMan.setSpeed(getLevelPacManSpeed());
        noDotCounter = new Counter(getNoDotCounterLimit());

        this.score = oldScore;
        this.numberOfLives = oldLives;
        this.boardState = BoardState.INITIAL;

        this.globalGhostState = GhostState.SCATTER;
        this.ghostStateFrameCount = 0;
        this.frightenedFramesRemaining = 0;
        this.frightenedGhostsEaten = 0;

        this.currentBonus = null;
        this.nextBonusScore = 700;
        this.bonusDurationFrames = 0;
        this.bonusSpawnIndex = 0;
    }

    @Override
    public void setNumberOfLives(int nbLives) {
        if (nbLives < 0) {
            throw new IllegalArgumentException("Number of lives must be >= 0");
        }
        this.numberOfLives = nbLives;
    }

    @Override
    public int getNumberOfLives() {
        return numberOfLives;
    }


    /**
     * Initializes a new life:
     * - one life is consumed
     * - actors are reset to their initial positions
     * - maze is NOT reloaded
     * - score is kept
     */
    @Override
    public void initializeNewLife() {
        if (numberOfLives > 0) {
            numberOfLives--;
        }
        if (soundPlayer != null) {
            soundPlayer.stop();
        }
        resetActorsToInitialPositions();
        boardState = BoardState.INITIAL;
        this.globalGhostState = GhostState.SCATTER;
        this.ghostStateFrameCount = 0;
        this.frightenedFramesRemaining = 0;
        this.frightenedGhostsEaten = 0;

        noDotCounter = new Counter(getNoDotCounterLimit());
    }

    /**
     * Helper used by initializeNewLife().
     *
     * It resets actors to their initial positions without reloading the maze.
     */
    protected void resetActorsToInitialPositions() {
        if (getGameType() == GameType.CLASSIC) {
            pacMan = new PacMan(this, 112, 211);
            pacMan.setSpeed(getLevelPacManSpeed());
            ghosts.clear();

            if (!disabledGhosts.contains(GhostType.BLINKY)) {
                Blinky blinky = new Blinky(this, 112, 115);
                blinky.setGhostState(GhostState.SCATTER);
                blinky.setGhostPenState(GhostPenState.OUT);
                blinky.setOutOfPenDirection(Direction.LEFT);
                blinky.setSpeed(getLevelGhostSpeed());
                ghosts.add(blinky);
            }

            if (!disabledGhosts.contains(GhostType.PINKY)) {
                Pinky pinky = new Pinky(this, 112, 139);
                pinky.setGhostState(GhostState.SCATTER);
                pinky.setGhostPenState(GhostPenState.IN);
                pinky.setOutOfPenDirection(Direction.LEFT);
                pinky.setSpeed(getLevelGhostSpeed());
                ghosts.add(pinky);
            }

            if (!disabledGhosts.contains(GhostType.INKY)) {
                Inky inky = new Inky(this, 96, 139);
                inky.setGhostState(GhostState.SCATTER);
                inky.setGhostPenState(GhostPenState.IN);
                inky.setOutOfPenDirection(Direction.LEFT);
                inky.setSpeed(getLevelGhostSpeed());
                ghosts.add(inky);
            }

            if (!disabledGhosts.contains(GhostType.CLYDE)) {
                Clyde clyde = new Clyde(this, 128, 139);
                clyde.setGhostState(GhostState.SCATTER);
                clyde.setGhostPenState(GhostPenState.IN);
                clyde.setOutOfPenDirection(Direction.LEFT);
                clyde.setSpeed(getLevelGhostSpeed());
                ghosts.add(clyde);
            }

        } else {
            pacMan = new PacMan(this, 35, 75);
            pacMan.setSpeed(getLevelPacManSpeed());
            ghosts.clear();
        }
    }

    @Override
    public boolean hasGhost(GhostType ghostType) {
        return getGhost(ghostType) != null;
    }


    /**
     * Disables a ghost before board initialization.
     */
    @Override
    public void disableGhost(GhostType ghostType) {
        if (!disabledGhosts.contains(ghostType)) {
            disabledGhosts.add(ghostType);
        }
    }

    @Override
    public void disableStateTime() {
        this.stateTimeDisabled = true;
    }

    @Override
    public Direction getRandomDirection() {
        Direction[] dirs = Direction.values();
        return dirs[random.nextInt(dirs.length)];
    }

    @Override
    public TilePosition penEntry() {
        return new TilePosition(14, 13);
    }

    @Override
    public int minYPen() {
        return 134;
    }

    @Override
    public int maxYPen() {
        return 144;
    }

    @Override
    public int penGhostXPosition(GhostType type) {
        switch (type) {
            case BLINKY:
            case PINKY:
                return 112;
            case INKY:
                return 96;
            case CLYDE:
                return 128;
            default:
                return 112;
        }
    }

    @Override
    public int penGhostYPosition(GhostType type) {
        return 139;
    }

    @Override
    public int outPenXPosition() {
        return 112;
    }

    @Override
    public int outPenYPosition() {
        return 115;
    }

    @Override
    public Counter noDotCounter() {
        return noDotCounter;
    }

    @Override
    public Counter specialDotCounter() {
        specialDotCounter.reset();
        return specialDotCounter;
    }

    /**
     * PacMan speed for level.
     */
    @Override
    public double getLevelPacManSpeed() {
        if (level == 1) return 1.0;
        if (level >= 2 && level <= 4) return 1.14;
        if (level <= 20) return 1.26;
        return 1.14;
    }

    @Override
    public double getFrightPacManSpeed() {
        if (level == 1) return 1.14;
        if (level <= 4) return 1.20;
        return 1.26;
    }

    /**
     * Ghost normal speed for level.
     */
    @Override
    public double getLevelGhostSpeed() {
        if (level == 1) return 0.94;
        if (level <= 4) return 1.07;
        return 1.20;
    }

    @Override
    public double getTunnelGhostSpeed() {
        if (level == 1) return 0.50;
        if (level >= 2 && level <= 4) return 0.57;
        return 0.63;
    }

    @Override
    public double getFrightGhostSpeed() {
        if (level == 1) return 0.63;
        if (level <= 4) return 0.69;
        return 0.75;
    }

    @Override
    public double getDeadGhostSpeed() {
        return 1.26;
    }

    /**
     * Simplified frightened duration in frames.
     *
     * We convert seconds to frames using 60 fps.
     */
    protected int getFrightenedDurationFrames() {
        switch (level) {
            case 1: return 360;
            case 2: return 300;
            case 3: return 240;
            case 4: return 180;
            case 5: return 120;
            case 6: return 300;
            case 7: return 120;
            case 8: return 120;
            case 9: return 60;
            case 10: return 300;
            case 11: return 120;
            case 12: return 60;
            case 13: return 60;
            case 14: return 180;
            case 15: return 60;
            case 16: return 60;
            case 18: return 60;
            default: return 0; // 17, 19+
        }
    }

    /**
     * Simplified blinking duration at the end of frightened mode.
     */
    protected int getFrightenedEndFrames() {
        switch (level) {
            case 9:
            case 12:
            case 13:
            case 15:
            case 16:
            case 18:
                return 90;
            case 17:
                return 0;
            default:
                if (level >= 19) return 0;
                return 150;
        }
    }

    /**
     * Duration of the current CHASE / SCATTER phase in frames.
     *
     * This is a simplified approximation for step 3.5.
     */
    protected int getCurrentGlobalGhostStateDurationFrames() {
        if (globalGhostState == GhostState.SCATTER) {
            return 7 * 60;
        }
        return 20 * 60;
    }

    protected void updateGhostReleaseCountersAfterDot() {
        Ghost pinky = getGhost(GhostType.PINKY);
        Ghost inky = getGhost(GhostType.INKY);
        Ghost clyde = getGhost(GhostType.CLYDE);

        boolean specialMode = boardState == BoardState.STARTED && level == 1 && numberOfLives < 2;
        // ou un bool dédié, plus propre

        if (specialMode) {
            specialDotCounter.inc();

            if (specialDotCounter.getValue() == 7 && pinky != null && pinky.getGhostPenState() == GhostPenState.IN) {
                pinky.setGhostPenState(GhostPenState.GET_OUT);
                return;
            }
            if (specialDotCounter.getValue() == 17 && inky != null && inky.getGhostPenState() == GhostPenState.IN) {
                inky.setGhostPenState(GhostPenState.GET_OUT);
                return;
            }
            if (specialDotCounter.getValue() >= 32) {
                // après ça, Clyde utilise son dotCounter normal
                if (clyde != null && clyde.getGhostPenState() == GhostPenState.IN && clyde.hasDotCounter()) {
                    clyde.getDotCounter().inc();
                    if (clyde.getDotCounter().hasReachedLimit()) {
                        clyde.setGhostPenState(GhostPenState.GET_OUT);
                    }
                }
                return;
            }
            return;
        }

        if (inky != null && inky.getGhostPenState() == GhostPenState.IN && inky.hasDotCounter()) {
            inky.getDotCounter().inc();
            if (inky.getDotCounter().hasReachedLimit()) {
                inky.setGhostPenState(GhostPenState.GET_OUT);
                return;
            }
        }

        if (clyde != null && clyde.getGhostPenState() == GhostPenState.IN && clyde.hasDotCounter()) {
            clyde.getDotCounter().inc();
            if (clyde.getDotCounter().hasReachedLimit()) {
                clyde.setGhostPenState(GhostPenState.GET_OUT);
            }
        }
    }

    protected void updateNoDotCounterRelease() {
        if (!noDotCounter.hasReachedLimit()) {
            return;
        }

        Ghost inky = getGhost(GhostType.INKY);
        Ghost clyde = getGhost(GhostType.CLYDE);

        if (inky != null && inky.getGhostPenState() == GhostPenState.IN) {
            inky.setGhostPenState(GhostPenState.GET_OUT);
            noDotCounter.reset();
            return;
        }

        if (clyde != null && clyde.getGhostPenState() == GhostPenState.IN) {
            clyde.setGhostPenState(GhostPenState.GET_OUT);
            noDotCounter.reset();
            return;
        }

        noDotCounter.reset();
    }

    @Override
    public void setExtraLifeScore(int score) {
        if (score <= 0) {
            throw new IllegalArgumentException("Extra life score must be positive");
        }
        this.extraLifeScore = score;
        this.extraLifeGiven = false;
    }

    @Override
    public int getExtraLifeScore() {
        return extraLifeScore;
    }

    protected void checkExtraLife() {
        if (!extraLifeGiven && score >= extraLifeScore) {
            numberOfLives++;
            extraLifeGiven = true;
            currentEvents.add(new BoardEvent(BoardEventType.EXTRA_LIFE, pacMan.getCurrentTile(), 1));

            if (soundPlayer != null) {
                soundPlayer.play("resources/sounds/powerUp.wav");
            }
        }
    }

    protected void eatBonusIfNeeded() {
        if (currentBonus == null || !currentBonus.isActive()) {
            return;
        }

        int dx = Math.abs(pacMan.getX() - currentBonus.getX());
        int dy = Math.abs(pacMan.getY() - currentBonus.getY());

        // Collision en pixels, plus permissive que la tuile exacte
        if (dx <= 10 && dy <= 10) {
            int value = currentBonus.getBonusType().getValue();
            score += value;

            currentEvents.add(new BoardEvent(
                BoardEventType.EAT_BONUS,
                currentBonus.getCurrentTile(),
                value
            ));
            checkExtraLife();


            currentBonus = null;
        }
    }
    @Override
    public Bonus getCurrentBonus() {
        if (currentBonus != null && currentBonus.isActive()) {
            return currentBonus;
        }
        return null;
    }

    @Override
    public BonusType getLevelBonusType(int level) {
        BonusType[] cycle = {
            BonusType.CHERRY,
            BonusType.STRAWBERRY,
            BonusType.PEACH,
            BonusType.APPLE,
            BonusType.GRAPES,
            BonusType.GALAXIAN,
            BonusType.BELL,
            BonusType.KEY
        };

        return cycle[(level - 1) % cycle.length];
    }


    @Override
    public void setBonusOnBoard() {
        BonusType[] cycle = BonusType.values();
        BonusType type = cycle[bonusSpawnIndex % cycle.length];

        int[][] positions = {
            {112, 163}, // centre classique
            {40, 187},  // gauche bas
            {184, 187}, // droite bas
            {40, 51},   // gauche haut
            {184, 51}   // droite haut
        };

        int[] pos = positions[bonusSpawnIndex % positions.length];
        bonusSpawnIndex++;

        currentBonus = new FruitBonus(this, pos[0], pos[1], type);
        bonusDurationFrames = 10 * 60;
    }

    @Override
    public int getElroyDotValue(int elroyNumber) {
        if (elroyNumber == 1) {
            if (level == 1) return 20;
            if (level <= 4) return 30;
            return 40;
        }

        if (elroyNumber == 2) {
            if (level == 1) return 10;
            if (level <= 4) return 15;
            return 20;
        }

        return 0;
    }

    @Override
    public List<BoardEvent> getCurrentEvents() {
        return Collections.unmodifiableList(currentEvents);
    }

    @Override
    public double getElroyGhostSpeed(int elroyNumber) {
        if (elroyNumber == 1) {
            if (level == 1) return 1.0;
            if (level <= 4) return 1.14;
            return 1.26;
        }

        if (elroyNumber == 2) {
            if (level == 1) return 1.14;
            if (level <= 4) return 1.20;
            return 1.33;
        }

        return getLevelGhostSpeed();
    }

    
}