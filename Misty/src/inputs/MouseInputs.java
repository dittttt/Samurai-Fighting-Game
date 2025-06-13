
package inputs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import gamestates.Gamestate;
import main.Game;
import main.GamePanel;

public class MouseInputs implements MouseListener, MouseMotionListener {
	private GamePanel gamePanel;

	public MouseInputs(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
	}

	private int scaleX(int x) {
		float scale = (float) gamePanel.getWidth() / Game.GAME_WIDTH;
		return (int) (x / scale); // Adjust scaling for fullscreen
	}

	private int scaleY(int y) {
		float scale = (float) gamePanel.getHeight() / Game.GAME_HEIGHT;
		return (int) (y / scale); // Adjust scaling for fullscreen
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int scaledX = scaleX(e.getX());
		int scaledY = scaleY(e.getY());
		e.translatePoint(scaledX - e.getX(), scaledY - e.getY());
		switch (Gamestate.state) {
		case PLAYING:
			gamePanel.getGame().getPlaying().mouseDragged(e);
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int scaledX = scaleX(e.getX());
		int scaledY = scaleY(e.getY());
		e.translatePoint(scaledX - e.getX(), scaledY - e.getY());
		switch (Gamestate.state) {
		case MENU:
			gamePanel.getGame().getMenu().mouseMoved(e);
			break;
		case PLAYING:
			gamePanel.getGame().getPlaying().mouseMoved(e);
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int scaledX = scaleX(e.getX());
		int scaledY = scaleY(e.getY());
		e.translatePoint(scaledX - e.getX(), scaledY - e.getY());
		switch (Gamestate.state) {
		case PLAYING:
			gamePanel.getGame().getPlaying().mouseClicked(e);
			break;
		default:
			break;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int scaledX = scaleX(e.getX());
		int scaledY = scaleY(e.getY());
		e.translatePoint(scaledX - e.getX(), scaledY - e.getY());
		switch (Gamestate.state) {
		case MENU:
			gamePanel.getGame().getMenu().mousePressed(e);
			break;
		case PLAYING:
			gamePanel.getGame().getPlaying().mousePressed(e);
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int scaledX = scaleX(e.getX());
		int scaledY = scaleY(e.getY());
		e.translatePoint(scaledX - e.getX(), scaledY - e.getY());
		switch (Gamestate.state) {
		case MENU:
			gamePanel.getGame().getMenu().mouseReleased(e);
			break;
		case PLAYING:
			gamePanel.getGame().getPlaying().mouseReleased(e);
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
