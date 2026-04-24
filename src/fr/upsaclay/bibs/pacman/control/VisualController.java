package fr.upsaclay.bibs.pacman.control;

import java.util.Random;

import javax.swing.JFrame;
import fr.upsaclay.bibs.pacman.bonus.ticpactoe;
import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.bonus.ticpactoe;
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
    private boolean showdownOpened = false;

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
        showdownOpened = false;
    }

    @Override
    public void receiveAction(GameAction action) throws PacManException {
        BoardState stateBefore = getBoard() != null ? getBoard().getBoardState() : null;
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
                 * We reconnect the new board to the view and return to INIT if game over.
                 * If life lost but not game over, we return to the GAME_ON layout instantly.
                 */
                showdownOpened = false;
                view.setBoard(getBoard());
                if (stateBefore == BoardState.LIFE_OVER) {
                    view.setLayout(PacManLayout.GAME_ON);
                } else {
                    view.setLayout(PacManLayout.INIT);
                }
                
                view.update();
                break;

            case NEXT_LEVEL:
                /*
                 * Next level is initialized in the parent controller.
                 * We reconnect the board to the view and return to INIT.
                 */
                view.setBoard(getBoard());
                view.setLayout(PacManLayout.GAME_ON);
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
                    if (showdownOpened) {
                        view.setLayout(PacManLayout.GAME_OVER);
                        view.update();
                        break;
                    }
                    showdownOpened = true;
                    view.setLayout(PacManLayout.PAUSE);
                    getBoard().getSoundPlayer().stop();
                    ticpactoe duel = new ticpactoe(view instanceof JFrame ? (JFrame)view : null, getBoard().getMaze().getPixelWidth() * 3, getBoard().getMaze().getPixelHeight() * 3);
                    if (duel.isPacmanWon()) {
                        getBoard().setNumberOfLives(1); // PACMAN WON ?? LET HIM COOK 1 MORE TIME
                        getBoard().getSoundPlayer().play("resources/sounds/powerUp.wav");
                        showdownOpened = false;
                        super.initializeNewGame();
                        view.setBoard(getBoard());
                        view.setLayout(PacManLayout.GAME_ON);
                        view.update();
                    } 
                    else if (duel.isShowdownDraw()) {
                        if (getBoard().getLevel() < 5) { // HO MY LORD PRAY GACHA GOD FOR THIS
                            getBoard().setNumberOfLives(1);
                            getBoard().startActors();
                            //getBoard().teleportPacManToRandomEmptyTile();
                            getBoard().getSoundPlayer().play("resources/sounds/powerUp.wav");
                            view.setLayout(PacManLayout.GAME_ON);
                            view.update();
                        }
                        else if (getBoard().getLevel() >= 5 && getBoard().getLevel() < 10) {
                            Random rand = new Random();
                            int x = rand.nextInt(2);
                            if (x == 0) {
                                getBoard().setNumberOfLives(1);
                                getBoard().getSoundPlayer().play("resources/sounds/powerUp.wav");
                                super.initializeNewGame();
                                view.setLayout(PacManLayout.GAME_ON);
                            }
                            else {
                                view.setLayout(PacManLayout.GAME_OVER);
                            }
                        } else {
                            view.setLayout(PacManLayout.GAME_OVER);
                        }
                    }
                    else {
                        view.setLayout(PacManLayout.GAME_OVER);
                        view.update();
                    }
                    break;

                default:
                    break;
            }
        }
    }
}