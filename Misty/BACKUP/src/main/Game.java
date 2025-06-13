package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import gamestates.Gamestate;
import gamestates.Menu;
import gamestates.Playing;

public class Game implements Runnable {

	private GamePanel gamePanel;
	private Thread gameThread;
	private final int FPS_SET = 120;
	private final int UPS_SET = 200;

	private Playing playing;
	private Menu menu;
	
	public final static int TILES_DEFAULT_SIZE = 32;
	public final static float SCALE = 5.0f;
	public final static float MENU_SCALE = 2f;
	public final static int TILES_IN_WIDTH = 12;
	public final static int TILES_IN_HEIGHT = 7;
	public final static int TILES_SIZE = (int) (TILES_DEFAULT_SIZE * SCALE);
	public final static int GAME_WIDTH = TILES_SIZE * TILES_IN_WIDTH;
	public final static int GAME_HEIGHT = TILES_SIZE * TILES_IN_HEIGHT;
	
	private Clip menuMusicClip;
	private Clip inGameMusicClip;
	
	public Game() {
		initClasses();
		
		gamePanel = new GamePanel(this);
		new GameWindow(gamePanel);
		gamePanel.requestFocus();

		playMusic("audio/menu.wav");
		playInGameMusic("audio/ingame.wav");

		startGameLoop();
	}

	private void initClasses() {
		menu = new Menu(this);
		playing = new Playing(this);
	}

	private void startGameLoop() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	public void update() {
	    switch (Gamestate.state) {
	        case MENU:
	            menu.update();
	             startMenuMusic();
	             stopInGameMusic();
	            break;
	        case PLAYING:
	            playing.update();
	            if (playing.getPlayer().getHealth() <= 0) {
	                // Player's health is zero or less, pause the game and display "GAME OVER"
	                Gamestate.state = Gamestate.GAME_OVER;
	                try {
	                    Thread.sleep(2000); // Pause for three seconds
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	                Gamestate.state = Gamestate.MENU; // Return to the menu
	            } 
	            if(playing.getEnemy().getHealth() <= 0){
	            	// Player's health is zero or less, pause the game and display "GAME OVER"
	                Gamestate.state = Gamestate.VICTORY;
	                try {
	                    Thread.sleep(2000); // Pause for three seconds
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }
	             stopMenuMusic();
	             startInGameMusic();
	            break;
	        case GAME_OVER:
	        case VICTORY:
	            break;
	        case OPTIONS:
	        case QUIT:
	        default:
	            System.exit(0);
	            break;
	    }
	}


	public void render(Graphics g) {
	    switch (Gamestate.state) {
	        case MENU:
	            menu.draw(g);
	            break;
	        case PLAYING:
	            playing.draw(g);
	            break;
	        case GAME_OVER:
	            // Set the background to black
	            g.setColor(Color.BLACK);
	            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

	            // Set the text color to white
	            g.setColor(Color.WHITE);

	            // Use a Font object for the text
	            Font font = new Font("Arial", Font.PLAIN, 30);
	            g.setFont(font);

	            // Draw the "GAME OVER" text at the center of the screen
	            String gameOverText = "GAME OVER";
	            int textWidth = g.getFontMetrics().stringWidth(gameOverText);
	            int x = (GAME_WIDTH - textWidth) / 2;
	            int y = GAME_HEIGHT / 2;
	            g.drawString(gameOverText, x, y);

	            break;
	        case VICTORY:
	        	// Set the background to black
	            g.setColor(Color.WHITE);
	            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

	            // Set the text color to white
	            g.setColor(Color.BLACK);

	            // Use a Font object for the text
	            Font fontl = new Font("Arial", Font.PLAIN, 30);
	            g.setFont(fontl);

	            // Draw the "GAME OVER" text at the center of the screen
	            String gameOverTextl = "YOU WIN!!";
	            int textWidthl = g.getFontMetrics().stringWidth(gameOverTextl);
	            int xl = (GAME_WIDTH - textWidthl) / 2;
	            int y1 = GAME_HEIGHT / 2;
	            g.drawString(gameOverTextl, xl, y1);
	            break;
	        default:
	            break;
	    }
	}


	@Override
	public void run() {

		double timePerFrame = 1000000000.0 / FPS_SET;
		double timePerUpdate = 1000000000.0 / UPS_SET;

		long previousTime = System.nanoTime();

		int frames = 0;
		int updates = 0;
		long lastCheck = System.currentTimeMillis();

		double deltaU = 0;
		double deltaF = 0;

		while (true) {
			long currentTime = System.nanoTime();

			deltaU += (currentTime - previousTime) / timePerUpdate;
			deltaF += (currentTime - previousTime) / timePerFrame;
			previousTime = currentTime;

			if (deltaU >= 1) {
				update();
				updates++;
				deltaU--;
			}

			if (deltaF >= 1) {
				gamePanel.repaint();
				frames++;
				deltaF--;
			}

			if (System.currentTimeMillis() - lastCheck >= 1000) {
				lastCheck = System.currentTimeMillis();
//				System.out.println("FPS: " + frames + " | UPS: " + updates);
				frames = 0;
				updates = 0;
			}

		}

	}

	public void windowFocusLost() {
		if (Gamestate.state == Gamestate.PLAYING)
			playing.getPlayer().resetDirBooleans();
	}

	public Menu getMenu() {
		return menu;
	}

	public Playing getPlaying() {
		return playing;
	}
	
	private void playMusic(String filepath) {
	    try {
	        File musicFile = new File(System.getProperty("user.dir") + File.separator + filepath);
	        if (musicFile.exists()) {
	            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
	            menuMusicClip = AudioSystem.getClip();
	            menuMusicClip.open(audioInput);
	            menuMusicClip.start();
	            menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
	        } else {
	            System.out.println("Can't find file!");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void stopMenuMusic() {
	    if (menuMusicClip != null && menuMusicClip.isRunning()) {
	        menuMusicClip.stop();
	    }
	}
	
	private void startMenuMusic() {
	    if (menuMusicClip != null && !menuMusicClip.isRunning()) {
	        menuMusicClip.start();
	        menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
	    }
	}

	private void playInGameMusic(String filepath) {
	    try {
	        File musicFile = new File(System.getProperty("user.dir") + File.separator + filepath);
	        if (musicFile.exists()) {
	            AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
	            inGameMusicClip = AudioSystem.getClip();
	            inGameMusicClip.open(audioInput);
	            inGameMusicClip.start();
	            inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
	        } else {
	            System.out.println("Can't find file!");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void startInGameMusic() {
		if (inGameMusicClip != null && !inGameMusicClip.isRunning()) {
			inGameMusicClip.start();
			inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
	    }
	}
	
	private void stopInGameMusic() {
	    if (inGameMusicClip != null && inGameMusicClip.isRunning()) {
	    	inGameMusicClip.stop();
	    }
	}
}
