package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.maze.Maze;


public class ClassicBoard extends AbstractBoard {
    /**
     * Return the type of game of the board
     * Depending on the type, the maze or other initializations might be different
     * @return the game type
     */
    @Override
    public GameType getGameType(){
        return GameType.CLASSIC;
    };

    /**
     * Initialization of the board
     * (loads the maze, create and place the actors, etc.)
     * @throws PacManException in case something went wrong
     */
    @Override
    public void initialize() throws PacManException {
        try {
            this.maze = Maze.loadFromFile("/home/neorain/M1_bioinfo/Projet_Pacman/resources/maze.txt");
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
}
