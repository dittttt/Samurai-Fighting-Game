package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import main.Game;
import utilz.LoadSave;

public class Player extends Entity {

	private BufferedImage[][] animations;
	private int aniTick, aniIndex, aniSpeed = 25;
	private int playerAction = IDLE;
	private boolean moving = false, light_attack = false, heavy_attack = false;
	private boolean roll = false, rollingForward = false, canMove = true, normalMirrorState = true;
	private int rollDuration = 3, rollCounter = 0;
	private boolean continueMovingAfterRoll = false;
	private boolean left, right, jump, run;
	private float playerSpeed = 0.6f * Game.SCALE;
	private int[][] lvlData;
	// OLD is 21, 4 || NEW is 43/48, 34
	private int xDrawOffsetValue = 44;
	private float xDrawOffset = xDrawOffsetValue * Game.SCALE;
	private float yDrawOffset = 36 * Game.SCALE;

//	JUMPING / GRAVITY
	public float airSpeed = 0f;
	private float gravity = 0.04f * Game.SCALE;
	private float jumpSpeed = -2.25f * Game.SCALE;
	private float fallSpeedAfterCollision = 1.0f * Game.SCALE;
	private boolean inAir = false;

	public Player(float x, float y, int width, int height) {
		super(x, y, width, height);
		loadAnimations();
		// OLD is 24 x 32 || NEW is 24 x 36
		initHitbox(x, y, (int) (20 * Game.SCALE), (int) (30 * Game.SCALE));
	}

	public void update() {
		updatePos();
		updateAnimationTick();
		setAnimation();
		updateXDrawOffset();
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
		// Draw hitbox
		drawHitbox(g);

		// Draw character
		BufferedImage currentFrame = animations[playerAction][aniIndex];

		if (normalMirrorState) {
			g.drawImage(currentFrame, (int) (hitbox.x - xDrawOffset), (int) (hitbox.y - yDrawOffset), width, height,
					null);
		} else {
			// Flip the image horizontally and adjust the width parameter
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
					normalMirrorState = right; // Mirror state based on the direction of movement
				} else if (run && (!left || !right)) {
					playerAction = IDLE;
				} else if (left || right) {
					playerAction = WALKING;
					normalMirrorState = right; // Mirror state based on the direction of movement
				}
			} else {
				playerAction = IDLE;
			}

			if (light_attack)
				playerAction = LIGHT_ATTACK;

			if (heavy_attack)
				playerAction = HEAVY_ATTACK;

			if (roll) {
				playerAction = ROLL;
				if (!rollingForward) {
					rollingForward = true;
					rollCounter = 0;
					canMove = false; // Set canMove to false at the start of the roll
				}

				if (rollingForward && rollCounter < rollDuration) {
					float xSpeed = playerSpeed * 2.0f * (normalMirrorState ? 1 : -1);
					updateXPos(xSpeed);
					rollCounter++;
				} else {
					rollingForward = false;
					rollCounter = 0; // Reset rollCounter after the roll animation is completed
					if (right || left) {
						// If right or left is still held after the roll, continue moving
						float xSpeed = playerSpeed * (right ? 1 : -1);
						updateXPos(xSpeed);
					}
					canMove = true; // Set canMove to true after the roll duration
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
				playerSpeed = 0.6f * Game.SCALE;
				canMove = false;
				aniSpeed = 20;
				continueMovingAfterRoll = left || right; // Set the flag
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
				}else if (run && (!left || !right)) {
					aniSpeed = 30;
					playerSpeed = 0.6f * Game.SCALE;
				}
			} 
			
			if(light_attack || heavy_attack) {
				aniSpeed = 15;
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

				// Add this block to continue moving after the roll animation
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

		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

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

	public void toggleRoll(boolean heavy_attack) {
		this.roll = true;
	}

}
