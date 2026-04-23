package fr.upsaclay.bibs.pacman.bonus;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ticpactoe {
    int boardwidth = 600;
    int boardheight = 650;
    JFrame frame = new JFrame("Tic Pac Toe");
    JLabel timerLabel = new JLabel();
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel BoardPanel = new JPanel();
    JButton[][] board = new JButton[3][3];
    String player1 = "X";
    String player2 = "O";
    String currentPlayer = player1;
    
    int turns = 0;
    boolean gameOver = false;

    Timer timer;
    int timeLeft = 3;

    void TicTacToe(){
        frame.setSize(boardwidth, boardheight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());

        textLabel.setBackground(new Color(0xFF2121FF));
        textLabel.setForeground(Color.YELLOW);
        textLabel.setFont(new Font("Century Schoolbook", Font.BOLD | Font.ITALIC, 40));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Tic-Pac-Toe");
        textLabel.setOpaque(true);

        timerLabel.setBackground(new Color(0xFF2121FF));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Century Schoolbook", Font.BOLD, 20));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
        timerLabel.setText("Time left: " + timeLeft);
        timerLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel);
        textPanel.add(timerLabel, BorderLayout.SOUTH);
        frame.add(textPanel, BorderLayout.NORTH);

        BoardPanel.setLayout(new GridLayout(3, 3));
        BoardPanel.setBackground(new Color(0xFF2121FF));
        frame.add(BoardPanel, BorderLayout.CENTER);


        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameOver) {
                    timer.stop();
                    return;
                }
                
                timeLeft--;
                if (timeLeft > 0) {
                    timerLabel.setText("Thiiiiiink Faaaaaahst: " + timeLeft);
                } else {
                    timer.stop();
                    playRandomMove();
                    checkGameOver();
                    if (!gameOver) {
                        currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
                        textLabel.setText(currentPlayer + "'s turn");
                    }
                }
            }
        });
        timer.start();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JButton tileButton = new JButton();
                board[i][j] = tileButton;
                BoardPanel.add(tileButton);

                tileButton.setFont(new Font("Century Schoolbook", Font.BOLD, 100));
                tileButton.setBackground(Color.WHITE);
                tileButton.setForeground(Color.BLACK);
                tileButton.setFocusable(false);
                
                tileButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) { 
                        JButton tileButton = (JButton) e.getSource();
                        if (gameOver) {
                            return;
                        }
                        if (tileButton.getText().equals("")) {
                            tileButton.setText(currentPlayer);
                            turns++;
                            checkGameOver();

                            if (!gameOver) {
                                currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
                                textLabel.setText(currentPlayer + "'s turn");

                                timeLeft = 3;
                                timerLabel.setText("Thiiiiiink Faaaaaahst: " + timeLeft);
                                timer.restart();
                           } else {
                                timer.stop();
                            }
                        }
                    }
                });
            }
        }
    }

    void playRandomMove() {
        if (gameOver) {
            return;
        }

        List<int[]> emptyTiles = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j].getText().equals("")) {
                    emptyTiles.add(new int[]{i, j}); 
                }
            }
        }

        if (emptyTiles.isEmpty()) {
            return;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(emptyTiles.size());
        int[] chosenTile = emptyTiles.get(randomIndex);
        
        int row = chosenTile[0];
        int col = chosenTile[1];

        board[row][col].doClick();
    }

    void setWinnerTile(JButton tile) {
        tile.setBackground(new Color(0xFF2121FF));
        tile.setForeground(new Color(0xFFFFFF00));
        textLabel.setText(currentPlayer + " WoN !");
    }
    
    void setTie(JButton tile) {
        textLabel.setText("It's a draw?! or is it...");
        tile.setBackground(new Color(0xFF2121FF));
        tile.setForeground(new Color(0xFFFF0000));
    }

    void checkGameOver() {
        // Horizontal check
        for (int i = 0; i < 3; i++) {
            if (board[i][0].getText()==board[i][1].getText() && 
                board[i][1].getText()==board[i][2].getText() && 
                !board[i][0].getText().equals("")) {
                    for (int j = 0; j < 3; j++) {
                        setWinnerTile(board[i][j] );
                    }
                    gameOver = true;
                    return;
            }
        }
        // Vertical check
        for (int j = 0; j < 3; j++) {
            if (board[0][j].getText()==board[1][j].getText() && 
            board[1][j].getText()==board[2][j].getText() && 
            !board[0][j].getText().equals("")) {
                    for (int i = 0; i < 3; i++) {
                        setWinnerTile(board[i][j] );
                    }
                    gameOver = true;
                    return;
            }
        }
        // Diagonal check
        if (board[0][0].getText()==board[1][1].getText() && 
            board[1][1].getText()==board[2][2].getText() && 
            !board[0][0].getText().equals("")) {
                for (int i = 0; i < 3; i++) {
                    setWinnerTile(board[i][i]);
                }
                gameOver = true;
                return;
        }
        if (board[0][2].getText()==board[1][1].getText() && 
            board[1][1].getText()==board[2][0].getText() && 
            !board[0][2].getText().equals("")) {
                setWinnerTile(board[0][2]);
                setWinnerTile(board[1][1]);
                setWinnerTile(board[2][0]);
                gameOver = true;
                return;
            }

        if (turns == 9) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    setTie(board[i][j]);
                }
            }
            gameOver = true;
        }
    }
}
