package gamestates;

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

	public Playing(Game game) {
		super(game);
		initClasses();
	}

	private void initClasses() {
		levelManager = new LevelManager(game);
		player = new Player(100, 200, (int) (112 * Game.SCALE), (int) (72 * Game.SCALE));
		player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		enemy = new Enemy(1719, 200, (int) (112 * Game.SCALE), (int) (72 * Game.SCALE));
		enemy.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		pauseOverlay = new PauseOverlay(this);
	}

	@Override
	public void update() {
		if (!paused) {
			levelManager.update();
			player.update();
			float playerX = player.getPlayerX(); // Obtain player's X coordinate
			float playerY = player.getPlayerY(); // Obtain player's Y coordinate
			enemy.update(playerX, playerY);
		} else {
			pauseOverlay.update();
		}
	}

	@Override
	public void draw(Graphics g) {
		levelManager.draw(g);
		player.render(g);
		enemy.render(g);

		if (paused)
			pauseOverlay.draw(g);

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
//IF GANAHAN TA MA CANCEL
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
		return pauseOverlay; // Ensure `pauseOverlay` is properly initialized in the `Playing` class.
	}

}
