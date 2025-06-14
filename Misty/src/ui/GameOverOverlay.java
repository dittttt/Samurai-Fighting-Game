package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;

import gamestates.Gamestate;
import gamestates.Playing;
import main.Game;
import main.GamePanel;

public class GameOverOverlay {
	private Playing playing;
	private Game game;
	private GamePanel gamePanel;
	private UrmButton replayButton, menuButton;
	private Font font;

	public GameOverOverlay(Playing playing, Game game, GamePanel gamePanel) {
		this.playing = playing;
		this.game = game;
		this.gamePanel = gamePanel; // Initialize gamePanel
		createButtons();
		font = new Font("Arial", Font.BOLD, (int) (40 * Game.MENU_SCALE));
	}

	private void createButtons() {
		int buttonWidth = (int) (56 * Game.MENU_SCALE);
		int buttonHeight = (int) (56 * Game.MENU_SCALE);
		int buttonY = (int) (Game.GAME_HEIGHT / 1.7);
		int replayX = Game.GAME_WIDTH / 2 - buttonWidth - (int) (20 * Game.MENU_SCALE);
		int menuX = Game.GAME_WIDTH / 2 + (int) (20 * Game.MENU_SCALE);

		replayButton = new UrmButton(replayX, buttonY, buttonWidth, buttonHeight, 1);
		menuButton = new UrmButton(menuX, buttonY, buttonWidth, buttonHeight, 2);
	}

	public void update() {
		replayButton.update();
		menuButton.update();
	}

	public void draw(Graphics g) {
		System.out.println("Drawing GameOverOverlay"); // Debug line

		// Dark overlay
		g.setColor(new Color(0, 0, 0, 200));
		g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

		// Game Over text
		g.setColor(Color.WHITE);
		g.setFont(font);
		String text = "GAME OVER";
		int textWidth = g.getFontMetrics().stringWidth(text);
		int textX = (Game.GAME_WIDTH - textWidth) / 2;
		int textY = (int) (Game.GAME_HEIGHT / 3);
		g.drawString(text, textX, textY);

		// Buttons
		replayButton.draw(g);
		menuButton.draw(g);
	}

	public void mousePressed(MouseEvent e) {
		if (isIn(replayButton, e))
			replayButton.setMousePressed(true);
		else if (isIn(menuButton, e))
			menuButton.setMousePressed(true);
	}

	public void mouseReleased(MouseEvent e) {
		System.out.println("Mouse released at: " + e.getX() + "," + e.getY());
		System.out.println("Replay button bounds: " + replayButton.getBounds());
		System.out.println("Menu button bounds: " + menuButton.getBounds());

		if (isIn(replayButton, e)) {
			System.out.println("Replay button clicked");
			if (replayButton.isMousePressed()) {
				playing.resetAll();
				Gamestate.state = Gamestate.PLAYING;
			}
		} else if (isIn(menuButton, e)) {
			System.out.println("Menu button clicked");
			if (menuButton.isMousePressed()) {
				playing.resetAll();
				Gamestate.state = Gamestate.MENU;
			}
		}

		replayButton.resetBools();
		menuButton.resetBools();
	}

	public void mouseMoved(MouseEvent e) {
		replayButton.setMouseOver(false);
		menuButton.setMouseOver(false);

		if (isIn(replayButton, e))
			replayButton.setMouseOver(true);
		else if (isIn(menuButton, e))
			menuButton.setMouseOver(true);
	}

	private boolean isIn(UrmButton b, MouseEvent e) {
		// Use raw coordinates since MouseInputs already handles scaling
		return b.getBounds().contains(e.getX(), e.getY());
	}
}