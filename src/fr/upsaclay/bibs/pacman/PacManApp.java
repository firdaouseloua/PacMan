package fr.upsaclay.bibs.pacman;

import fr.upsaclay.bibs.pacman.control.Controller;
import fr.upsaclay.bibs.pacman.control.InterfaceMode;

import javax.swing.*;

/**
 * The main application
 */
public class PacManApp {

    public static void main(String[] args) throws PacManException {
        SwingUtilities.invokeLater(() -> {
            Controller controller;
            try {
                controller = Controller.getController(InterfaceMode.VISUAL);
                controller.initialize();
            } catch (PacManException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
