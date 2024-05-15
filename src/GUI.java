import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import javax.swing.*;

public class GUI extends javax.swing.JFrame {

    public final BoardControl boardControl;
    private final JButton tiles[];
    private Timer timer;
    private long startTime;
    private int elapsedTimeInSeconds = 0;
    private String playerName;
    private ArrayList<Player> leaderboard = new ArrayList<>();

    public GUI() {
        super("15 Puzzle");
        initComponents();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setAlwaysOnTop(true);

        this.tiles = new JButton[]{Tile_1, Tile_2, Tile_3, Tile_4, Tile_5, Tile_6, Tile_7, Tile_8, Tile_9, Tile_10, Tile_11, Tile_12, Tile_13, Tile_14, Tile_15, Tile_0};
        this.boardControl = new BoardControl();
        loadLeaderboard();
        askForPlayerName();

        for (int i = 0; i < tiles.length; ++i) {
            tiles[i].setFocusable(false);
            tiles[i].setFont(tiles[i].getFont().deriveFont(25.0f));

            tiles[i].addActionListener(new ActionListener() {
                int num;

                ActionListener me(int i) {
                    num = i;
                    return this;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (GUI.this.boardControl.isSolving()) return;
                    GUI.this.boardControl.tilePressed(num);
                    GUI.this.drawBoard();
                }
            }.me(i));
        }

        Button_Reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUI.this.boardControl.resetBoard();
                GUI.this.drawBoard();
                stopTimer();
                Label_Time.setText("00:00:00");
            }
        });

        Button_Rand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GUI.this.boardControl.isSolving()) return;
                GUI.this.boardControl.randomizeBoard();
                GUI.this.drawBoard();
                startTimer();
            }
        });

        Button_Solve_A.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GUI.this.boardControl.isSolving()) return;
                GUI.this.boardControl.solve(GUI.this, Solvers.SOLVE_METHOD.A_STAR);
                GUI.this.pack();
            }
        });

        Button_Solve_DFS.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GUI.this.boardControl.isSolving()) return;
                GUI.this.boardControl.solve(GUI.this, Solvers.SOLVE_METHOD.DFS);
                GUI.this.pack();
            }
        });

        Button_Speed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (GUI.this.boardControl.isSolving()) return;
                String current = ((JButton) e.getSource()).getText();
                switch (current) {
                    case "Slow":
                        GUI.this.boardControl.setTimerSpeed(BoardControl.SPEED.MEDIUM);
                        GUI.this.Button_Speed.setText("Medium");
                        break;
                    case "Medium":
                        GUI.this.boardControl.setTimerSpeed(BoardControl.SPEED.FAST);
                        GUI.this.Button_Speed.setText("Fast");
                        break;
                    case "Fast":
                        GUI.this.boardControl.setTimerSpeed(BoardControl.SPEED.SLOW);
                        GUI.this.Button_Speed.setText("Slow");
                        break;
                }
            }

        });
        this.drawBoard();
        this.pack();
    }

    private void loadLeaderboard() {
        // Load leaderboard from a file or database if available
        // For simplicity, we'll initialize it with dummy data
        leaderboard.add(new Player("Player 1", 120)); // Dummy data
        leaderboard.add(new Player("Player 2", 150)); // Dummy data
        leaderboard.add(new Player("Player 3", 90)); // Dummy data
        // Sort leaderboard by time taken (ascending order)
        Collections.sort(leaderboard, Comparator.comparingInt(Player::getTimeTaken));
    }

    private void askForPlayerName() {
        playerName = JOptionPane.showInputDialog(this, "Enter your name:");
    }

    private void updateLeaderboard() {
        // Update leaderboard with current player's data
        leaderboard.add(new Player(playerName, elapsedTimeInSeconds));
        // Sort leaderboard by time taken (ascending order)
        Collections.sort(leaderboard, Comparator.comparingInt(Player::getTimeTaken));
        // Limit the leaderboard to top 5 players
        if (leaderboard.size() > 5) {
            leaderboard.subList(5, leaderboard.size()).clear();
        }
    }

    private void displayLeaderboard() {
        // Display leaderboard in a dialog box
        StringBuilder leaderboardText = new StringBuilder("Leaderboard:\n");
        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboardText.append(i + 1).append(". ").append(leaderboard.get(i)).append("\n");
        }
        JOptionPane.showMessageDialog(this, leaderboardText.toString(), "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startTimer() {
        elapsedTimeInSeconds = 0; // Reset the elapsed time counter
        timer = new Timer(1000, new ActionListener() { // Timer updates every second
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedTimeInSeconds++;
                updateTimerLabel();
            }
        });
        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    private void updateTimerLabel() {
        int hours = elapsedTimeInSeconds / 3600;
        int minutes = (elapsedTimeInSeconds % 3600) / 60;
        int seconds = elapsedTimeInSeconds % 60;
        Label_Time.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds)); // Update the timer label
    }

    private void solveWithAStar() {
        // Start solving algorithm in a separate thread
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                GUI.this.boardControl.solve(GUI.this, Solvers.SOLVE_METHOD.A_STAR);
                stopTimer(); // Stop the timer when the puzzle is solved
                return null;
            }
        }.execute();
    }

    public final void drawBoard() {
        final byte[] board = boardControl.getCurrentBoard();
        int empty = -1;

        for (int i = 0; i < board.length; ++i) {
            if (board[i] == 0) empty = i;
            else tiles[i].setText(String.valueOf(board[i]));
        }

        for (JButton tile : tiles) tile.setVisible(true);
        tiles[empty].setVisible(false);

        Main_Middle.repaint();
        Main_Middle.revalidate();
    }

    public void setStatus(String stat) {
        this.Label_Status.setText(stat);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        Button_Leaderboard = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        Main_Right = new javax.swing.JPanel();
        ButtonsPanel = new javax.swing.JPanel();
        Button_Rand = new javax.swing.JButton();
        Button_Reset = new javax.swing.JButton();
        Button_Solve_A = new javax.swing.JButton();
        Button_Solve_DFS = new javax.swing.JButton();
        Button_Speed = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        Label_Status = new javax.swing.JLabel();
        Main_Middle = new javax.swing.JPanel();
        Tile_1 = new javax.swing.JButton();
        Tile_2 = new javax.swing.JButton();
        Tile_3 = new javax.swing.JButton();
        Tile_4 = new javax.swing.JButton();
        Tile_5 = new javax.swing.JButton();
        Tile_6 = new javax.swing.JButton();
        Tile_7 = new javax.swing.JButton();
        Tile_8 = new javax.swing.JButton();
        Tile_9 = new javax.swing.JButton();
        Tile_10 = new javax.swing.JButton();
        Tile_11 = new javax.swing.JButton();
        Tile_12 = new javax.swing.JButton();
        Tile_13 = new javax.swing.JButton();
        Tile_14 = new javax.swing.JButton();
        Tile_15 = new javax.swing.JButton();
        Tile_0 = new javax.swing.JButton();
        Label_Time = new javax.swing.JLabel();
        Label_Time.setFont(new java.awt.Font("Tahoma", 0, 18)); // Set the font for the timer label
        Label_Time.setText("00:00:00"); // Initial text for the timer label
        Label_Time.setHorizontalAlignment(javax.swing.SwingConstants.CENTER); // Align the text to the center
        Label_Time.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add some padding to the label

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Button_Leaderboard.setText("Leaderboard");
        Button_Leaderboard.setFocusable(false);
        Button_Leaderboard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayLeaderboard();
            }
        });

        Main_Right.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 10));
        Main_Right.setLayout(new java.awt.BorderLayout());

        ButtonsPanel.setOpaque(false);
        ButtonsPanel.setLayout(new java.awt.GridBagLayout());

        Button_Rand.setFont(new java.awt.Font("Ubuntu", 0, 16));
        Button_Rand.setText("Randomize");
        Button_Rand.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        ButtonsPanel.add(Button_Rand, gridBagConstraints);

        Button_Reset.setFont(new java.awt.Font("Ubuntu", 0, 16));
        Button_Reset.setText("Reset");
        Button_Reset.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        ButtonsPanel.add(Button_Reset, gridBagConstraints);

        jPanel2.add(Label_Time);
        jPanel2.add(Button_Leaderboard); // Moved leaderboard button to be under the timer

        Button_Solve_A.setFont(new java.awt.Font("Ubuntu", 0, 16));
        Button_Solve_A.setText("Solve - A*");
        Button_Solve_A.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        ButtonsPanel.add(Button_Solve_A, gridBagConstraints);

        Button_Solve_DFS.setFont(new java.awt.Font("Ubuntu", 0, 16));
        Button_Solve_DFS.setText("Solve - DFS");
        Button_Solve_DFS.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        ButtonsPanel.add(Button_Solve_DFS, gridBagConstraints);

        Button_Speed.setText("Slow");
        Button_Speed.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        ButtonsPanel.add(Button_Speed, gridBagConstraints);

        Main_Right.add(ButtonsPanel, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 1, 1, 1));
        jPanel2.setOpaque(false);
        jPanel2.add(Label_Status);

        Main_Right.add(jPanel2, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(Main_Right, java.awt.BorderLayout.LINE_END);

        Main_Middle.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Main_Middle.setLayout(new java.awt.GridBagLayout());

        Tile_1.setText("1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_1, gridBagConstraints);

        Tile_2.setText("2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_2, gridBagConstraints);

        Tile_3.setText("3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_3, gridBagConstraints);

        Tile_4.setText("4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_4, gridBagConstraints);

        Tile_5.setText("5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_5, gridBagConstraints);

        Tile_6.setText("6");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_6, gridBagConstraints);

        Tile_7.setText("7");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_7, gridBagConstraints);

        Tile_8.setText("8");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_8, gridBagConstraints);

        Tile_9.setText("9");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_9, gridBagConstraints);

        Tile_10.setText("10");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_10, gridBagConstraints);

        Tile_11.setText("11");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_11, gridBagConstraints);

        Tile_12.setText("12");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_12, gridBagConstraints);

        Tile_13.setText("13");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_13, gridBagConstraints);

        Tile_14.setText("14");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_14, gridBagConstraints);

        Tile_15.setText("15");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_15, gridBagConstraints);

        Tile_0.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        Main_Middle.add(Tile_0, gridBagConstraints);

        getContentPane().add(Main_Middle, java.awt.BorderLayout.CENTER);

        pack();
    }

    // Variables declaration - do not modify
    private javax.swing.JButton Button_Leaderboard;
    private javax.swing.JButton Button_Rand;
    private javax.swing.JButton Button_Reset;
    private javax.swing.JButton Button_Solve_A;
    private javax.swing.JButton Button_Solve_DFS;
    private javax.swing.JButton Button_Speed;
    private javax.swing.JPanel ButtonsPanel;
    private javax.swing.JLabel Label_Status;
    private javax.swing.JLabel Label_Time; // Added label for displaying time
    private javax.swing.JPanel Main_Middle;
    private javax.swing.JPanel Main_Right;
    private javax.swing.JButton Tile_0;
    private javax.swing.JButton Tile_1;
    private javax.swing.JButton Tile_10;
    private javax.swing.JButton Tile_11;
    private javax.swing.JButton Tile_12;
    private javax.swing.JButton Tile_13;
    private javax.swing.JButton Tile_14;
    private javax.swing.JButton Tile_15;
    private javax.swing.JButton Tile_2;
    private javax.swing.JButton Tile_3;
    private javax.swing.JButton Tile_4;
    private javax.swing.JButton Tile_5;
    private javax.swing.JButton Tile_6;
    private javax.swing.JButton Tile_7;
    private javax.swing.JButton Tile_8;
    private javax.swing.JButton Tile_9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration
}
