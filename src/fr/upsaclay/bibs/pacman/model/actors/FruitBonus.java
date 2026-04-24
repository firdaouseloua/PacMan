package fr.upsaclay.bibs.pacman.model.actors;

import fr.upsaclay.bibs.pacman.model.Direction;
import fr.upsaclay.bibs.pacman.model.board.Board;

public class FruitBonus extends AbstractActor implements Bonus {

    private final BonusType bonusType;
    private boolean active;

    public FruitBonus(Board board, int x, int y, BonusType bonusType) {
        super(board, x, y, Direction.LEFT);
        this.bonusType = bonusType;
        this.active = true;
    }

    @Override
    public ActorType getType() {
        return ActorType.BONUS;
    }

    @Override
    public void start() {
        this.active = true;
        this.realX = x;
        this.realY = y;
    }

    @Override
    public void nextMove() {
        // Bonus immobile
    }

    @Override
    public BonusType getBonusType() {
        return bonusType;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}