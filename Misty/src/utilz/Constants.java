package utilz;

import main.Game;

public class Constants {
	
	public static class UI{
		public static class Buttons{
			public static final int B_WIDTH_DEFAULT = 140;
			public static final int B_HEIGHT_DEFAULT = 56;
			public static final int B_WIDTH = (int) (B_WIDTH_DEFAULT * Game.MENU_SCALE);
			public static final int B_HEIGHT = (int) (B_HEIGHT_DEFAULT * Game.MENU_SCALE);
		}
		
		public static class PauseButtons{
			public static final int SOUND_SIZE_DEFAULT = 42;
			public static final int SOUND_SIZE = (int)(SOUND_SIZE_DEFAULT * Game.MENU_SCALE);
		}
		
		public static class URMButtons{
			public static final int URM_DEFAULT_SIZE = 56;
			public static final int URM_SIZE = (int)(URM_DEFAULT_SIZE * Game.MENU_SCALE);
		}
		public static class VolumeButtons{
			public static final int VOLUME_DEFAULT_WIDTH = 28;
			public static final int VOLUME_DEFAULT_HEIGHT = 44;
			public static final int SLIDER_DEFAULT_WIDTH = 215;
			
			
			public static final int VOLUME_WIDTH = (int)(VOLUME_DEFAULT_WIDTH * Game.MENU_SCALE);
			public static final int VOLUME_HEIGHT = (int)(VOLUME_DEFAULT_HEIGHT * Game.MENU_SCALE);
			public static final int SLIDER_WIDTH = (int)(SLIDER_DEFAULT_WIDTH * Game.MENU_SCALE);
		}
	}
	
	public static class Directions{
		public static final int LEFT = 0;
		public static final int UP = 1;
		public static final int RIGHT = 2;
		public static final int DOWN = 3;
	}
	
	public static class PlayerConstants {
		public static final int IDLE = 0;
		public static final int RUNNING = 1;
		public static final int WALKING = 2;
		public static final int JUMP = 3;
		public static final int FALLING = 3;
		public static final int ROLL = 4;
		public static final int HIT = 5;
		public static final int DEATH = 6;
		public static final int LIGHT_ATTACK = 7;
		public static final int HEAVY_ATTACK = 8;
		
		public static int GetSpriteAmount(int player_action) {
			
			switch(player_action) {
			case DEATH:
			case HEAVY_ATTACK:
				return 11;
			case RUNNING:
				return 10;
			case LIGHT_ATTACK:
				return 7;
			case ROLL:
			case HIT:
				return 6;
			case WALKING:
			case JUMP:
				return 4;
			case IDLE:
			default:
				return 3;
			}
			
		}
	    public static int GetHitFrame(int player_action) {
	        switch(player_action) {
	            case LIGHT_ATTACK:
	                return 4; // Hits on frame 4 of 7
	            case HEAVY_ATTACK:
	                return 5; // Hits on frame 5 of 11
	            default:
	                return 0;
	        }
	    }
	}
}
