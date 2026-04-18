package fr.upsaclay.bibs.pacman.view;

import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.control.Controller;
import fr.upsaclay.bibs.pacman.control.GameAction;
import fr.upsaclay.bibs.pacman.model.actors.Actor;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.Tile;
import fr.upsaclay.bibs.pacman.model.board.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Basic Swing implementation of the PacMan view.
 * It displays the maze as colored squares and uses a timer for NEXT_FRAME.
 */
public class BasicPacManView extends JFrame implements PacManView {

    private static final int PIXEL_SCALE_INTERFACE = 3;
    private static final int TIMER_DELAY_MS = 17;

    private final GamePanel gamePanel;
    private final JLabel statusLabel;
    private final Timer timer;

    private Board board;
    private Controller controller;
    private PacManLayout viewLayout;

    public BasicPacManView() {
        super("PacMan");
        this.gamePanel = new GamePanel();
        this.statusLabel = new JLabel("Press ENTER to start. Use arrows to move.");
        this.timer = new Timer(TIMER_DELAY_MS, e -> doNextFrame());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                onKeyPressed(e);
            }
        });

        pack();
    }

    @Override
    public void setBoard(Board board) {
        this.board = board;
        if (board != null) {
            gamePanel.updatePreferredSize();
            pack();
        }
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void initialize() {
        setLayoutState(PacManLayout.INIT);
        setVisible(true);
        requestFocusInWindow();
    }

    @Override
    public void setLayout(PacManLayout layout) {
        setLayoutState(layout);
    }

    @Override
    public void update() {
        gamePanel.repaint();
        updateStatusText();
    }

    private void setLayoutState(PacManLayout layout) {
        this.viewLayout = layout;
        setTitle("PacMan - " + layout);
        updateStatusText();

        if (layout == PacManLayout.GAME_ON) {
            if (!timer.isRunning()) {
                timer.start();
            }
        } else {
            timer.stop();
        }

        gamePanel.repaint();
    }

    private void updateStatusText() {
        if (statusLabel == null) {
            return;
        }

        if (viewLayout == PacManLayout.INIT) {
            statusLabel.setText("Press ENTER to start. Use arrow keys to choose direction.");
        } else if (viewLayout == PacManLayout.GAME_ON) {
            statusLabel.setText("Game on. Press P to pause.");
        } else if (viewLayout == PacManLayout.PAUSE) {
            statusLabel.setText("Paused. Press R to resume.");
        } else {
            statusLabel.setText("PacMan");
        }
    }

    private void doNextFrame() {
        if (controller == null || viewLayout != PacManLayout.GAME_ON) {
            return;
        }

        try {
            controller.receiveAction(GameAction.NEXT_FRAME);
        } catch (Exception e) {
            // ignore forbidden actions during timer ticks or log if helpful
            System.err.println("Timer action failed: " + e.getMessage());
        }
    }

    private void onKeyPressed(KeyEvent e) {
        if (controller == null) {
            return;
        }

        GameAction action = null;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                action = GameAction.UP;
                break;
            case KeyEvent.VK_DOWN:
                action = GameAction.DOWN;
                break;
            case KeyEvent.VK_LEFT:
                action = GameAction.LEFT;
                break;
            case KeyEvent.VK_RIGHT:
                action = GameAction.RIGHT;
                break;
            case KeyEvent.VK_ENTER:
                action = GameAction.START;
                break;
            case KeyEvent.VK_P:
                action = GameAction.PAUSE;
                break;
            case KeyEvent.VK_R:
                action = GameAction.RESUME;
                break;
        }

        if (action != null) {
            try {
                controller.receiveAction(action);
            } catch (Exception ex) {
                System.err.println("Controller action failed: " + ex.getMessage());
            }
        }
    }

    private class GamePanel extends JPanel {

        public GamePanel() throws PacManException{
            setBackground(Color.BLACK);
            updatePreferredSize();
            setFocusable(true);
        }

        public void updatePreferredSize() throws PacManException {
            if (board == null || board.getMaze() == null) {
                throw new PacManException("Board or Maze is null, cannot set preferred size");
            }
            Maze maze = board.getMaze();
            int width = maze.getPixelWidth() * PIXEL_SCALE_INTERFACE;
            int height = maze.getPixelHeight() * PIXEL_SCALE_INTERFACE;
            setPreferredSize(new Dimension(width, height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (board == null || board.getMaze() == null) {
                return;
            }

            Maze maze = board.getMaze();
            int tileSize = Maze.TILE_WIDTH * PIXEL_SCALE_INTERFACE;
            for (int line = 0; line < maze.getHeight(); line++) {
                for (int col = 0; col < maze.getWidth(); col++) {
                    Tile tile = maze.getTile(line, col);
                    int x = col * tileSize;
                    int y = line * tileSize;
                    if (tile.isWall()) {
                        g.setColor(Color.BLUE);
                        g.fillRect(x, y, tileSize, tileSize);
                    } else {
                        g.setColor(Color.BLACK);
                        g.fillRect(x, y, tileSize, tileSize);
                        if (tile.hasDot()) {
                            g.setColor(Color.WHITE);
                            int dotSize = Math.max(2, PIXEL_SCALE_INTERFACE * 2);
                            g.fillOval(x + tileSize / 2 - dotSize / 2,
                                       y + tileSize / 2 - dotSize / 2,
                                       dotSize, dotSize);
                        }
                    }
                }
            }

            Actor pacman = board.getPacMan();
            if (pacman != null) {
                g.setColor(Color.YELLOW);
                int x = pacman.getX() * PIXEL_SCALE_INTERFACE;
                int y = pacman.getY() * PIXEL_SCALE_INTERFACE;
                int size = Maze.TILE_WIDTH * PIXEL_SCALE_INTERFACE;
                g.fillOval(x, y, size, size);
            }
        }
    }
}
