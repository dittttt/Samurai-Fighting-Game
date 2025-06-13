
package main;

import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import gamestates.Gamestate;
import inputs.KeyboardInputs;
import inputs.MouseInputs;

public class GamePanel extends JPanel implements MouseWheelListener {

	private MouseInputs mouseInputs;
	private Game game;

	public GamePanel(Game game) {
		mouseInputs = new MouseInputs(this);
		this.game = game;

		setPanelSize();
		addKeyListener(new KeyboardInputs(this));
		addMouseListener(mouseInputs);
		addMouseMotionListener(mouseInputs);
		addMouseWheelListener(this); // Register mouse wheel listener
	}

	private void setPanelSize() {
		Dimension size = new Dimension(GAME_WIDTH, GAME_HEIGHT);
		setPreferredSize(size);
		System.out.println("Content size : " + GAME_WIDTH + " : " + GAME_HEIGHT);
	}

	public void updateGame() {
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Cast Graphics to Graphics2D
		Graphics2D g2d = (Graphics2D) g;

		// Calculate the scaling factors
		float scaleX = (float) getWidth() / Game.GAME_WIDTH;
		float scaleY = (float) getHeight() / Game.GAME_HEIGHT;

		// Apply scaling
		g2d.scale(scaleX, scaleY);

		// Render the game using the scaled graphics
		game.render(g2d);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (Gamestate.state == Gamestate.PLAYING) {
			game.getPlaying().getPauseOverlay().mouseWheelMoved(e);
		}
	}

	public Game getGame() {
		return game;
	}
}
