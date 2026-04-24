package fr.upsaclay.bibs.pacman.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import fr.upsaclay.bibs.pacman.PacManException;
import fr.upsaclay.bibs.pacman.control.Controller;
import fr.upsaclay.bibs.pacman.control.ForbiddenActionException;
import fr.upsaclay.bibs.pacman.control.GameAction;
import fr.upsaclay.bibs.pacman.model.actors.Actor;
import fr.upsaclay.bibs.pacman.model.actors.Ghost;
import fr.upsaclay.bibs.pacman.model.actors.GhostState;
import fr.upsaclay.bibs.pacman.model.actors.GhostType;
import fr.upsaclay.bibs.pacman.model.board.Board;
import fr.upsaclay.bibs.pacman.model.board.BoardState;
import fr.upsaclay.bibs.pacman.model.maze.Maze;
import fr.upsaclay.bibs.pacman.model.maze.Tile;
import fr.upsaclay.bibs.pacman.model.actors.Bonus;
import fr.upsaclay.bibs.pacman.model.actors.BonusType;


public class BasicPacManView extends JFrame implements PacManView {
    private boolean endSfxPlayed = false; // a lock for sound looping purpose yes sound only... my thirteen reason why <-----
    private static final int TIMER_DELAY_MS = 17;
    private static final int SCALE = 3; 
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
        titleLabel.setFont(new Font("Century Schoolbook", Font.BOLD, 14));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        titleBar.add(titleLabel, BorderLayout.WEST);
        
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Century Schoolbook", Font.BOLD, 18));
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(true);
        closeButton.setBackground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createEmptyBorder());
        closeButton.addActionListener(e -> System.exit(0));
        titleBar.add(closeButton, BorderLayout.EAST);
        
        return titleBar;
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
                this.endSfxPlayed = false;
                break;
            case KeyEvent.VK_P:
                action = GameAction.PAUSE;
                break;
            case KeyEvent.VK_R:
                action = GameAction.RESUME;
                break;
            case KeyEvent.VK_N:
                action = GameAction.NEW_GAME;
                this.endSfxPlayed = false;
                break;
            case KeyEvent.VK_L:
                if (board != null && board.getBoardState() == BoardState.LEVEL_OVER) {
                    action = GameAction.NEXT_LEVEL;
                    this.endSfxPlayed = false; 
                }
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

    @Override
    public void setBoard(Board board) throws PacManException {
        this.board = board;
        if (board != null) {
            gamePanel.updatePreferredSize();
            gamePanel.initGridTextures();
            pack();
            setLocationRelativeTo(null);
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

    private void updateStatusText() {
        SwingUtilities.invokeLater(() -> gamePanel.repaint());
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

        if (board != null) {
            BoardState state = board.getBoardState();

            if (state == BoardState.LIFE_OVER) {
                return "Press N to continue";
            }
            if (state == BoardState.GAME_OVER) {
                return "Game over";
            }
        }

        if (viewLayout == PacManLayout.INIT) {
                return "Press ENTER to start";
        } else if (viewLayout == PacManLayout.GAME_ON) {
            return "Pause is possible : Press P";
        } else if (viewLayout == PacManLayout.PAUSE) {
            return "Resume is required : Press R";
        }
        return "";
        }

    private void doNextFrame() throws PacManException {
        if (controller == null || viewLayout != PacManLayout.GAME_ON) {
            return;
        }
        try {
            if (board != null && board.getBoardState() == BoardState.STARTED) {
                controller.receiveAction(GameAction.NEXT_FRAME);
            }

        if (board != null) {
                    BoardState currentState = board.getBoardState();
                    if (!endSfxPlayed && (currentState == BoardState.GAME_OVER || 
                                            currentState == BoardState.LEVEL_OVER || 
                                            currentState == BoardState.LIFE_OVER)) {
                                                endSfxPlayed = true; 
                                                timer.stop();
                                                board.getSoundPlayer().stop();

                                                if (currentState == BoardState.GAME_OVER) {
                                                    board.getSoundPlayer().play("resources/sounds/get_a_life.wav");
                                                } else if (currentState == BoardState.LEVEL_OVER) {
                                                    board.getSoundPlayer().play("resources/sounds/thank_you_next.wav");
                                                } else {
                                                    board.getSoundPlayer().play("resources/sounds/explosion.wav");
                                                }
                                                
                                                gamePanel.repaint(); 
                                            }
                }
            } catch (PacManException e) {
                throw new PacManException(e);
            }
        }

// HELL is here ...
private class GamePanel extends JPanel {
        
    private final Map<Tile, BufferedImage> tileTextures = new EnumMap<>(Tile.class);
    private final Map<String, BufferedImage> actorTextures = new HashMap<>();
    private BufferedImage[][] gridTextures; 

    public GamePanel() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(1200, 1800));
        setFocusable(true);
        loadTileTextures();
        loadActorTextures();
    }

    private void loadActorTextures() {

        Map<String, List<Integer>> ghost = new HashMap<>();
        ghost.put("BLINKY", List.of(0x00000000, 0xFFFF0000, 0xFFFFFFFF, 0xFF2121DE));
        ghost.put("PINKY", List.of(0x00000000, 0xFFFFB8FF, 0xFFFFFFFF, 0xFFDE21DE));
        ghost.put("INKY", List.of(0x00000000, 0xFF00FFFF, 0xFFFFFFFF, 0xFF2121DE));
        ghost.put("CLYDE", List.of(0x00000000, 0xFFFFB851, 0xFFFFFFFF, 0xFF2121DE));
        ghost.put("FRIGHTENED", List.of(0x00000000, 0xFF2121FF, 0xFFFFB8AE, 0xFFFFB8AE));
        String[] actorsDir = {"UP", "DOWN", "LEFT", "RIGHT", "FRIGHTENED"};

        for (int i = 0; i < actorsDir.length - 1; i++) {
            String dir = actorsDir[i]; 
            String path = "resources/pacman/" + dir + ".txt"; 
            File file = new File(path);
            if (file.exists()) {
                try (InputStream input = new FileInputStream(file)) {
                    BufferedImage img = parseTexture(input, List.of(0x00000000, 0xFFFFFF00), 13);
                    actorTextures.put("PACMAN_" + dir, img);
                } catch (IOException e) {
                    System.err.println("Error on PacMan " + dir + ": " + e.getMessage());
                }
            } else {
                System.err.println("File not found : " + file.getAbsolutePath());
            }
        }

        for (Map.Entry<String, List<Integer>> entry : ghost.entrySet()) {
            String ghostName = entry.getKey();
            List<Integer> palette = entry.getValue();

            for (String dir : actorsDir) {
                if (ghostName.equals("FRIGHTENED") != dir.equals("FRIGHTENED")) continue;
                String path = "resources/ghosts/" + dir + ".txt";
                File file = new File(path);
                if (file.exists()) {
                    try (InputStream input = new FileInputStream(file)) {
                        BufferedImage img = parseTexture(input, palette, 16);
                        actorTextures.put(ghostName + "_" + dir, img);
                    } catch (IOException e) {
                        System.err.println("Error loading " + ghostName + " " + dir + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("File not found : " + file.getAbsolutePath());
                }
            }
        }
    }

    private BufferedImage parseTexture(InputStream input, List<Integer> colors, int size) throws IOException {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            int y = 0;
            
            while ((line = reader.readLine()) != null && y < size) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] cells = line.split("\\s+");

                for (int x = 0; x < size && x < cells.length; x++) {
                    try {
                        int colorIndex = Integer.parseInt(cells[x]);
                        img.setRGB(x, y, colors.get(colorIndex));
                    } catch (NumberFormatException e) {
                        System.err.println("Error loading actor texture :" + e.getMessage());
                    }
                }
                y++;
            }
        }
        return img;
    }
 
    private void loadTileTextures() {
        for (Tile tile : Tile.values()) {
            String relativePath = "resources/tiles/" + tile.name().toUpperCase() + ".txt";
            File file = new File(relativePath);
            
            if (file.exists()) {
                try (InputStream input = new FileInputStream(file)) {
                    BufferedImage texture = parseTileTexture(input);
                    tileTextures.put(tile, texture);
                } catch (IOException e) {
                    System.err.println("Error loading " + tile.name() + ": " + e.getMessage());
                }
            } else {
                System.err.println("File not found : " + file.getAbsolutePath());
            }
        }
    }

    private BufferedImage parseTileTexture(InputStream input) throws IOException {
        BufferedImage img = new BufferedImage(Maze.TILE_WIDTH, Maze.TILE_WIDTH, BufferedImage.TYPE_INT_ARGB);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String line;
            int y = 0;
            while ((line = reader.readLine()) != null && y < Maze.TILE_WIDTH) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] cells = line.split("\\s+");
                for (int x = 0; x < Maze.TILE_WIDTH && x < cells.length; x++) {
                    try {
                        int value = Integer.parseInt(cells[x]);
                        int color = (value == 1) ? 0xFF0000FF : 0x00000000; 
                        img.setRGB(x, y, color);
                    } catch (NumberFormatException e) {
                        img.setRGB(x, y, 0x00000000);
                    }
                }
                y++;
            }
        }
        return img;
    }

    public void initGridTextures() {
        if (board == null || board.getMaze() == null) return;
        
        Maze maze = board.getMaze();
        int rows = maze.getHeight();
        int cols = maze.getWidth();
        
        gridTextures = new BufferedImage[rows][cols];
        
        for (int line = 0; line < rows; line++) {
            for (int col = 0; col < cols; col++) {
                Tile tile = maze.getTile(line, col);
                gridTextures[line][col] = tileTextures.get(tile);
            }
        }
    }

    public void updatePreferredSize() {
        if (board == null || board.getMaze() == null) {
            return; 
        }
        Maze maze = board.getMaze();
        int width = maze.getPixelWidth() * SCALE;
        int height = maze.getPixelHeight() * SCALE;
        setPreferredSize(new Dimension(width, height));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(SCALE, SCALE);
        
        if (viewLayout == PacManLayout.INIT) {
            drawStartPage(g2d);
            return;
        }

        if (board != null && board.getBoardState() == BoardState.GAME_OVER) {
            drawGameOverPage(g2d);
            return; 
        }

        if (board != null && board.getBoardState() == BoardState.LEVEL_OVER) {
            drawLevelComplete(g2d);
            return; 
        }

        
        if (board == null || board.getMaze() == null) {
            return;
        }

        Maze maze = board.getMaze();
        int tileSize = Maze.TILE_WIDTH;

        if (gridTextures != null) {                    
            for (int line = 0; line < gridTextures.length; line++) {
                for (int col = 0; col < gridTextures[line].length; col++) {
                    int x = col * tileSize;
                    int y = line * tileSize;
                    Tile tile = maze.getTile(line, col);
                    
                    if (tile.isWall()) {
                        BufferedImage texture = gridTextures[line][col];
                        if (texture != null) {
                            g2d.drawImage(texture, x, y, tileSize, tileSize, null);
                        } else {
                            g2d.setColor(Color.BLUE);
                            g2d.fillRect(x, y, tileSize, tileSize);
                        }
                    } 
                    else if (tile.hasDot()) {
                        if (tile == Tile.BD) {
                            g2d.setColor(new Color(0xFF2121FF)); 
                            int bigDotSize = 5; 
                            g2d.fillOval(x + tileSize / 2 - bigDotSize / 2, 
                                        y + tileSize / 2 - bigDotSize / 2, 
                                        bigDotSize, bigDotSize);
                        } else {
                            g2d.setColor(Color.WHITE);
                            int dotSize = 2; 
                            g2d.fillOval(x + tileSize / 2 - dotSize / 2, 
                                        y + tileSize / 2 - dotSize / 2, 
                                        dotSize, dotSize);
                        }
                    }
                }
            }
        }

        Actor pacman = board.getPacMan();

        if (pacman != null) {
            String dir = pacman.getDirection().toString().toUpperCase(); 
            BufferedImage img = actorTextures.get("PACMAN_" + dir);
            if (img == null) img = actorTextures.get("PACMAN_RIGHT"); 
            
            if (img != null) {
                int drawX = pacman.getX() - (img.getWidth() / 2);
                int drawY = pacman.getY() - (img.getHeight() / 2);

                boolean isMouthOpen = (System.currentTimeMillis() / 150) % 2 == 0;

                if (isMouthOpen) {
                    g2d.drawImage(img, drawX, drawY, null);
                } else {
                    g2d.setColor(new Color(0xFFFFFF00));
                    g2d.fillOval(drawX, drawY, img.getWidth(), img.getHeight());
                }
            }
        }

        Bonus bonus = board.getCurrentBonus();

        if (bonus != null && bonus.isActive()) {
            int bx = bonus.getX();
            int by = bonus.getY();

            switch (bonus.getBonusType()) {
                case CHERRY:
                    g2d.setColor(Color.RED);
                    g2d.fillOval(bx - 5, by - 4, 6, 6);
                    g2d.fillOval(bx, by - 4, 6, 6);
                    g2d.setColor(Color.GREEN);
                    g2d.drawLine(bx, by - 4, bx + 3, by - 9);
                    break;

                case STRAWBERRY:
                    g2d.setColor(Color.RED);
                    g2d.fillOval(bx - 5, by - 5, 10, 10);
                    g2d.setColor(Color.GREEN);
                    g2d.fillRect(bx - 2, by - 8, 4, 3);
                    break;

                case PEACH:
                    g2d.setColor(new Color(0xFFFFB070));
                    g2d.fillOval(bx - 6, by - 6, 12, 12);
                    break;

                case APPLE:
                    g2d.setColor(Color.RED);
                    g2d.fillOval(bx - 6, by - 6, 12, 12);
                    g2d.setColor(Color.GREEN);
                    g2d.fillRect(bx - 1, by - 9, 3, 4);
                    break;

                case GRAPES:
                    g2d.setColor(new Color(0xAA55FF));
                    g2d.fillOval(bx - 5, by - 5, 5, 5);
                    g2d.fillOval(bx, by - 5, 5, 5);
                    g2d.fillOval(bx - 2, by, 5, 5);
                    break;

                case GALAXIAN:
                    g2d.setColor(Color.YELLOW);
                    g2d.fillPolygon(
                        new int[]{bx, bx - 7, bx + 7},
                        new int[]{by - 7, by + 6, by + 6},
                        3
                    );
                    break;

                case BELL:
                    g2d.setColor(Color.YELLOW);
                    g2d.fillArc(bx - 6, by - 6, 12, 12, 0, 180);
                    g2d.fillRect(bx - 6, by, 12, 5);
                    break;

                case KEY:
                    g2d.setColor(Color.YELLOW);
                    g2d.fillOval(bx - 7, by - 3, 6, 6);
                    g2d.fillRect(bx - 1, by - 1, 10, 2);
                    g2d.fillRect(bx + 5, by - 1, 2, 5);
                    break;
            }
        }

        for (Ghost ghost : board.getGhosts()) {
            if (ghost != null) {
                String key;
                if (ghost.getGhostState() == GhostState.FRIGHTENED) {
                    key = "FRIGHTENED_FRIGHTENED";
                } 
                else if (ghost.getGhostState() == GhostState.FRIGHTENED_END) {
                    boolean blink = (System.currentTimeMillis() / 200) % 2 == 0;
                    if (blink) {
                        key = "FRIGHTENED_FRIGHTENED"; 
                    } 
                    else {
                        key = ghost.getGhostType().toString().toUpperCase() + "_" + ghost.getDirection().toString().toUpperCase();
                    }
                } 
                else {
                    key = ghost.getGhostType().toString().toUpperCase() + "_" + ghost.getDirection().toString().toUpperCase();
                }

            BufferedImage img = actorTextures.get(key);
                if (img != null) {
                    int drawX = ghost.getX() - (img.getWidth() / 2);
                    int drawY = ghost.getY() - (img.getHeight() / 2);

                    g2d.drawImage(img, drawX, drawY, null);
                }
            }
        }

        if (board != null && board.getBoardState() == BoardState.LIFE_OVER) {
            drawLifeOver(g2d);
        }

        drawStatusText(g2d);
    }
    
    private void drawStatusText(Graphics2D g) {
        int width = getWidth() / SCALE;
        int height = getHeight() / SCALE;
        String status = getStatusText();
        if (status == null || status.isEmpty()) {
            return;
        }
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Century Schoolbook", Font.BOLD, 5));
        FontMetrics fm = g.getFontMetrics();
        int x = width - fm.stringWidth(status) - 5;
        int y = 15;
        g.drawString(status, x, y);

        drawLives(g, 7, 10);

        g.setFont(new Font("Century Schoolbook", Font.BOLD, 5));
        g.setColor(Color.WHITE);
        String scoreText = "Score : " + (board != null ? board.getScore() : 0);
        int scoreX = 7;
        int scoreY = 20;

        g.drawString(scoreText, scoreX, scoreY);


        g.setFont(new Font("Century Schoolbook", Font.BOLD, 8));
        String level = "Level " + (board != null ? board.getLevel() : 0);
        FontMetrics fm_level = g.getFontMetrics();
        int levelX = (width - fm_level.stringWidth(level))/2;
        int levelY = 15;

        GradientPaint levelGradient = new GradientPaint(
        levelX + fm_level.stringWidth(level), levelY - fm_level.getAscent() , new Color(0x2121FF), 
        levelX, levelY, new Color(0xFFFF00));
        g.setPaint(levelGradient);
        g.drawString(level, levelX, levelY);
    }

    private void drawLives(Graphics2D g, int startX, int y) {
    if (board == null) return;

    int remainingLives = board.getNumberOfLives() + 1;
    int maxLives = 3;

    g.setFont(new Font("Century Schoolbook", Font.BOLD, 5));
    g.setColor(Color.YELLOW);
    String label = "Lives Left :";
    g.drawString(label, startX, y);

    FontMetrics fm = g.getFontMetrics();
    int x = startX + fm.stringWidth(label) + 5;

    g.setFont(new Font("Dialog", Font.BOLD, 7));

    for (int i = 0; i < maxLives; i++) {
        int hx = x + i * 8;

        if (i < remainingLives) {
            g.setColor(Color.RED);
            g.drawString("♥", hx, y);
        } else {
            g.setColor(Color.GRAY);
            g.drawString("♥", hx, y);

            // cassure du cœur
            g.setColor(Color.BLACK);
            g.drawLine(hx + 2, y - 6, hx + 4, y - 3);
            g.drawLine(hx + 4, y - 3, hx + 2, y);
        }
    }
}

    private void drawStartPage(Graphics2D g) {
        int width = getWidth() / SCALE;
        int height = getHeight() / SCALE;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Century Schoolbook", Font.BOLD, 16));
        String title = "PAC-MAN";
        FontMetrics fm = g.getFontMetrics();
        int titleX = (width - fm.stringWidth(title)) / 2;
        int titleY = height / 2 - 20;
        g.drawString(title, titleX, titleY);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Century Schoolbook", Font.BOLD | Font.ITALIC, 16));
        String instruction = "Press ENTER to Start";
        fm = g.getFontMetrics();
        int instrX = (width - fm.stringWidth(instruction)) / 2;
        int instrY = height / 2 + 20;
        g.drawString(instruction, instrX, instrY);
    }

    private void drawLifeOver(Graphics2D g) {
            String text = "LIFE OVER!";
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Century Schoolbook", Font.BOLD, 12));
            
            FontMetrics fm = g.getFontMetrics();
            int width = getWidth() / SCALE;
            int height = getHeight() / SCALE;
            
            int x = (width - fm.stringWidth(text)) / 2;
            int y = height / 2;
            
            g.setColor(Color.BLACK);
            g.fillRect(x - 5, y - 12, fm.stringWidth(text) + 10, 16);
            
            g.setColor(Color.YELLOW);
            g.drawString(text, x, y);
        }


    private void drawGameOverPage(Graphics2D g) {
        int width = getWidth() / SCALE;
        int height = getHeight() / SCALE;
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.RED);
        g.setFont(new Font("Century Schoolbook", Font.BOLD, 16));
        String title = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        int titleX = (width - fm.stringWidth(title)) / 2;
        int titleY = height / 3;
        g.drawString(title, titleX, titleY);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Century Schoolbook", Font.BOLD, 12));
        String scoreText = "Final Score: " + (board != null ? board.getScore() : 0);
        fm = g.getFontMetrics();
        int scoreX = (width - fm.stringWidth(scoreText)) / 2;
        int scoreY = height / 2;
        g.drawString(scoreText, scoreX, scoreY);
    }

        private void drawLevelComplete(Graphics2D g) {
        int width = getWidth() / SCALE;
        int height = getHeight() / SCALE;
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.RED);
        g.setFont(new Font("Century Schoolbook", Font.BOLD, 16));
        String title = "You WON!... Maybe?";
        FontMetrics fm = g.getFontMetrics();
        int titleX = (width - fm.stringWidth(title)) / 2;
        int titleY = height / 3 + 10;
        g.drawString(title, titleX, titleY);

        String level_message = "Press L To Continue";
        g.setFont(new Font("Century Schoolbook", Font.BOLD | Font.ITALIC, 14));
        FontMetrics fm_level = g.getFontMetrics();
        int levelX = (width - fm_level.stringWidth(level_message)) / 2;
        int levelY = height / 2 + 40;

        GradientPaint levelGradient = new GradientPaint(
            levelX, levelY - fm_level.getAscent(), new Color(0xFFFF00), 
            levelX, levelY,                        new Color(0x2121FF)  
        );

        g.setPaint(levelGradient);
        g.drawString(level_message, levelX, levelY);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Century Schoolbook", Font.BOLD, 12));
        String scoreText = "Score: " + (board != null ? board.getScore() : 0);
        fm = g.getFontMetrics();
        int scoreX = (width - fm.stringWidth(scoreText)) / 2;
        int scoreY = height / 2;
        g.drawString(scoreText, scoreX, scoreY);
    }
}

}
