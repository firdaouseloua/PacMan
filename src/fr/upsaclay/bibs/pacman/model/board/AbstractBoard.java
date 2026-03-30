package fr.upsaclay.bibs.pacman.model.board;

import fr.upsaclay.bibs.pacman.GameType;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.actors.*;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.TilePosition;

import java.util.List;


public abstract class AbstractBoard implements Board {
    private GameType gameType;

    protected Maze maze;
    protected PacMan pacMan;

    public AbstractBoard(GameType gameType) {
        this.gameType = gameType;
    }

    public GameType getGameType() {
        return gameType;
    }

    public Maze getMaze() {
        return maze;
    }

    public PacMan getPacMan() {
        return pacMan;
    }

     public static Board createBoard(GameType type) {
        switch (type) {
             case CLASSIC:
                 return new ClassicBoard();
             case TEST:
                 return new TestBoard();
             default:
                 throw new IllegalArgumentException("Unknown game type : " + type);
        }
     }
}
