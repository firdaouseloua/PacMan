package fr.upsaclay.bibs.pacman.control;

import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.view.BasicPacManView;
import fr.upsaclay.bibs.pacman.view.PacManLayout;
import fr.upsaclay.bibs.pacman.view.PacManView;

/**
 * Visual controller with view, extends SimpleController.
 * Adds view management for VISUAL mode.
 */
public class VisualController extends SimpleController {

    private PacManView view;

    public VisualController() throws PacManException{
        super();
        this.view = new BasicPacManView();
    }

    @Override
    public void initialize() throws PacManException {
        super.initialize();
        view.setController(this);
        view.setBoard(getBoard());
        view.initialize();
        view.setLayout(PacManLayout.INIT);
    }

    @Override
    public void initializeNewGame() throws PacManException {
        super.initializeNewGame();
        view.setBoard(getBoard());
        view.setLayout(PacManLayout.GAME_ON);
        view.update();
    }

    @Override
    public void receiveAction(GameAction action) throws PacManException {
        super.receiveAction(action);
        switch (action) {
            case START:
                view.setLayout(PacManLayout.GAME_ON);
                initializeNewGame();
                break;
            case PAUSE:
                view.setLayout(PacManLayout.PAUSE);
                break;
            case RESUME:
                view.setLayout(PacManLayout.GAME_ON);
                break;
            case NEXT_FRAME:
                view.update();
                break;
            default:
                break;
        }
    }
}