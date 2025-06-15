package entities;

import static utilz.Constants.PlayerConstants.GetSpriteAmount;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Random;
import static utilz.Constants.PlayerConstants.*;

import main.Game;
import utilz.LoadSave;

public class Enemy extends Character {
	// Enemy-specific constants
	private static final int HITBOX_WIDTH = (int) (20 * Game.SCALE);
	private static final int HITBOX_HEIGHT = (int) (30 * Game.SCALE);
	private static final int X_DRAW_OFFSET = 44;
	private static final int Y_DRAW_OFFSET = 36;
	private static final int LIGHT_ATTACK_DAMAGE = 10;
	private static final int HEAVY_ATTACK_DAMAGE = 20;
	private static final int ATTACK_RANGE = 50;

	private enum State {
		IDLE, PATROLLING, CHASING, ATTACKING, DEFENSIVE, RECOVERING, HIT
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
				// Only reset these flags if not in hit state
				if (characterAction != HIT) {
					light_attack = false;
					heavy_attack = false;
					roll = false;
				}
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
		// Immediate reactions first
		if (hit) {
			currentState = State.HIT;
			return;
		}

		// More dynamic defensive reactions
		if (player.isAttacking() && distanceToPlayer < 150 && currentTime - lastRollTime >= rollCooldown) {
			if (random.nextFloat() < 0.8f) { // 80% chance to dodge
				currentState = State.DEFENSIVE;
				return;
			}
		}

		// State transitions
		switch (currentState) {
		case HIT:
			if (!hit)
				currentState = State.RECOVERING;
			break;
		case RECOVERING:
			if (currentTime - lastHitTime > 500) { // 0.5s recovery
				currentState = distanceToPlayer < 200 ? State.CHASING : State.IDLE;
			}
			break;
		case IDLE:
			if (distanceToPlayer < 300) {
				currentState = State.CHASING;
			} else if (random.nextInt(100) < 3) { // Reduced patrol chance
				currentState = State.PATROLLING;
			}
			break;
		case PATROLLING:
			if (distanceToPlayer < 400) { // Increased detection range
				currentState = State.CHASING;
			} else {
				patrol();
			}
			break;
		case CHASING:
			if (distanceToPlayer > 400) {
				currentState = State.IDLE;
			} else if (distanceToPlayer <= ATTACK_RANGE * 1.2f) {
				currentState = State.ATTACKING;
			} else {
				followPlayer();

				// Smarter distance closing
				if (distanceToPlayer > 120 && random.nextFloat() < 0.15f
						&& currentTime - lastRollTime >= rollCooldown) {
					roll = true;
					lastRollTime = currentTime;
				}
			}
			break;
		case ATTACKING:
			if (distanceToPlayer > ATTACK_RANGE * 1.5f) {
				currentState = State.CHASING;
			} else if (currentTime - lastAttackTime >= attackCooldown) {
				attack();
				lastAttackTime = currentTime;
				attackCooldown = 800 + random.nextInt(1200); // More varied cooldown
			}
			break;
		case DEFENSIVE:
			roll = true;
			normalMirrorState = playerX < hitbox.x;
			lastRollTime = currentTime;
			currentState = State.CHASING;
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
			if (distanceToPlayer > 100 && random.nextFloat() < 0.1f
					&& System.currentTimeMillis() - lastRollTime >= rollCooldown) {
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
		if (distanceToPlayer < ATTACK_RANGE * 0.7f) {
			heavy_attack = true;
			if (checkPlayerInRange() && aniIndex >= GetSpriteAmount(characterAction) / 2 && !attackProcessed) {
				player.takeDamage(HEAVY_ATTACK_DAMAGE);
				attackProcessed = true;
			}
		} else {
			light_attack = true;
			if (checkPlayerInRange() && aniIndex >= GetSpriteAmount(characterAction) / 3 && !attackProcessed) {
				player.takeDamage(LIGHT_ATTACK_DAMAGE);
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

		// More precise movement with smaller buffer
		if (hitbox.x < playerX - 5) {
			right = true;
			normalMirrorState = true;
		} else if (hitbox.x > playerX + 5) {
			left = true;
			normalMirrorState = false;
		}

		// Adjust speed based on distance
		if (distanceToPlayer > 200) {
			run = true;
		} else {
			run = random.nextFloat() < 0.7f; // 70% chance to run when close
		}

		moving = right || left;
	}

	private void handleHitState(long currentTime) {
		// Stop all movement during hit
		left = false;
		right = false;
		moving = false;

		// Stay in hit state until animation completes
		if (!hit) {
			currentState = State.RECOVERING;
			lastHitTime = currentTime;
		}
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