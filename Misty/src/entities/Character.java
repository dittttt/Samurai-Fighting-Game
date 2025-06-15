package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import main.Game;

public abstract class Character extends Entity {
    protected BufferedImage[][] animations;
    protected int aniTick, aniIndex, aniSpeed = 25;
    protected int characterAction = IDLE;
    protected boolean moving = false;
    protected boolean light_attack = false;
    protected boolean heavy_attack = false;
    protected boolean roll = false;
    protected boolean rollingForward = false;
    protected boolean canMove = true;
    protected boolean normalMirrorState = true;
    protected int rollDuration = 1;
    protected int rollCounter = 0;
    protected boolean continueMovingAfterRoll = false;
    protected boolean left, right, jump, run, death, hit;
    protected float characterSpeed = 0.6f * Game.SCALE;
    protected int[][] lvlData;
    protected int xDrawOffsetValue = 44;
    protected float xDrawOffset = xDrawOffsetValue * Game.SCALE;
    protected float yDrawOffset = 36 * Game.SCALE;
    protected float airSpeed = 0f;
    protected float gravity = 0.04f * Game.SCALE;
    protected float jumpSpeed = -2.25f * Game.SCALE;
    protected float fallSpeedAfterCollision = 1.0f * Game.SCALE;
    protected boolean inAir = false;
    protected int health = 100;
    protected Color healthBarFillColor;
    protected Color healthBarBgColor;
    protected Rectangle2D.Float attackHitbox;
    protected boolean attackConnected = false;
    protected boolean invulnerable = false;
    protected long lastHitTime;
    protected long invulnerabilityDuration = 500; // 0.5 seconds

    public Character(float x, float y, int width, int height) {
        super(x, y, width, height);
        initHitbox(x, y, (int) (20 * Game.SCALE), (int) (30 * Game.SCALE));
        attackHitbox = new Rectangle2D.Float(x, y, 40 * Game.SCALE, 20 * Game.SCALE);
    }

    protected void updateCharacter() {
        updatePos();
        updateAnimationTick();
        setAnimation();
        updateXDrawOffset();
        updateAttackHitbox();
    }

    protected void updateAttackHitbox() {
        if (light_attack || heavy_attack) {
            float xPos = hitbox.x + (normalMirrorState ? hitbox.width : -40 * Game.SCALE);
            attackHitbox.x = xPos;
            attackHitbox.y = hitbox.y;
        }
    }
    
    protected void updateXDrawOffset() {
        if (left) {
            xDrawOffsetValue = 48;
        } else if (right) {
            xDrawOffsetValue = 44;
        }
        xDrawOffset = xDrawOffsetValue * Game.SCALE;
    }
    
    public void renderCharacter(Graphics g, BufferedImage[][] animations) {
        BufferedImage currentFrame = animations[characterAction][aniIndex];
        
        if (normalMirrorState) {
            g.drawImage(currentFrame, (int) (hitbox.x - xDrawOffset), (int) (hitbox.y - yDrawOffset), width, height, null);
        } else {
            g.drawImage(currentFrame, (int) (hitbox.x - xDrawOffset + width), (int) (hitbox.y - yDrawOffset), -width, height, null);
        }
    }
    
    protected void renderHealthBar(Graphics g) {
        // Health bar dimensions
        int healthBarWidth = 85;
        int healthBarHeight = 8;
        int healthBarOffset = -145;
        
        // Calculate position relative to character
        int x = (int) (hitbox.x - xDrawOffset + width / 2 - healthBarWidth / 2);
        int y = (int) (hitbox.y - yDrawOffset - healthBarHeight - healthBarOffset);
        int currentHealthWidth = (int) ((health / 100.0) * healthBarWidth);

        // Draw health bar (subclasses will set colors)
        g.setColor(healthBarBgColor);
        g.fillRect(x, y, healthBarWidth, healthBarHeight);
        
        g.setColor(healthBarFillColor);
        g.fillRect(x, y, currentHealthWidth, healthBarHeight);
        
        // Draw border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, healthBarWidth, healthBarHeight);
        
        // Draw health text
        g.setColor(Color.WHITE);
        g.drawString(health + "/100", x + healthBarWidth / 2 - 15, y - 2);
    }
    
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
            }
        }
        
        // Check invulnerability
        if (invulnerable && System.currentTimeMillis() - lastHitTime >= invulnerabilityDuration) {
            invulnerable = false;
        }
    }
    
    protected void setAnimation() {
        int startAni = characterAction;
        
        if (inAir) {
            if (airSpeed < 0)
                characterAction = JUMP;
            else
                characterAction = FALLING;
        } else {
            if (moving) {
                if (run && (left || right)) {
                    characterAction = RUNNING;
                    normalMirrorState = right;
                } else if (run && (!left || !right)) {
                    characterAction = IDLE;
                } else if (left || right) {
                    characterAction = WALKING;
                    normalMirrorState = right;
                }
            } else {
                characterAction = IDLE;
            }
            
            if (light_attack)
                characterAction = LIGHT_ATTACK;
            if (heavy_attack)
                characterAction = HEAVY_ATTACK;
            if (hit)
                characterAction = HIT;
            if (death)
                characterAction = DEATH;
            
            handleRollAnimation();
        }
        
        if (startAni != characterAction) {
            resetAniTick();
        }
    }
    
    private void handleRollAnimation() {
        if (roll) {
            characterAction = ROLL;
            if (!rollingForward) {
                rollingForward = true;
                rollCounter = 0;
                canMove = false;
            }
            
            if (rollingForward && rollCounter < rollDuration) {
                float xSpeed = characterSpeed * 2.0f * (normalMirrorState ? 1 : -1);
                updateXPos(xSpeed);
                rollCounter++;
            } else {
                rollingForward = false;
                rollCounter = 0;
                if (right || left) {
                    float xSpeed = characterSpeed * (right ? 1 : -1);
                    updateXPos(xSpeed);
                }
                canMove = true;
            }
        }
    }
    
    public void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }
    
    protected void updatePos() {
        moving = false;
        
        if (!inAir) {
            if (roll) {
                characterSpeed = 0.75f * Game.SCALE;
                canMove = false;
                aniSpeed = 20;
                continueMovingAfterRoll = left || right;
            } else {
                canMove = true;
            }
        }
        
        if (canMove) {
            handleMovementSpeed();
            handleSpecialAttacks();
            
            if (jump) {
                jump();
            }
            
            if (!(left || right || inAir || run)) {
                return;
            }
            
            float xSpeed = calculateXSpeed();
            handleAirMovement(xSpeed);
        }
    }
    
    private void handleMovementSpeed() {
        if (!run) {
            aniSpeed = 30;
            if (left || right) {
                characterSpeed = 0.6f * Game.SCALE;
            }
        } else if (!light_attack || !heavy_attack) {
            if (run && (left || right)) {
                aniSpeed = 20;
                characterSpeed = 0.85f * Game.SCALE;
            } else if (run && (!left || !right)) {
                aniSpeed = 30;
                characterSpeed = 0.6f * Game.SCALE;
            }
        }
    }
    
    private void handleSpecialAttacks() {
        if (light_attack) {
            aniSpeed = 20;
        }
        if (heavy_attack) {
            aniSpeed = 21;
        }
        if (hit) {
            aniSpeed = 12;
        }
        if (death) {
            aniSpeed = 12;
        }
    }
    
    private float calculateXSpeed() {
        float xSpeed = 0;
        if (left) {
            xSpeed -= characterSpeed;
        } else if (right) {
            xSpeed += characterSpeed;
        }
        return xSpeed;
    }
    
    private void handleAirMovement(float xSpeed) {
        if (!inAir) {
            if (!IsEntityOnFloor(hitbox, lvlData)) {
                inAir = true;
            }
        }
        
        if (inAir) {
            handleVerticalMovement(xSpeed);
        } else {
            updateXPos(xSpeed);
            moving = true;
            handlePostRollMovement();
        }
    }
    
    private void handleVerticalMovement(float xSpeed) {
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
    }
    
    private void handlePostRollMovement() {
        if (roll && !rollingForward && continueMovingAfterRoll) {
            float additionalXSpeed = characterSpeed * (right ? 1 : -1);
            updateXPos(additionalXSpeed);
        }
    }
    
    protected void jump() {
        if (inAir)
            return;
        inAir = true;
        airSpeed = jumpSpeed;
    }
    
    protected void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }
    
    protected void updateXPos(float xSpeed) {
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x += xSpeed;
        } else {
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
        }
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
    
    public void takeDamage(int damage) {
        if (!invulnerable && !death) {
            health = Math.max(0, health - damage);
            hit = true;
            invulnerable = true;
            lastHitTime = System.currentTimeMillis();
            // Force immediate animation change
            characterAction = HIT;
            resetAniTick();
            aniSpeed = 12; // Match player's hit animation speed
            
            if (health <= 0) {
                health = 0;
                death = true;
            }
        }
    }

    public Rectangle2D.Float getHitbox() {
        return hitbox;
    }

    public boolean isAttacking() {
        return light_attack || heavy_attack;
    }
    
    // Getters and setters
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
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
    
    // Abstract methods for character-specific implementations
    public abstract void update();
    public abstract void render(Graphics g);
    public abstract void loadAnimations();
    public abstract float getCharacterX();
    public abstract float getCharacterY();
}