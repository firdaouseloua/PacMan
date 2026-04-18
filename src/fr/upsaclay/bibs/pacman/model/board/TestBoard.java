package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.actors.*;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

import java.util.List;

public class TestBoard extends AbstractBoard {
    /**
     * Return the type of game of the board
     * Depending on the type, the maze or other initializations might be different
     * @return the game type
     */
    @Override
    public GameType getGameType(){
        return GameType.TEST;
    };

    /**
     * Initialization of the board
     * (loads the maze, create and place the actors, etc.)
     * @throws PacManException in case something went wrong
     */
    @Override
    public void initialize() throws PacManException {
        try {
            this.maze = Maze.loadFromFile("/home/neorain/M1_bioinfo/Projet_Pacman/resources/test.txt");
        } catch (Exception e) {
            throw new PacManException("Error while loading the maze");
        }
    };

    /**
     * Start the actors
     * Perform all necessary actions to start actors at the beginning of the game
     */
    @Override
    public void startActors(){
    };

    /** Return the maze
     * @return the maze
     */
    @Override
    public Maze getMaze(){
        return this.maze;
    };

    /**
     * Return PacMan
     * @return the PacMan actor
     */
    @Override
    public Actor getPacMan(){
        return null;
    };

    /**
     * Perform all necessary actions for the next game frame
     * This might require to move the actors,
     * perform some checks, etc.
     */
    @Override
    public void nextFrame(){
    };

    // Step 2
    // The methods below won't be used / tested before step 2

    /**
     * Return the current score of the game
     * @return the score
     */
    public int getScore(){
        return 0;
    };

    /**
     * Return the current state of the board
     * used to detect end of level / end of game`
     * @return the board state
     */
    public BoardState getBoardState(){
        return null;
    };

    /**
     * Return the ghost of a given type
     * If the board does not contain such a ghost, it returns null
     * @param ghostType, the type of ghost
     * @return a ghost or null
     */
    public Ghost getGhost(GhostType ghostType){
        return null;
    };

    /**
     * Return the list of ghosts present on the board
     * @return a list of ghost (might be empty)
     */
    public List<Ghost> getGhosts(){
        return null;
    };

    // Step 3
    // The methods below won't be used / tested before step 3

    /**
     * Return the current level of the game
     * @return a positive integer
     */
    public int getLevel(){
        return 0;
    };

    /**
     * Perfom all necessary actions to initiliaze a new level
     * (might need to load the maze, to place the actors, etc)
     * @param level, a positive integer
     * @throws PacManException if anything goes wrong
     */
    public void initializeNewLevel(int level) throws PacManException{
    };

    /**
     * Sets the number of extra lives that pacman has
     * @param nbLives, a non negative integer
     */
    public void setNumberOfLives(int nbLives){
    };

    /**
     * Return the current number of extra lives
     * @return a non negative integer
     */
    public int getNumberOfLives(){
        return 0;
    };

    /**
     * Perform all necessary actions to initialize the game after a life has been lost
     * (reduce the nb of lives, replace the actors, re-initialize certain values)
     */
    public void initializeNewLife(){
    };

    /**
     * Return whether the board contain a certain type of ghost
     * @param ghostType the type of ghost
     * @return true if this ghost is on the board
     */
    public boolean hasGhost(GhostType ghostType){
        return false;
    };

    /**
     * Disables a certain ghost before game initialization
     * @param ghostType the type of ghost to disable
     */
    public void disableGhost(GhostType ghostType){
    };

    /**
     * Disable the use of state time before game initialization
     */
    public void disableStateTime(){
    };

    /**
     * Return a pseudo-random direction
     * The random generator is initialized once at the beginning of the game
     * @return a pseudo-random direction
     */
    public Direction getRandomDirection(){
        return null;
    };

    /**
     * Re turn the tile position of the pen entry
     * (this corresponds to the ghost target when dead)
     * @return a tile position
     */
    public TilePosition penEntry(){
        return null;
    };

    /**
     * Return the minimum y value a ghost can take when inside the pen
     * @return a positive integer
     */
    public int minYPen(){
        return 0;
    };

    /**
     * Return the maximal y value a ghost can take when inside the pen
     * @return a positive integer
     */
    public int maxYPen(){
        return 0;
    };

    /**
     * Return the x position of given ghost type inside the pen
     * @param type, a ghost type
     * @return a positive integer
     */
    public int penGhostXPosition(GhostType type){
        return 0;
    };

    /**
     * Return the y position of given ghost type inside the pen
     * @param type, a ghost type
     * @return a positive integer
     */
    public int penGhostYPosition(GhostType type){
        return 0;
    };

    /**
     * Return the x position used by ghost to enter / leave the ghost pen
     * @return a positive integer
     */
    public int outPenXPosition(){
        return 0;
    };

    /**
     * Return the y position used by ghost to enter / leave the ghost pen
     * @return a positive integer
     */
    public int outPenYPosition(){
        return 0;
    };

    /**
     * Return the counter used to count the number of successive frames without eating dots
     * This is a way to count the time passed between 2 dots are eaten
     * @return a counter
     */
    public Counter noDotCounter(){
        return null;
    };

    /**
     * Return the board "special counter" used for allowing ghosts out of the pen
     * @return a counter
     */
    public Counter specialDotCounter(){
        return null;
    };

    /**
     * Return the normal speed of pacman at the board current level
     * @return a speed as a decimal
     */
    public double getLevelPacManSpeed(){
        return 0;
    };

    /**
     * Return the speed of pacman in "fright" mode at the board current level
     * @return a speed as a decimal
     */
    public double getFrightPacManSpeed(){
        return 0;
    };

    /**
     * Return the normal speed of ghost at the board current level
     * @return a speed as a decimal
     */
    public double getLevelGhostSpeed(){
        return 0;
    };

    /**
     * Return the slow speed of ghosts at current level (inside the tunnel)
     * @return a speed as a decimal
     */
    public double getTunnelGhostSpeed(){
        return 0;
    };

    /**
     * return the speed of ghosts in "fright" mode at current level
     * @return a speed as a decimal
     */
    public double getFrightGhostSpeed(){
        return 0;
    };

    /**
     * return the speed of ghosts when dead at current level
     * @return a speed as a decimal
     */
    public double getDeadGhostSpeed(){
        return 0;
    };

    // Step 4
    // The methods below won't be used / tested before step 4

    /**
     * Sets the score value at which pacman gains a new life
     * @param score, a positive integer
     */
    public void setExtraLifeScore(int score){
    };

    /**
     * Return the score value at chich pacman gains a new life
     * @return a positive integer
     */
    public int getExtraLifeScore(){
        return 0;
    };

    /**
     * Return the current bonus n the board if existing, null otherwise
     * @return a Bonus or null
     */
    public Bonus getCurrentBonus(){
        return null;
    };

    /**
     * Return the bonus type associated with given level
     * @param level, a positive integer
     * @return the bonus type of this level
     */
    public BonusType getLevelBonusType(int level){
        return null;
    };

    /**
     * Place the bonus associated with current level on board at its intended position
     */
    public void setBonusOnBoard(){
    };

    /**
     * Return the ghost speed at given Elroy value
     * Note : if elroyNumber is zero, this is the level ghost speed
     * @param elroyNumber, the ghost elroy value (0, 1 or 2)
     * @return the elroy ghost speed
     */
    public double getElroyGhostSpeed(int elroyNumber){
        return 0;
    };

    /**
     * Return the dot value (number of remaining dots) at which Blinky turns to elroy (1 or 2)
     * @param elroyNumber, 1 or 2
     * @return a positive number
     */
    public int getElroyDotValue(int elroyNumber) {
        return 0;
    };

    /**
     * Return the list of events that occured at the last frame
     * @return a list (might be empty)
     */
    public List<BoardEvent> getCurrentEvents(){
        return null;
    };

}
