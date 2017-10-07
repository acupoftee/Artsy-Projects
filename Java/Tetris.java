package com.bethydiakabana;

/**
 * Created by bethydiakabana on 10/7/17.
 */
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import javax.swing.*;


public class Tetris extends JPanel implements ActionListener {

    final Color empty = Color.black;
    public static int DEFAULT_R = 20;
    public static int DEFAULT_C = 10;
    public int rows;
    public int cols;
    public Color[][] board;

    public Timer timer;
    public int delay;

    public int counter;
    public int currentScore;
    public int numRowsCleared;

    //DIRECTIONS
    private static final int[] DOWN = {1, 0};
    private static final int[] LEFT = {0, -1};
    private static final int[] RIGHT = {0, 1};

    /*CURRENT PIECE*/
    boolean[][] p;
    int pieceRow;
    int pieceCol;
    Color pieceColor;

    /*NEXT PIECE*/
    boolean[][] nextp;
    Color nextColor;

    /*TYPES OF PIECES*/
    final int current = 1;
    final int next = 2;

    /** Constructor for empty tetris board with java keylistener
     * @param r = rows
     * @param c = columns
     */
    public Tetris(int r, int c) {
        this.rows = r;
        this.cols = c;
        board = new Color[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = empty;
            }
        }

        delay = 500;
        timer = new Timer(delay, this);
        timer.start();

        counter = 0;
        currentScore = 0;

        this.setFocusable(true);
        KeyListener l = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    moveFallingPiece(LEFT);
                    repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    moveFallingPiece(RIGHT);
                    repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    dropDown();
                    repaint();
                }
                else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    rotateFallingPiece();
                    repaint();
                }
            }
        };
        addKeyListener(l);
        p = newFallingPiece(1);
        nextp = newFallingPiece(2);
    }

    public Tetris() {
        this(DEFAULT_R, DEFAULT_C);
    }

    /** moves falling piece, updates game and repaints */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!moveFallingPiece(1, 0)) {
            placeFallingPiece();
            if (!gameIsOver()) {
                setNewPiece();
                nextp = newFallingPiece(2);
            }
        }
        checkRowCompletion();
        setTimer();
        repaint();
    }

    /**SEVEN STANDARD PIECES (TETROMINOES):*/

    static boolean T = true;
    private static final boolean[][] I_PIECE = {
            {T, T, T, T},
    };
    private static final boolean[][] J_PIECE = {
            {T, false, false},
            {T, T, T}
    };
    private static final boolean[][] L_PIECE = {
            {false, false, T},
            {T, T, T}

    };
    private static final boolean[][] O_PIECE = {
            {T, T},
            {T, T}
    };
    private static final boolean[][] T_PIECE = {
            {false, T, false},
            {T, T, T}

    };
    private static final boolean[][] S_PIECE = {
            {false, T, T},
            {T, T, false}

    };
    private static final boolean[][] Z_PIECE = {
            {T, T, false},
            {false, T, T}
    };

    private static boolean[][][] PIECES = {
            I_PIECE, J_PIECE, L_PIECE, O_PIECE, T_PIECE, S_PIECE, Z_PIECE
    };

    private static Color[] PIECE_COLORS = {
            Color.red, Color.yellow, Color.magenta, Color.pink, Color.green, Color.cyan, Color.orange
    };

    /**checks for game over based on position of piece*/
    public boolean gameIsOver() {
        int height = p.length;
        if (pieceRow - height <= 0) {
            return true;
        }
        return false;
    }

    /** generates a random new tetromino piece, assigns position based on type
     * @param type = current piece or next piece (1 or 2)
     * @return falling piece
     */
    public boolean[][] newFallingPiece(int type) {
        Random r = new Random();
        int i = r.nextInt(7);

        boolean[][] fallingPiece = PIECES[i];
        if (type == 1)
            pieceColor = PIECE_COLORS[i];
        else
            nextColor = PIECE_COLORS[i];

        if (type == 1) {
            this.pieceRow = 0;
            int offset = (fallingPiece[0].length+1)/2;
            this.pieceCol = 5 - offset;
        }

        return fallingPiece;
    }

    /**sets the new current piece to the "next piece"*/
    public void setNewPiece() {
        p = nextp;
        pieceColor = nextColor;
        this.pieceRow = 0;
        int offset = (p[0].length+1)/2;
        this.pieceCol = 5 - offset;
    }

    /**checks legality of piece position, returns false if:
     * off-board
     * collides
     */
    public boolean isLegal() {
        //off the grid?
        int maxRow = pieceRow+p.length-1;
        if (maxRow >= 20 || pieceCol < 0 || pieceCol+p[0].length > 10) {
            return false;
        }
        for (int i = 0; i < p.length; i++) { //rows of array
            for (int j = 0; j < p[0].length; j++) { //cols of array
                try {
                    if (p[i][j] == true && board[pieceRow+i][pieceCol+j] != empty) {
                        return false;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {}
            }
        }
        return true;
    }


    /**moves piece using direction array*/
    public void moveFallingPiece(int[] direction) {
        int dr = direction[0];
        int dc = direction[1];
        moveFallingPiece(dr, dc);
    }

    /**moves piece using the change in row, change in column*/
    public boolean moveFallingPiece(int drow, int dcol) {
        pieceRow += drow;
        pieceCol += dcol;
        if (!isLegal()) {
            pieceRow -= drow;
            pieceCol -= dcol;
            return false;
        }
        return true;
    }

    /** rotates falling piece by evaluating new dimensions and converting blocks to respective new positions
     * if rotated piece is legal, sets current piece equal
     */
    public void rotateFallingPiece() {
        boolean[][] original = p;
        int width = p.length-1;
        int height = p[0].length - 1;

        boolean[][] newPiece = new boolean[height+1][width+1];

        for (int i = 0; i <= height; i++) {
            for (int j = 0; j <= width; j++) {
                newPiece[i][j] = original[j][height-i];
            }
        }
        p = newPiece;
        int rowShift = (original.length-1-height)/2;
        int colShift = (original[0].length-1-width)/2;
        pieceRow += rowShift;
        pieceCol += colShift;

        if (!isLegal()) {
            p = original;
            pieceRow -= rowShift;
            pieceCol -= colShift;
        }
    }

    /**drops piece down to lowest possible row*/
    public void dropDown() {
        boolean notDropped = true;
        while (notDropped) {
            pieceRow++;
            if (!isLegal()) {
                pieceRow--;
                notDropped = false;
            }
        }
    }

    /**checks for row completions, make changes
     * sets score:
     * 1 row cleared = 10 pts
     * 2 rows cleared = 20 pts
     * 3 rows cleared = 40 pts
     * 4 rows cleared = 80 pts
     */
    public void checkRowCompletion() {
        boolean cleared = false; //for counter
        int score = 0;
        int[] complete = new int[20];
        for (int i = 0; i < 20; i++) {
            if (rowFull(i)) {
                complete[i] = 1;
                score = 10;
                cleared = true;
                if (i>=1 && complete[i-1]==1) {
                    score = 20;
                    if (i>=2 && complete[i-2]==1) {
                        score = 40;
                        if (i>=3 && complete[i-3]==1) {
                            score = 80;
                        }
                    }
                }
            }
        }
        if (cleared)
            counter++; //counter only increments by 1 per change (i.e. four lines cleared = 1 line cleared)
        setScore(score);
        clearComplete(complete);
        shiftDown(complete);
    }

    /**clears full rows*/
    public void clearComplete(int[] complete) {
        for (int i = 0; i < complete.length; i++) {
            if (complete[i]==1) {
                for (int j = 0; j < 10; j++) {
                    board[i][j] = Color.black;
                }
            }
        }
    }

    /**shifts down rows that were full and cleared*/
    public void shiftDown(int[] complete) {
        for (int row = 0; row < complete.length; row++) {
            if (complete[row] == 1) {
                for (int y=row; y>0; y--) {
                    for (int j = 0; j < 10; j++) {
                        board[y][j] = board[y-1][j];
                    }
                }
            }
        }
    }

    public void setScore(int score) {
        currentScore += score;
    }

    public boolean rowFull(int row) {
        for (Color c : board[row]) {
            if (c == Color.black) {
                return false;
            }
        }
        return true;
    }

    /**changes delay of actionlistener (makes piece move faster) based on number of times rows were cleared*/
    public void setTimer() {
        if (counter == 5) {
            counter = 0;
            delay -= 25;
            timer.setDelay(delay);
        }
    }

    /**GUI*/
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getSize().width, this.getSize().height);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                drawCell(g, board[i][j], j, i);
            }
        }
        paintFallingPiece(g);
        paintNextPiece(g);

        g.setColor(Color.WHITE);
        g.fillRect(250, 0, 2, 520);

        String score = Integer.toString(currentScore);
        g.setFont(new Font("Garamond", Font.BOLD, 18));
        g.drawString("Score: " + score, cols*25+10, 20);

        g.drawString("Next Piece:", cols*25+20, 200);
    }


    public void paintFallingPiece(Graphics g) {
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[0].length; j++) {
                //System.out.println(i + " " + j);
                if (p[i][j] == true) {
                    drawCell(g, pieceColor, j+pieceCol, i+pieceRow);
                }
            }
        }
    }

    public void paintNextPiece(Graphics g) {
        for (int i = 0; i < nextp.length; i++) {
            for (int j = 0; j < nextp[0].length; j++) {
                int offset = (nextp[0].length+1)/2;
                if (nextp[i][j] == true) {
                    drawCell(g, nextColor, j+13-offset, i+10);
                }
            }
        }
    }

    public void placeFallingPiece() {
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[0].length; j++) {
                if (p[i][j] == true) {
                    board[pieceRow+i][pieceCol+j] = pieceColor;
                }
            }
        }
    }

    public void drawCell(Graphics g, Color color, int x, int y) {
        int xPos = x*25;
        int yPos = y*25;

        g.setColor(color);
        g.fillRect(xPos+1, yPos+1, 24, 24);
    }


    //MAIN
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setTitle("Tetris");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //f.setSize(200, 420);
        f.setSize(390, 520);

        Tetris tetris = new Tetris();
        f.add(tetris);
        f.setVisible(true);
    }
}
