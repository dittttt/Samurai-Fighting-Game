
package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import main.Game;
import utilz.LoadSave;

public class Enemy extends Character {
	// Enemy-specific constants
	private static final int HITBOX_WIDTH = (int) (20 * Game.SCALE);
	private static final int HITBOX_HEIGHT = (int) (30 * Game.SCALE);
	private static final int X_DRAW_OFFSET = 44;
	private static final int Y_DRAW_OFFSET = 36;

	// Enemy-specific fields
	private float playerX;
	private float playerY;
	private float distanceToPlayer;
	private long lastAttackTime;
	private long attackCooldown = 1500; // 1.5 seconds
	private Player player; // Reference to player for attack detection
	private Random random = new Random();

	// AI States
	private enum State {
		IDLE, PATROLLING, CHASING, ATTACKING
	}

	private State currentState = State.IDLE;

	// Patrolling variables
	private float patrolSpeed = 0.3f * Game.SCALE;
	private float patrolRange = 100;
	private boolean patrolDirectionRight = true;

	public Enemy(float x, float y, int width, int height, Player player) {
		super(x, y, width, height);
		this.player = player;

		healthBarFillColor = new Color(200, 0, 0); // Red
		healthBarBgColor = new Color(80, 0, 0); // Dark Red

		loadAnimations();
		initHitbox(x, y, HITBOX_WIDTH, HITBOX_HEIGHT);	
		xDrawOffset = X_DRAW_OFFSET * Game.SCALE;
		yDrawOffset = Y_DRAW_OFFSET * Game.SCALE;

		// Set facing direction based on player's position
		normalMirrorState = player.getCharacterX() > x;
	}

	@Override
	public void update() {
		calculateDistanceToPlayer();
		switch (currentState) {
		case IDLE:
			handleIdleState();
			break;
		case PATROLLING:
			handlePatrollingState();
			break;
		case CHASING:
			handleChasingState();
			break;
		case ATTACKING:
			handleAttackingState();
			break;
		}
		updateCharacter();
	}

	@Override
	public void render(Graphics g) {
		renderCharacter(g, animations);
		renderHealthBar(g); // health bar rendering
	}

	@Override
	public void loadAnimations() {
		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.ENEMY_ATLAS);
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

	// AI State Handlers
	private void handleIdleState() {
		if (distanceToPlayer <= 300) {
			currentState = State.CHASING;
		} else if (random.nextInt(100) < 5) { // Random chance to start patrolling
			currentState = State.PATROLLING;
		}
	}

	private void handlePatrollingState() {
		if (distanceToPlayer <= 300) {
			currentState = State.CHASING;
		} else {
			patrol();
		}
	}

	private void handleChasingState() {
		if (distanceToPlayer > 300) {
			currentState = State.IDLE;
		} else if (distanceToPlayer <= 200) {
			currentState = State.ATTACKING;
		} else {
			followPlayer(playerX, playerY);
		}
	}

	private void handleAttackingState() {
		long currentTime = System.currentTimeMillis();
		if (distanceToPlayer > 200) {
			currentState = State.CHASING;
		} else if (currentTime - lastAttackTime >= attackCooldown) {
			attack();
			lastAttackTime = currentTime;
			attackCooldown = 1000 + random.nextInt(1000); // Randomize cooldown
		}
	}

	// Patrolling logic
	private void patrol() {
		float patrolSpeed = this.patrolSpeed * (patrolDirectionRight ? 1 : -1);
		updateXPos(patrolSpeed);
		if (Math.abs(hitbox.x - x) >= patrolRange) {
			patrolDirectionRight = !patrolDirectionRight;
		}
	}

	// Attack logic
	private void attack() {
		light_attack = true;
		player.setHealth(player.getHealth() - 10);
		System.out.println("Enemy Attacked, health = " + player.getHealth());
	}

	private void calculateDistanceToPlayer() {
		playerX = player.getCharacterX(); // Update playerX dynamically
		playerY = player.getCharacterY(); // Update playerY dynamically
		float dx = playerX - hitbox.x;
		float dy = playerY - hitbox.y;
		distanceToPlayer = (float) Math.sqrt(dx * dx + dy * dy);
	}

	private void followPlayer(float playerX, float playerY) {
		if (!inAir) {
			if (hitbox.x < playerX) {
				right = true;
				left = false;
				moving = true; // Ensure movement is active
			} else if (hitbox.x > playerX) {
				right = false;
				left = true;
				moving = true; // Ensure movement is active
			} else {
				right = false;
				left = false;
				moving = false; // Stop movement when aligned with the player
			}
		}
	}

}
