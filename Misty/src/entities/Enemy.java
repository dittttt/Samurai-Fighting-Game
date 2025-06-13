package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import main.Game;
import utilz.LoadSave;

public class Enemy extends Entity {

	private BufferedImage[][] animations;
	private int aniTick, aniIndex, aniSpeed = 25;
	private int playerAction = IDLE;
	private boolean moving = false, light_attack = false, heavy_attack = false;
	private boolean roll = false, rollingForward = false, canMove = true, normalMirrorState = true;
	private int rollDuration = 1, rollCounter = 0;
	private boolean continueMovingAfterRoll = false;
	private boolean left, right, jump, run, death, hit;
	private float playerSpeed = 0.6f * Game.SCALE;
	private int[][] lvlData;
	private int xDrawOffsetValue = 44;
	private float xDrawOffset = xDrawOffsetValue * Game.SCALE;
	private float yDrawOffset = 36 * Game.SCALE;

	public float airSpeed = 0f;
	private float gravity = 0.04f * Game.SCALE;
	private float jumpSpeed = -2.25f * Game.SCALE;
	private float fallSpeedAfterCollision = 1.0f * Game.SCALE;
	private boolean inAir = false;
	private float playerX;
	private float playerY;

	private float distanceToPlayer;

	private long lastAttackTime;
	private long attackCooldown = 1500; // Cooldown period in milliseconds (2 seconds)
	private int enemyHealth = 100; 
	
	public int getHealth() {
		return enemyHealth;
	}
	
	public Enemy(float x, float y, int width, int height) {
		super(x, y, width, height);
		loadAnimations();
		initHitbox(x, y, (int) (20 * Game.SCALE), (int) (30 * Game.SCALE));
	}

	public void update(float playerX, float playerY) {
		updatePos();
		updateAnimationTick();
		setAnimation();
		updateXDrawOffset();
		setPlayerCoordinates(playerX, playerY);
		followPlayer(playerX, playerY);

		// Calculate the distance to the player and update the variable
		calculateDistanceToPlayer();

		long currentTime = System.currentTimeMillis();
        if (distanceToPlayer <= 200) {
            stopMovingAndAttack();
            if (currentTime - lastAttackTime >= attackCooldown) {
                // Check if the distance is less than or equal to 25
            	attack();
            	isPlayerAttacking();
                // Update the last attack time
                lastAttackTime = currentTime;
            }
        }
	}
	
	private void playerLightAttacked() {
    	enemyHealth -= 10;
		System.out.println("You Light Attacked the Enemy | Enemy Health = " + enemyHealth);
		setPlayerAction(HIT);
	}
	private void playerHeavyAttacked() {
		enemyHealth -= 20;
		System.out.println("You Heavy Attacked the Enemy | Enemy Health = " + enemyHealth);
		setPlayerAction(HIT);
	}

	private void setPlayerAction(int action) {
    	playerAction = action;
    }
	
	private void isPlayerAttacking() {
        // Check if the player is attacking (either light or heavy attack)
        if(Player.light_attack) {
        	playerLightAttacked();
        }else if(Player.heavy_attack) {
        	playerHeavyAttacked();
        }
    }
	
	private void stopMovingAndAttack() {
		// Stop moving logic (setLeft, setRight, etc. based on your game logic)
		setLeft(false);
		setRight(false);
	}

	private void attack() {
		setLightAttack(true);
		Player.health -= 10;
		System.out.println("Enemy Attacked, health = " + Player.health);
	}

	// FOLLOW PLAYER BEHAVIOR
	public float getDistanceToPlayer() {
		return distanceToPlayer;
	}

	private void calculateDistanceToPlayer() {
		// Use the player's coordinates (this.playerX, this.playerY) and the enemy's
		// coordinates (hitbox.x, hitbox.y)
		// to calculate the distance between them. Update the distanceToPlayer variable.
		float dx = this.playerX - hitbox.x;
		float dy = this.playerY - hitbox.y;
		distanceToPlayer = (float) Math.sqrt(dx * dx + dy * dy);
	}

	public void setPlayerCoordinates(float playerX, float playerY) {
		this.playerX = playerX;
		this.playerY = playerY;
	}

	public void followPlayer(float playerX, float playerY) {
		if (!inAir) {
			if (hitbox.x < playerX) {
				// Move towards the player on the x-axis
				setRight(true);
				setLeft(false);
			} else if (hitbox.x > playerX) {
				// Move towards the player on the x-axis
				setRight(false);
				setLeft(true);
			} else {
				// Stop moving on the x-axis
				setRight(false);
				setLeft(false);
			}

			if (hitbox.y < playerY) {
				// Move towards the player on the y-axis
				// (You might want to adjust this part based on your game's coordinate system)
				// In this example, moving down means increasing the y-coordinate
				setJump(false); // Prevent jumping while following the player
			} else if (hitbox.y > playerY) {
				// Move towards the player on the y-axis
				// (You might want to adjust this part based on your game's coordinate system)
				// In this example, moving up means decreasing the y-coordinate
				setJump(false); // Prevent jumping while following the player
			}

			// Adjust other actions or conditions as needed based on your game logic
			// For example, you might want to trigger attacks or other behaviors here
		}
	}

	private void updateXDrawOffset() {
		if (left) {
			xDrawOffsetValue = 48; // Change to the desired value when moving left
		} else if (right) {
			xDrawOffsetValue = 44; // Change to the desired value when moving right
		}
		xDrawOffset = xDrawOffsetValue * Game.SCALE;
	}

	public void render(Graphics g) {
//		drawHitbox(g); 

		BufferedImage currentFrame = animations[playerAction][aniIndex];

		if (normalMirrorState) {
			g.drawImage(currentFrame, (int) (hitbox.x - xDrawOffset), (int) (hitbox.y - yDrawOffset), width, height,
					null);
		} else {
			g.drawImage(currentFrame, (int) (hitbox.x - xDrawOffset + width), (int) (hitbox.y - yDrawOffset), -width,
					height, null);
		}
	}

	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= aniSpeed) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(playerAction)) {
				aniIndex = 0;
				light_attack = false;
				heavy_attack = false;
				roll = false;
				death = false;
			}
		}
	}

	private void setAnimation() {
		int startAni = playerAction;

		if (inAir) {
			if (airSpeed < 0)
				playerAction = JUMP;
			else
				playerAction = FALLING;
		} else {
			if (moving) {
				if (run && (left || right)) {
					playerAction = RUNNING;
					normalMirrorState = right;
				} else if (run && (!left || !right)) {
					playerAction = IDLE;
				} else if (left || right) {
					playerAction = WALKING;
					normalMirrorState = right;
				}
			} else {
				playerAction = IDLE;
			}

			if (light_attack)
				playerAction = LIGHT_ATTACK;
			if (heavy_attack)
				playerAction = HEAVY_ATTACK;
			if (death)
				playerAction = HIT;

			if (roll) {
				playerAction = ROLL;
				if (!rollingForward) {
					rollingForward = true;
					rollCounter = 0;
					canMove = false;
				}

				if (rollingForward && rollCounter < rollDuration) {
					float xSpeed = playerSpeed * 2.0f * (normalMirrorState ? 1 : -1);
					updateXPos(xSpeed);
					rollCounter++;
				} else {
					rollingForward = false;
					rollCounter = 0;
					if (right || left) {
						float xSpeed = playerSpeed * (right ? 1 : -1);
						updateXPos(xSpeed);
					}
					canMove = true;
				}
			}
		}

		if (startAni != playerAction) {
			resetAniTick();
		}
	}

	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() {
		moving = false;

		if (!inAir) {
			if (roll) {
				playerSpeed = 0.75f * Game.SCALE;
				canMove = false;
				aniSpeed = 20;
				continueMovingAfterRoll = left || right;
			} else {
				canMove = true;
			}
		}

		if (canMove) {
			if (!run) {
				aniSpeed = 30;
				if (left || right) {
					playerSpeed = 0.6f * Game.SCALE;
				}
			} else if (!light_attack || !heavy_attack) {
				if (run && (left || right)) {
					aniSpeed = 20;
					playerSpeed = 0.85f * Game.SCALE;
				} else if (run && (!left || !right)) {
					aniSpeed = 30;
					playerSpeed = 0.6f * Game.SCALE;
				}
			}

			if (light_attack) {
				aniSpeed = 20;
			}

			if (heavy_attack) {
				aniSpeed = 21;
			}

			if (death) {
				aniSpeed = 12;
			}

			if (jump) {
				jump();
			}

			if (!(left || right || inAir || run)) {
				return;
			}

			float xSpeed = 0;

			if (left) {
				xSpeed -= playerSpeed;
			} else if (right) {
				xSpeed += playerSpeed;
			}

			if (!inAir) {
				if (!IsEntityOnFloor(hitbox, lvlData)) {
					inAir = true;
				}
			}

			if (inAir) {
				if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
					hitbox.y += airSpeed;
					airSpeed += gravity;
					updateXPos(xSpeed);
				} else {
					hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
					if (airSpeed > 0) {
						resetInAir();
					} else {
						airSpeed = fallSpeedAfterCollision;
					}
					updateXPos(xSpeed);
				}
			} else {
				updateXPos(xSpeed);
				moving = true;

				if (roll && !rollingForward && continueMovingAfterRoll) {
					float additionalXSpeed = playerSpeed * (right ? 1 : -1);
					updateXPos(additionalXSpeed);
				}
			}
		}
	}

	private void jump() {
		if (inAir)
			return;
		inAir = true;
		airSpeed = jumpSpeed;
	}

	private void resetInAir() {
		inAir = false;
		airSpeed = 0;
	}

	private void updateXPos(float xSpeed) {
		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
			hitbox.x += xSpeed;
		} else {
			hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
		}
	}

	private void loadAnimations() {
		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.ENEMY_ATLAS);

		animations = new BufferedImage[9][12];
		for (int j = 0; j < animations.length; j++)
			for (int i = 0; i < animations[j].length; i++)
				animations[j][i] = img.getSubimage(i * 112, j * 72, 112, 72);
	}

	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	public void resetDirBooleans() {
		left = false;
		right = false;
		run = false;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public void setJump(boolean jump) {
		this.jump = jump;
	}

	public boolean isRunning() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

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

	public void toggleRoll(boolean roll) {
		this.roll = true;
	}
	
	public void toggleHit(boolean hit) {
		this.hit = true;
	}
	
	public void toggleDeath(boolean death) {
		this.death = true;
	}
}
