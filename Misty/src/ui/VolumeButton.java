package ui;

import static utilz.Constants.UI.VolumeButtons.SLIDER_DEFAULT_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_DEFAULT_HEIGHT;
import static utilz.Constants.UI.VolumeButtons.VOLUME_DEFAULT_WIDTH;
import static utilz.Constants.UI.VolumeButtons.VOLUME_WIDTH;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import utilz.LoadSave;

public class VolumeButton extends PauseButton {

	private BufferedImage[] imgs;
	private BufferedImage slider;
	private int index = 0;
	private boolean mouseOver, mousePressed;
	private int buttonX, minX, maxX;
	private int scrollStep = 5; // How many pixels to move per scroll step

	public VolumeButton(int x, int y, int width, int height) {
		super(x + width / 2, y, VOLUME_WIDTH, height);
		bounds.x -= VOLUME_WIDTH / 2;
		buttonX = x + width / 2;
		this.x = x;
		this.width = width;
		minX = x + VOLUME_WIDTH / 2;
		maxX = x + width - VOLUME_WIDTH / 2;
		loadImgs();
	}

	private void loadImgs() {
		BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.VOLUME_BUTTONS);
		imgs = new BufferedImage[3];
		for (int i = 0; i < imgs.length; i++)
			imgs[i] = temp.getSubimage(i * VOLUME_DEFAULT_WIDTH, 0, VOLUME_DEFAULT_WIDTH, VOLUME_DEFAULT_HEIGHT);

		slider = temp.getSubimage(3 * VOLUME_DEFAULT_WIDTH, 0, SLIDER_DEFAULT_WIDTH, VOLUME_DEFAULT_HEIGHT);
	}

	public void update() {
		index = 0;
		if (mouseOver)
			index = 1;
		if (mousePressed)
			index = 2;
	}

	public void draw(Graphics g) {
		g.drawImage(slider, x, y, width, height, null);
		g.drawImage(imgs[index], buttonX - VOLUME_WIDTH / 2, y, VOLUME_WIDTH, height, null);
	}

	public void changeX(int x) {
		if (x < minX)
			buttonX = minX;
		else if (x > maxX)
			buttonX = maxX;
		else
			buttonX = x;

		bounds.x = buttonX - VOLUME_WIDTH / 2;
	}

	public void onMouseWheelMoved(MouseWheelEvent e) {
	    // Check if the mouse is within the horizontal range of the slider
	    if (e.getX() >= x && e.getX() <= x + width) {
	        int notches = -e.getWheelRotation(); // Invert the scrolling direction
	        int newX = buttonX + (notches * scrollStep);
	        changeX(newX);
	    }
	}

	public void resetBools() {
		mouseOver = false;
		mousePressed = false;
	}

	public boolean isMouseOver() {
		return mouseOver;
	}

	public void setMouseOver(boolean mouseOver) {
		this.mouseOver = mouseOver;
	}

	public boolean isMousePressed() {
		return mousePressed;
	}

	public void setMousePressed(boolean mousePressed) {
		this.mousePressed = mousePressed;
	}
	
	public boolean isMouseOverSlider(MouseEvent e) {
	    return e.getX() >= x && e.getX() <= x + width && e.getY() >= y && e.getY() <= y + height;
	}
}