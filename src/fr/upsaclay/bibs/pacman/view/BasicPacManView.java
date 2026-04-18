package fr.upsaclay.bibs.pacman.view;

import fr.upsaclay.bibs.pacman.control.Controller;
import fr.upsaclay.bibs.pacman.model.board.Board;

/**
 * Basic implementation of PacManView interface.
 * For now, a stub implementation for testing.
 */
public class BasicPacManView implements PacManView {

    private Board board;
    private Controller controller;
    private PacManLayout layout;

    @Override
    public void setBoard(Board board) {
        this.board = board;
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void initialize() {
        // Stub: initialize the view
        System.out.println("View initialized");
    }

    @Override
    public void setLayout(PacManLayout layout) {
        this.layout = layout;
        // Stub: update UI based on layout
        System.out.println("Layout set to: " + layout);
    }

    @Override
    public void update() {
        // Stub: refresh the display
        System.out.println("View updated");
    }
}