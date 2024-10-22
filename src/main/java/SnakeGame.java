import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    @SuppressWarnings("InnerClassMayBeStatic")
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    int boardWidth;
    int boardHeight;
    int tileSize;
    int totalTiles;

    //Snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    Tile food;
    Random random;

    //Game logic
    Timer gameLoop;
    int velocityX;
    int velocityY;
    boolean hasStarted = false;

    SnakeGame(int boardWidth, int boardHeight, int tileSize) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.tileSize = tileSize;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        //noinspection Convert2Diamond
        snakeBody = new ArrayList<Tile>();

        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        gameLoop = new Timer(50, this);
        gameLoop.start();

        // Calculate the total number of tiles
        totalTiles = (boardWidth * boardHeight) / (tileSize * tileSize);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        //Grid
        for (int i = 0; i < boardWidth/tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
            g.drawLine(0, i * tileSize, boardWidth, i * tileSize);
        }

        //Food
        g.setColor(Color.RED);
        g.fillRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize);

        //Snake head
        g.setColor(Color.GREEN);
        g.fillRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize);

        //Snake body
        for (Tile tile : snakeBody) {
            g.fillRect(tile.x * tileSize, tile.y * tileSize, tileSize, tileSize);
        }
    }

    public void placeFood() {
        food.x = random.nextInt(boardWidth/tileSize);
        food.y = random.nextInt(boardHeight/tileSize);
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    public void move() {
        if (!hasStarted) {
            return;
        }

        // Move the body: start from the last part and set its position to the part before it
        for (int i = snakeBody.size() - 1; i > 0; i--) {
            snakeBody.get(i).x = snakeBody.get(i - 1).x;
            snakeBody.get(i).y = snakeBody.get(i - 1).y;
        }

        // Move the first body part to follow the head
        if (!snakeBody.isEmpty()) {
            //noinspection SequencedCollectionMethodCanBeUsed
            snakeBody.get(0).x = snakeHead.x;
            //noinspection SequencedCollectionMethodCanBeUsed
            snakeBody.get(0).y = snakeHead.y;
        }

        // Move the head
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        // Wall collision detection
        if (snakeHead.x < 0 || snakeHead.x >= boardWidth / tileSize || snakeHead.y < 0 || snakeHead.y >= boardHeight / tileSize) {
            // Snake hits the wall, handle game over logic here
            setGameOver("Game over! You hit a wall.");
        }

        // Self collision check
        for (Tile tile : snakeBody) {
            if (collision(snakeHead, tile)) {
                setGameOver("Game over! You hit yourself.");
            }
        }

        // Eat food
        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(snakeHead.x, snakeHead.y)); // Add new part at the current head position
            placeFood();

            // Check if the snake has filled the entire board (win condition)
            if (snakeBody.size() + 1 == totalTiles) { // +1 for the snake head
                setGameOver("Congratulations! You've won!");
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (!hasStarted) {
            hasStarted = true;
        }

        if ((e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W ) && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        } else if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        } else if ((e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        } else if ((e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    void setGameOver(String message) {
        gameLoop.stop();
        hasStarted = false;

        int reply = JOptionPane.showConfirmDialog(this, message + "\nDo you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (reply == JOptionPane.YES_OPTION) {
            resetGame(); // Restart the game
        } else {
            System.exit(0); // Exit the game
        }
    }

    public void resetGame() {
        // Reset the snake head and body
        snakeHead = new Tile(5, 5);
        snakeBody.clear(); // Clear the snake body

        // Place new food
        placeFood();

        // Reset velocity
        velocityX = 1;
        velocityY = 0;

        // Restart the game loop
        gameLoop.restart();
    }
}