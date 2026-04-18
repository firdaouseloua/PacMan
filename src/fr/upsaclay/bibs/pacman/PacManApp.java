package fr.upsaclay.bibs.pacman;

import fr.upsaclay.bibs.pacman.control.Controller;
import fr.upsaclay.bibs.pacman.control.InterfaceMode;

import javax.swing.*;

/**
 * The main application
 */
public class PacManApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Controller controller = Controller.getController(InterfaceMode.VISUAL);
            try {
                controller.initialize();
            } catch (PacManException e) {
                e.printStackTrace();
            }
        });
    }
}
