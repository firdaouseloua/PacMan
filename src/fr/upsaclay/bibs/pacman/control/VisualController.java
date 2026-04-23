package fr.upsaclay.bibs.pacman.control;

import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.model.board.BoardState;
import fr.upsaclay.bibs.pacman.view.BasicPacManView;
import fr.upsaclay.bibs.pacman.view.PacManLayout;
import fr.upsaclay.bibs.pacman.view.PacManView;

/**
 * Visual controller with view, extends SimpleController.
 * Main role:
 * - initialize the graphical view
 * - update the current layout depending on actions and board state
 * - keep model and view synchronized
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
                /*
                 * START has already started actors in the parent controller.
                 * Here we only switch the layout to gameplay and refresh.
                 */
                view.setLayout(PacManLayout.GAME_ON);
                view.update();
                break;
            case PAUSE:
                view.setLayout(PacManLayout.PAUSE);
                view.update();
                break;
            case RESUME:
                view.setLayout(PacManLayout.GAME_ON);
                view.update();
                break;
            case NEXT_FRAME:
                view.update();
                break;
            case NEW_GAME:
                /*
                 * A new game is created in the parent controller.
                 * We reconnect the new board to the view and return to INIT.
                 */
                view.setBoard(getBoard());
                view.setLayout(PacManLayout.INIT);
                view.update();
                break;

            case NEXT_LEVEL:
                /*
                 * Next level is initialized in the parent controller.
                 * We reconnect the board to the view and return to INIT.
                 */
                view.setBoard(getBoard());
                view.setLayout(PacManLayout.INIT);
                view.update();
                break;
            default:
                break;
        }
        /*
         * Then react to the current board state after the action.
         * This keeps the view synchronized even when the board state changes as a consequence of NEXT_FRAME.
         */
        if (getBoard() != null) {
            BoardState state = getBoard().getBoardState();

            switch (state) {
                case LIFE_OVER:
                    view.setLayout(PacManLayout.LIFE_OVER);
                    view.update();
                    break;

                case LEVEL_OVER:
                    view.setLayout(PacManLayout.LEVEL_OVER);
                    view.update();
                    break;

                case GAME_OVER:
                    view.setLayout(PacManLayout.GAME_OVER);
                    view.update();
                    break;

                default:
                    break;
            }
        }
    }
}