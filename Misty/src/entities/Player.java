package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import main.Game;
import utilz.LoadSave;

public class Player extends Character {
	// Player-specific constants
	private static final int HITBOX_WIDTH = (int) (20 * Game.SCALE);
	private static final int HITBOX_HEIGHT = (int) (30 * Game.SCALE);
	private static final int X_DRAW_OFFSET = 44;
	private static final int Y_DRAW_OFFSET = 36;

	public Player(float x, float y, int width, int height) {
		super(x, y, width, height);
		// Set player-specific health bar colors
		healthBarFillColor = new Color(0, 180, 0); // Green
		healthBarBgColor = new Color(0, 80, 0); // Dark Green

		loadAnimations();
		initHitbox(x, y, HITBOX_WIDTH, HITBOX_HEIGHT);
		xDrawOffset = X_DRAW_OFFSET * Game.SCALE;
		yDrawOffset = Y_DRAW_OFFSET * Game.SCALE;
	}

	@Override
	public void update() {
		updateCharacter();
	}

	@Override
	public void render(Graphics g) {
		renderCharacter(g, animations);
		renderHealthBar(g); //health bar rendering
	}

	@Override
	public void loadAnimations() {
		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);
		animations = new BufferedImage[9][12];
		for (int j = 0; j < animations.length; j++)
			for (int i = 0; i < animations[j].length; i++)
				animations[j][i] = img.getSubimage(i * 112, j * 72, 112, 72);
	}

	@Override
	public float getCharacterX() {
		return hitbox.x;
	}

	@Override
	public float getCharacterY() {
		return hitbox.y;
	}

	// Player-specific methods
	public void setLightAttack(boolean light_attack) {
		this.light_attack = light_attack;
	}

	public void setHeavyAttack(boolean heavy_attack) {
		this.heavy_attack = heavy_attack;
	}

	public void toggleLightAttack(boolean light_attack) {
		this.light_attack = true;
	}

	public void toggleHeavyAttack(boolean heavy_attack) {
		this.heavy_attack = true;
	}

	public void toggleDeath(boolean death) {
		this.death = true;
	}

	public void toggleHit(boolean hit) {
		this.hit = true;
	}
}