package utilz;

import java.awt.geom.Rectangle2D;

import main.Game;

public class HelpMethods {

	public static boolean CanMoveHere(float x, float y, float width, float height, int[][] lvlData) {
	    int numPointsX = (int) Math.ceil(width / Game.TILES_SIZE);
	    int numPointsY = (int) Math.ceil(height / Game.TILES_SIZE);

	    for (int i = 0; i < numPointsX; i++) {
	        for (int j = 0; j < numPointsY; j++) {
	            float pointX = x + i * Game.TILES_SIZE;
	            float pointY = y + j * Game.TILES_SIZE;

	            if (IsSolid(pointX, pointY, lvlData)) {
	                return false;
	            }
	        }
	    }

	    return true;
	}


	private static boolean IsSolid(float x, float y, int[][] lvlData) {
		if (x < 0 || x >= Game.GAME_WIDTH)
		    return true;
		if (y < 0 || y >= Game.GAME_HEIGHT)
		    return true;

		float xIndex = x / Game.TILES_SIZE;
		float yIndex = y / Game.TILES_SIZE;

		int value = lvlData[(int) yIndex][(int) xIndex];

		if (value >= 72 && value <= 84)
		    return true;
		return false;
	}

	public static float GetEntityXPosNextToWall(Rectangle2D.Float hitbox, float xSpeed) {
		int currentTile = (int) (hitbox.x / Game.TILES_SIZE);
		if (xSpeed > 0) {
			// right
			int tileXPos = currentTile * Game.TILES_SIZE;
			int xOffset = (int) (Game.TILES_SIZE - hitbox.width);
			return tileXPos + xOffset - 1;
		} else {
			// left
			return currentTile * Game.TILES_SIZE;
		}
	}

	public static float GetEntityYPosUnderRoofOrAboveFloor(Rectangle2D.Float hitbox, float airSpeed) {
		int currentTile = (int) (hitbox.y / Game.TILES_SIZE);
		if (airSpeed > 0) {
			// Falling - touching floor
			int tileYPos = currentTile * Game.TILES_SIZE;
			int yOffset = (int) (Game.TILES_SIZE - hitbox.height);
			return tileYPos + yOffset - 1;
		} else {
			// Jumping
			return currentTile * Game.TILES_SIZE;

		}
	}

	public static boolean IsEntityOnFloor(Rectangle2D.Float hitbox, int[][] lvlData) {
		// Check the pixel below bottomleft and bottomright
		if (!IsSolid(hitbox.x, hitbox.y + hitbox.height + 1, lvlData))
			if (!IsSolid(hitbox.x + hitbox.width, hitbox.y + hitbox.height + 1, lvlData))
				return false;

		return true;
	}
}
