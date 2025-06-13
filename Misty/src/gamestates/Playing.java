package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import entities.Enemy;
import entities.Player;
import levels.LevelManager;
import main.Game;
import ui.PauseOverlay;

public class Playing extends State implements Statemethods {
    private Player player;
    private Enemy enemy;
    private LevelManager levelManager;
    private PauseOverlay pauseOverlay;
    private boolean paused = false;

    // Health bar dimensions
    private static final int HEALTH_BAR_WIDTH = 200;
    private static final int HEALTH_BAR_HEIGHT = 20;
    private static final int HEALTH_BAR_Y_OFFSET = 10;
    private static final int HEALTH_BAR_X_PADDING = 20;

    // Health bar colors
    private static final Color PLAYER_HEALTH_COLOR = new Color(0, 180, 0);  // Vibrant green
    private static final Color PLAYER_BG_COLOR = new Color(0, 80, 0);       // Dark green
    private static final Color ENEMY_HEALTH_COLOR = new Color(200, 0, 0);   // Deep red
    private static final Color ENEMY_BG_COLOR = new Color(80, 0, 0);        // Dark red
    private static final Color HEALTH_BAR_BORDER = Color.BLACK;

    public Playing(Game game) {
        super(game);
        initClasses();
    }

    private void initClasses() {
        levelManager = new LevelManager(game);
        player = new Player(100, 200, (int) (112 * Game.SCALE), (int) (72 * Game.SCALE));
        player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        enemy = new Enemy(1719, 200, (int) (112 * Game.SCALE), (int) (72 * Game.SCALE), player);
        enemy.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        pauseOverlay = new PauseOverlay(this);
    }

    @Override
    public void update() {
        if (!paused) {
            levelManager.update();
            player.update();
            enemy.update();
        } else {
            pauseOverlay.update();
        }
    }

    @Override
    public void draw(Graphics g) {
        levelManager.draw(g);
        player.render(g);
        enemy.render(g);

        // Draw health bars
        drawPlayerHealthBar(g);
        drawEnemyHealthBar(g);

        if (paused)
            pauseOverlay.draw(g);
    }

    private void drawPlayerHealthBar(Graphics g) {
        int x = HEALTH_BAR_X_PADDING;
        int y = HEALTH_BAR_Y_OFFSET;
        
        // Draw background
        g.setColor(PLAYER_BG_COLOR);
        g.fillRect(x, y, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        
        // Draw health fill
        int healthWidth = (int)((player.getHealth() / 100.0) * HEALTH_BAR_WIDTH);
        g.setColor(PLAYER_HEALTH_COLOR);
        g.fillRect(x, y, healthWidth, HEALTH_BAR_HEIGHT);
        
        // Draw border
        g.setColor(HEALTH_BAR_BORDER);
        g.drawRect(x, y, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        
        // Draw health text
        g.setColor(Color.WHITE);
        g.drawString("PLAYER: " + player.getHealth(), x + 5, y + HEALTH_BAR_HEIGHT - 5);
    }

    private void drawEnemyHealthBar(Graphics g) {
        // Use game's screen width directly
        int screenWidth = Game.GAME_WIDTH;
        int x = screenWidth - HEALTH_BAR_WIDTH - HEALTH_BAR_X_PADDING;
        int y = HEALTH_BAR_Y_OFFSET;
        
        // Draw background
        g.setColor(ENEMY_BG_COLOR);
        g.fillRect(x, y, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        
        // Draw health fill
        int healthWidth = (int)((enemy.getHealth() / 100.0) * HEALTH_BAR_WIDTH);
        g.setColor(ENEMY_HEALTH_COLOR);
        g.fillRect(x, y, healthWidth, HEALTH_BAR_HEIGHT);
        
        // Draw border
        g.setColor(HEALTH_BAR_BORDER);
        g.drawRect(x, y, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        
        // Draw health text
        g.setColor(Color.WHITE);
        g.drawString("ENEMY: " + enemy.getHealth(), x + 5, y + HEALTH_BAR_HEIGHT - 5);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            player.setLightAttack(true);
        else if (e.getButton() == MouseEvent.BUTTON3)
            player.setHeavyAttack(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                player.setLeft(true);
                break;
            case KeyEvent.VK_D:
                player.setRight(true);
                break;
            case KeyEvent.VK_SPACE:
                player.setJump(true);
                break;
            case KeyEvent.VK_J:
                player.toggleLightAttack(true);
                break;
            case KeyEvent.VK_K:
                player.toggleHeavyAttack(true);
                break;
            case KeyEvent.VK_SHIFT:
                player.setRun(true);
                break;
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_C:
                player.toggleRoll(true);
                break;
            case KeyEvent.VK_H:
                player.toggleHit(true);
                break;
            case KeyEvent.VK_ESCAPE:
                paused = !paused;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
                player.setLeft(false);
                break;
            case KeyEvent.VK_D:
                player.setRight(false);
                break;
            case KeyEvent.VK_SPACE:
                player.setJump(false);
                break;
            case KeyEvent.VK_SHIFT:
                player.setRun(false);
                break;
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (paused)
            pauseOverlay.mouseDragged(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (paused)
            pauseOverlay.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (paused)
            pauseOverlay.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (paused)
            pauseOverlay.mouseMoved(e);
    }

    public void unpauseGame() {
        paused = false;
    }

    public void windowFocusLost() {
        player.resetDirBooleans();
    }

    public Player getPlayer() {
        return player;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public PauseOverlay getPauseOverlay() {
        return pauseOverlay;
    }
}