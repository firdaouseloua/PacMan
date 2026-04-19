package fr.upsaclay.bibs.pacman.view;

import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.control.Controller;
import fr.upsaclay.bibs.pacman.control.ForbiddenActionException;
import fr.upsaclay.bibs.pacman.control.GameAction;
import fr.upsaclay.bibs.pacman.model.actors.Actor;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.Tile;
import fr.upsaclay.bibs.pacman.model.board.Board;

import javax.swing.*;
import javax.swing.border.BorderFactory;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic Swing implementation of the PacMan view.
 * It displays the maze as colored squares and uses a timer for NEXT_FRAME.
 */
public class BasicPacManView extends JFrame implements PacManView {

    private static final int PIXEL_SCALE_INTERFACE = 3;
    private static final int TIMER_DELAY_MS = 17;

    private final GamePanel gamePanel;
    private final Timer timer;

    private Board board;
    private Controller controller;
    private PacManLayout viewLayout;

    public BasicPacManView() throws PacManException {
        super("PacMan");
        this.gamePanel = new GamePanel();
        this.timer = new Timer(TIMER_DELAY_MS, e -> {
            try {
                doNextFrame();
            } catch (PacManException e1) {
                System.err.println("Error processing next frame: " + e1.getMessage());
                e1.printStackTrace();
            }
        });
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setLayout(new BorderLayout());
        
        JPanel titleBar = createTitleBar();
        add(titleBar, BorderLayout.NORTH);
        add(gamePanel, BorderLayout.CENTER);        
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                    try {
                        onKeyPressed(e);
                    } catch (PacManException e1) {
                        System.err.println("Error processing key event: " + e1.getMessage());
                        e1.printStackTrace();
                    }
            }
        });
        pack();
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(Color.WHITE);
        titleBar.setPreferredSize(new Dimension(0, 30));
        
        JLabel titleLabel = new JLabel("PacMan");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        titleBar.add(titleLabel, BorderLayout.WEST);
        
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 18));
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(true);
        closeButton.setBackground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.addActionListener(e -> System.exit(0));
        titleBar.add(closeButton, BorderLayout.EAST);
        
        return titleBar;
    }

    @Override
    public void setBoard(Board board) throws PacManException {
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
        setLocationRelativeTo(null);
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

        if (layout == PacManLayout.GAME_ON) {
            if (!timer.isRunning()) {
                timer.start();
            }
        } else {
            timer.stop();
        }

        gamePanel.repaint();
    }

    private String getStatusText() {
        if (viewLayout == PacManLayout.INIT) {
            return "Press ENTER to start";
        } else if (viewLayout == PacManLayout.GAME_ON) {
            return "P: Pause";
        } else if (viewLayout == PacManLayout.PAUSE) {
            return "R: Resume";
        }
        return "";
    }

    private void doNextFrame() throws PacManException {
        if (controller == null || viewLayout != PacManLayout.GAME_ON) {
            return;
        }

        try {
            controller.receiveAction(GameAction.NEXT_FRAME);
        } catch (PacManException e) {
            throw new PacManException(e);
        }
    }

    private void onKeyPressed(KeyEvent e) throws PacManException {
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
            } catch (ForbiddenActionException error) {
                throw new ForbiddenActionException(action);
            }
        }
    }

    private class GamePanel extends JPanel {
        private final Map<Tile, BufferedImage> tileTextures = new EnumMap<>(Tile.class);

        public GamePanel(){
            setBackground(Color.BLACK);
            setPreferredSize(new Dimension(400, 600)); 
            setFocusable(true);
            loadTileTextures();
        }

        private void loadTileTextures() {
            for (Tile tile : Tile.values()) {
                try {
                    String resourcePath = "/tiles/" + tile.name() + ".txt";
                    InputStream input = getClass().getResourceAsStream(resourcePath);
                    if (input != null) {
                        BufferedImage texture = parseTileTexture(input);
                        tileTextures.put(tile, texture);
                        input.close();
                    }
                } catch (IOException e) {
                    System.err.println("Failed to load texture for tile " + tile.name() + ": " + e.getMessage());
                }
            }
        }

        private BufferedImage parseTileTexture(InputStream input) throws IOException {
            List<String> lines = new BufferedReader(new InputStreamReader(input)).lines().filter(l -> !l.isBlank()).collect(Collectors.toList());
            
            if (lines.isEmpty()) {
                return null;
            }

            int size = lines.size();
            BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < lines.size(); y++) {
                String[] cells = lines.get(y).trim().split("\\s+");
                for (int x = 0; x < cells.length && x < size; x++) {
                    try {
                        int value = Integer.parseInt(cells[x]);
                        int color = value == 1 ? Color.BLUE.getRGB() : 0x00000000; // 0 = transparent
                        img.setRGB(x, y, color);
                    } catch (NumberFormatException e) {
                        img.setRGB(x, y, 0x00000000); // Default to transparent on parse error
                    }
                }
            }
            return img;
        }

        public void updatePreferredSize() {
            if (board == null || board.getMaze() == null) {
                return; // Skip if board not ready yet
            }
            Maze maze = board.getMaze();
            int width = maze.getPixelWidth() * PIXEL_SCALE_INTERFACE;
            int height = maze.getPixelHeight() * PIXEL_SCALE_INTERFACE;
            setPreferredSize(new Dimension(width, height));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Draw start page if in INIT state
            if (viewLayout == PacManLayout.INIT) {
                drawStartPage(g);
                return;
            }
            
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
                    // Draw texture if available
                    BufferedImage texture = tileTextures.get(tile);
                    if (texture != null) {
                        g.drawImage(texture, x, y, tileSize, tileSize, null);
                    } else {
                        // Fallback to solid color if texture not loaded
                        if (tile.isWall()) {
                            g.setColor(Color.blue);
                            g.fillRect(x, y, tileSize, tileSize);
                        } else {
                            g.setColor(Color.BLACK);
                            g.fillRect(x, y, tileSize, tileSize);
                        }
                    }
                    
                    // Draw dots if present
                    if (tile.hasDot()) {
                        g.setColor(Color.WHITE);
                        int dotSize = Math.max(2, PIXEL_SCALE_INTERFACE * 2);
                        g.fillOval(x + tileSize / 2 - dotSize / 2,
                                   y + tileSize / 2 - dotSize / 2,
                                   dotSize, dotSize);
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
            
            // Draw status text in top-right
            drawStatusText(g);;
        }
        
        private void drawStatusText(Graphics g) {
            String status = getStatusText();
            if (status == null || status.isEmpty()) {
                return;
            }
            
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();
            int x = getWidth() - fm.stringWidth(status) - 10;
            int y = 20;
            g.drawString(status, x, y);
        }

        private void drawStartPage(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            String title = "PAC-MAN";
            FontMetrics fm = g.getFontMetrics();
            int titleX = (width - fm.stringWidth(title)) / 2;
            int titleY = height / 3;
            g.drawString(title, titleX, titleY);
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            String instruction = "Press ENTER to Start";
            fm = g.getFontMetrics();
            int instrX = (width - fm.stringWidth(instruction)) / 2;
            int instrY = height * 2 / 3;
            g.drawString(instruction, instrX, instrY);
        }
    }
}
