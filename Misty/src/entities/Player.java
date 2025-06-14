package entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import main.Game;
import utilz.LoadSave;
import static utilz.Constants.PlayerConstants.*;

public class Player extends Character {
    // Player-specific constants
    private static final int HITBOX_WIDTH = (int) (20 * Game.SCALE);
    private static final int HITBOX_HEIGHT = (int) (30 * Game.SCALE);
    private static final int X_DRAW_OFFSET = 44;
    private static final int Y_DRAW_OFFSET = 36;
    private boolean attackProcessed = false;

    public Player(float x, float y, int width, int height) {
        super(x, y, width, height);
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

    @Override
    public void render(Graphics g) {
        renderCharacter(g, animations);
        renderHealthBar(g);
    }

    @Override
    public void loadAnimations() {
        BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);
        animations = new BufferedImage[9][12];
        for (int j = 0; j < animations.length; j++)
            for (int i = 0; i < animations[j].length; i++)
                animations[j][i] = img.getSubimage(i * 112, j * 72, 112, 72);
    }

    public void checkAttackHit(Enemy enemy) {
        if (isAttacking() && attackHitbox.intersects(enemy.getHitbox())) {
            // Only register hit at the end of the animation
            if (aniIndex >= GetSpriteAmount(characterAction) - 2 && !attackProcessed) {
                enemy.takeDamage(light_attack ? 10 : 20);
                attackProcessed = true;
            }
        }
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
        this.attackProcessed = false;
    }

    public void setHeavyAttack(boolean heavy_attack) {
        this.heavy_attack = heavy_attack;
        this.attackProcessed = false;
    }

    public void toggleLightAttack(boolean light_attack) {
        this.light_attack = true;
        this.attackProcessed = false;
    }

    public void toggleHeavyAttack(boolean heavy_attack) {
        this.heavy_attack = true;
        this.attackProcessed = false;
    }

    public void toggleDeath(boolean death) {
        this.death = true;
    }

    public void toggleHit(boolean hit) {
        this.hit = true;
        // Reset animation to ensure the hit animation plays
        resetAniTick(); 
    }
    
    public boolean isAttacking() {
        return light_attack || heavy_attack;
    }
}