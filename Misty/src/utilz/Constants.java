package utilz;

public class Constants {
	
	public static class Directions{
		public static final int LEFT = 0;
		public static final int UP = 1;
		public static final int RIGHT = 2;
		public static final int DOWN = 3;
	}
	
	public static class PlayerConstants {
//		public static final int IDLE = 0;
//		public static final int RUNNING = 1;
//		public static final int JUMP = 2;
//		public static final int FALLING = 3;
//		public static final int GROUND = 4;
//		public static final int HIT = 5;
//		public static final int ATTACK_1 = 6;
//		public static final int ATTACK_JUMP_1 = 7;
//		public static final int ATTACK_JUMP_2 = 8;
		
		public static final int IDLE = 0;
		public static final int RUNNING = 2;
		public static final int JUMP = 3;
		public static final int FALLING = 3;
		public static final int ROLL = 4;
		public static final int HIT = 5;
		public static final int DEATH = 6;
		public static final int LIGHT_ATTACK = 7;
		public static final int HEAVY_ATTACK = 8;
		
		public static int GetSpriteAmount(int player_action) {
			
			switch(player_action) {
//			case RUNNING:
//				return 6;
//			case IDLE:
//				return 5;
//			case HIT:
//				return 4;
//			case JUMP:
//			case ATTACK_1:
//			case ATTACK_JUMP_1:
//			case ATTACK_JUMP_2:
//				return 3;
//			case GROUND:
//				return 2;
//			case FALLING:
//			default:
//				return 1;
			
//			case RUNNING:
//				return 10;
			case DEATH:
			case HEAVY_ATTACK:
				return 11;
			case LIGHT_ATTACK:
				return 7;
			case ROLL:
			case HIT:
				return 6;
			case RUNNING:
			case JUMP:
				return 4;
			case IDLE:
			default:
				return 3;
			}
			
		}
	}
}
