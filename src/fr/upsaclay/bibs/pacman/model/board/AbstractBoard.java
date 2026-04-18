package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.actors.*;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

import java.util.List;

public abstract class AbstractBoard implements Board {
    protected GameType gameType;
    protected Maze maze;
    protected PacMan pacMan;
    protected Board board;

    /**
     * Return the type of game of the board
     * Depending on the type, the maze or other initializations might be different
     * @return the game type
     */
    public abstract GameType getGameType();

    /**
     * Initialization of the board
     * (loads the maze, create and place the actors, etc.)
     * @throws PacManException in case something went wrong
     */
    public abstract void initialize() throws PacManException;

    /**
     * Start the actors
     * Perform all necessary actions to start actors at the beginning of the game
     */
    public abstract void startActors();

    /** Return the maze
     * @return the maze
     */
    public Maze getMaze() {
        return this.maze;
    }

    /**
     * Return PacMan
     * @return the PacMan actor
     */
    public Actor getPacMan() {
        return this.pacMan;
    }

    /**
     * Perform all necessary actions for the next game frame
     * This might require to move the actors,
     * perform some checks, etc.
     */
    public void nextFrame(){
        if(this.pacMan != null) {
            this.pacMan.nextMove();
        }
    }

    // Step 2 methods 
    public int getScore() {
        return 0;
    }

    public BoardState getBoardState() {
        return null;
    }

    public Ghost getGhost(GhostType ghostType) {
        return null;
    }

    public List<Ghost> getGhosts() {
        return null;
    }

    // Step 3 methods
    public int getLevel() {
        return 0;
    }

    public void initializeNewLevel(int level) throws PacManException {
    }

    public void setNumberOfLives(int nbLives) {
    }

    public int getNumberOfLives() {
        return 0;
    }

    public void initializeNewLife() {
    }

    public boolean hasGhost(GhostType ghostType) {
        return false;
    }

    public void disableGhost(GhostType ghostType) {
    }

    public void disableStateTime() {
    }

    public Direction getRandomDirection() {
        return null;
    }

    public TilePosition penEntry() {
        return null;
    }

    public int minYPen() {
        return 0;
    }

    public int maxYPen() {
        return 0;
    }

    public int penGhostXPosition(GhostType type) {
        return 0;
    }

    public int penGhostYPosition(GhostType type) {
        return 0;
    }

    public int outPenXPosition() {
        return 0;
    }

    public int outPenYPosition() {
        return 0;
    }

    public Counter noDotCounter() {
        return null;
    }

    public Counter specialDotCounter() {
        return null;
    }

    public double getLevelPacManSpeed() {
        return 0;
    }

    public double getFrightPacManSpeed() {
        return 0;
    }

    public double getLevelGhostSpeed() {
        return 0;
    }

    public double getTunnelGhostSpeed() {
        return 0;
    }

    public double getFrightGhostSpeed() {
        return 0;
    }

    public double getDeadGhostSpeed() {
        return 0;
    }

    // Step 4 methods
    public void setExtraLifeScore(int score) {
    }

    public int getExtraLifeScore() {
        return 0;
    }

    public Bonus getCurrentBonus() {
        return null;
    }

    public BonusType getLevelBonusType(int level) {
        return null;
    }

    public void setBonusOnBoard() {
    }

    public double getElroyGhostSpeed(int elroyNumber) {
        return 0;
    }

    public int getElroyDotValue(int elroyNumber) {
        return 0;
    }

    public List<BoardEvent> getCurrentEvents() {
        return null;
    }
}