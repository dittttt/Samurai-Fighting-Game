package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import main.Game;
import ui.MenuButton;
import utilz.LoadSave;

public class Menu extends State implements Statemethods {

	private MenuButton[] buttons = new MenuButton[3];
	private BufferedImage backgroundImg;
	private int menuX, menuY, menuWidth, menuHeight;

	public Menu(Game game) {
		super(game);
		loadButtons();
		loadBackground();
	}

	private void loadBackground() {
	    backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
	    menuWidth = (int) (backgroundImg.getWidth() * Game.MENU_SCALE);
	    menuHeight = (int) (backgroundImg.getHeight() * Game.MENU_SCALE);

	    // Calculate the vertical center of the screen
	    int centerY = (Game.GAME_HEIGHT - menuHeight) / 2;

	    menuX = Game.GAME_WIDTH / 2 - menuWidth / 2;
	    menuY = centerY;
	}


	private void loadButtons() {
	    buttons[0] = new MenuButton(Game.GAME_WIDTH / 2, (int) (215 * Game.MENU_SCALE), 0, Gamestate.PLAYING);
	    buttons[1] = new MenuButton(Game.GAME_WIDTH / 2, (int) (285 * Game.MENU_SCALE), 1, Gamestate.OPTIONS);
	    buttons[2] = new MenuButton(Game.GAME_WIDTH / 2, (int) (355* Game.MENU_SCALE), 2, Gamestate.QUIT);
	}



	@Override
	public void update() {
		for (MenuButton mb : buttons)
			mb.update();
	}

	@Override
	public void draw(Graphics g) {

		g.drawImage(backgroundImg, menuX, menuY, menuWidth, menuHeight, null);

		for (MenuButton mb : buttons)
			mb.draw(g);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		for (MenuButton mb : buttons) {
			if (isIn(e, mb)) {
				mb.setMousePressed(true);
				break;
			}
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		for (MenuButton mb : buttons) {
			if (isIn(e, mb)) {
				if (mb.isMousePressed())
					mb.applyGamestate();
				break;
			}
		}
		resetButtons();
	}

	private void resetButtons() {
		for (MenuButton mb : buttons)
			mb.resetBools();

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		for (MenuButton mb : buttons)
			mb.setMouseOver(false);

		for (MenuButton mb : buttons)
			if (isIn(e, mb)) {
				mb.setMouseOver(true);
				break;
			}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
			Gamestate.state = Gamestate.PLAYING;

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
