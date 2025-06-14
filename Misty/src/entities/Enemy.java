package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;

import main.Game;
import utilz.LoadSave;
import static utilz.Constants.PlayerConstants.*;

public class Enemy extends Character {
	// Enemy-specific constants
	private static final int HITBOX_WIDTH = (int) (20 * Game.SCALE);
	private static final int HITBOX_HEIGHT = (int) (30 * Game.SCALE);
	private static final int X_DRAW_OFFSET = 44;
	private static final int Y_DRAW_OFFSET = 36;
	private static final int LIGHT_ATTACK_DAMAGE = 10;
	private static final int HEAVY_ATTACK_DAMAGE = 20;
	private static final int ATTACK_RANGE = 50;

	// AI States
	private enum State {
		IDLE, PATROLLING, CHASING, ATTACKING, DEFENSIVE, RECOVERING
	}

	// Enemy-specific fields
	private float playerX;
	private float playerY;
	private float distanceToPlayer;
	private long lastAttackTime;
	private long attackCooldown = 1500; // 1.5 seconds
	private Player player;
	private Random random = new Random();
	private State currentState = State.IDLE;
	private long lastDecisionTime;
	private final long DECISION_INTERVAL = 500; // 0.5 seconds
	private float patrolSpeed = 0.3f * Game.SCALE;
	private float patrolRange = 100;
	private boolean patrolDirectionRight = true;
	private long lastRollTime;
	private long rollCooldown = 3000; // 3 seconds
	private boolean attackProcessed = false;
	private float startX; // For patrolling

	public Enemy(float x, float y, int width, int height, Player player) {
		super(x, y, width, height);
		this.player = player;
		this.startX = x;
		health = 100;
		healthBarFillColor = new Color(200, 0, 0);
		healthBarBgColor = new Color(80, 0, 0);
		loadAnimations();
		initHitbox(x, y, HITBOX_WIDTH, HITBOX_HEIGHT);
		xDrawOffset = X_DRAW_OFFSET * Game.SCALE;
		yDrawOffset = Y_DRAW_OFFSET * Game.SCALE;
		normalMirrorState = player.getCharacterX() > x;
	}

	@Override
	public void update() {
		calculateDistanceToPlayer();

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastDecisionTime >= DECISION_INTERVAL) {
			lastDecisionTime = currentTime;
			makeDecision(currentTime);
		}

		updateCharacter();
	}

	@Override
	protected void updateAnimationTick() {
		aniTick++;
		if (aniTick >= aniSpeed) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(characterAction)) {
				aniIndex = 0;
				light_attack = false;
				heavy_attack = false;
				roll = false;
				death = false;
				hit = false;
				attackProcessed = false;
			}
		}

		if (invulnerable && System.currentTimeMillis() - lastHitTime >= invulnerabilityDuration) {
			invulnerable = false;
		}
	}

	private void makeDecision(long currentTime) {
	    if (hit) {
	        currentState = State.RECOVERING;
	        return;
	    }

	    // More immediate defensive reactions
	    if (player.isAttacking() && distanceToPlayer < 150 && currentTime - lastRollTime >= rollCooldown) {
	        if (random.nextFloat() < 0.8f) { // Increased chance to dodge
	            currentState = State.DEFENSIVE;
	            return;
	        }
	    }

	    // More aggressive when close
	    if (distanceToPlayer <= ATTACK_RANGE * 1.5f) {
	        currentState = State.ATTACKING;
	        return;
	    }

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
			handleAttackingState(currentTime);
			break;
		case DEFENSIVE:
			handleDefensiveState(currentTime);
			break;
		case RECOVERING:
			if (!hit) {
				currentState = State.IDLE;
			}
			break;
		}
	}

	private void handleIdleState() {
		if (distanceToPlayer <= 300) {
			currentState = State.CHASING;
		} else if (random.nextInt(100) < 5) {
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
	    } else if (distanceToPlayer <= ATTACK_RANGE) {
	        currentState = State.ATTACKING;
	    } else {
	        // More aggressive chasing
	        run = true;
	        followPlayer();
	        
	        // Occasionally roll forward to close distance
	        if (distanceToPlayer > 100 && random.nextFloat() < 0.1f && 
	            System.currentTimeMillis() - lastRollTime >= rollCooldown) {
	            roll = true;
	            lastRollTime = System.currentTimeMillis();
	        }
	    }
	}

	private void handleAttackingState(long currentTime) {
		if (distanceToPlayer > ATTACK_RANGE) {
			currentState = State.CHASING;
		} else if (currentTime - lastAttackTime >= attackCooldown) {
			attack();
			lastAttackTime = currentTime;
			attackCooldown = 1000 + random.nextInt(1000);
		}
	}

	private void handleDefensiveState(long currentTime) {
		roll = true;
		normalMirrorState = playerX < hitbox.x;
		lastRollTime = currentTime;
		currentState = State.CHASING;
	}

	private void patrol() {
		float speed = patrolSpeed * (patrolDirectionRight ? 1 : -1);
		updateXPos(speed);

		if (Math.abs(hitbox.x - startX) >= patrolRange) {
			patrolDirectionRight = !patrolDirectionRight;
		}
	}

	private void attack() {
	    if (random.nextFloat() < 0.7f) {
	        light_attack = true;
	        if (checkPlayerInRange() && aniIndex >= GetSpriteAmount(characterAction) / 2 && !attackProcessed) {
	            player.takeDamage(LIGHT_ATTACK_DAMAGE);
	            attackProcessed = true;
	        }
	    } else {
	        heavy_attack = true;
	        if (checkPlayerInRange() && aniIndex >= GetSpriteAmount(characterAction) / 2 && !attackProcessed) {
	            player.takeDamage(HEAVY_ATTACK_DAMAGE);
	            attackProcessed = true;
	        }
	    }
	}
	
	private boolean checkPlayerInRange() {
		return distanceToPlayer <= ATTACK_RANGE;
	}

	private void calculateDistanceToPlayer() {
		playerX = player.getCharacterX();
		playerY = player.getCharacterY();
		float dx = playerX - hitbox.x;
		float dy = playerY - hitbox.y;
		distanceToPlayer = (float) Math.sqrt(dx * dx + dy * dy);
	}

	private void followPlayer() {
	    right = false;
	    left = false;

	    if (hitbox.x < playerX - 10) { // Reduced buffer from 20 to 10
	        right = true;
	        normalMirrorState = true;
	    } else if (hitbox.x > playerX + 10) { // Reduced buffer
	        left = true;
	        normalMirrorState = false;
	    }

	    moving = right || left;
	}

	@Override
	public void render(Graphics g) {
		renderCharacter(g, animations);
		renderHealthBar(g);
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
}