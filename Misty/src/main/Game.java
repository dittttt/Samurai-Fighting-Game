
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
	private GameWindow gameWindow; // Store the GameWindow instance
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
	private boolean isMusicMuted = false;
	private boolean isSfxMuted = false;

	public Game() {
		initClasses();

		gamePanel = new GamePanel(this);
		gameWindow = new GameWindow(gamePanel); // Initialize the GameWindow instance
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
				Gamestate.state = Gamestate.GAME_OVER;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Gamestate.state = Gamestate.MENU;
			}
			if (playing.getEnemy().getHealth() <= 0) {
				Gamestate.state = Gamestate.VICTORY;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Gamestate.state = Gamestate.MENU;
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
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
			g.setColor(Color.WHITE);
			Font font = new Font("Arial", Font.PLAIN, 30);
			g.setFont(font);
			String gameOverText = "GAME OVER";
			int textWidth = g.getFontMetrics().stringWidth(gameOverText);
			int x = (GAME_WIDTH - textWidth) / 2;
			int y = GAME_HEIGHT / 2;
			g.drawString(gameOverText, x, y);
			break;
		case VICTORY:
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
			g.setColor(Color.BLACK);
			Font fontl = new Font("Arial", Font.PLAIN, 30);
			g.setFont(fontl);
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

	public void toggleMusicMute() {
		isMusicMuted = !isMusicMuted;
		if (isMusicMuted) {
			stopMenuMusic();
			stopInGameMusic();
		} else {
			if (Gamestate.state == Gamestate.MENU) {
				startMenuMusic();
			} else if (Gamestate.state == Gamestate.PLAYING) {
				startInGameMusic();
			}
		}
	}

	public void toggleSfxMute() {
		isSfxMuted = !isSfxMuted;
		// Add logic to mute/unmute sound effects if implemented
	}

	private void playMusic(String filepath) {
		try {
			File musicFile = new File(System.getProperty("user.dir") + File.separator + filepath);
			if (musicFile.exists()) {
				AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicFile);
				menuMusicClip = AudioSystem.getClip();
				menuMusicClip.open(audioInput);
				if (!isMusicMuted) {
					menuMusicClip.start();
					menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
				}
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
		if (menuMusicClip != null && !menuMusicClip.isRunning() && !isMusicMuted) {
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
				if (!isMusicMuted) {
					inGameMusicClip.start();
					inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
				}
			} else {
				System.out.println("Can't find file!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopInGameMusic() {
		if (inGameMusicClip != null && inGameMusicClip.isRunning()) {
			inGameMusicClip.stop();
		}
	}

	private void startInGameMusic() {
		if (inGameMusicClip != null && !inGameMusicClip.isRunning() && !isMusicMuted) {
			inGameMusicClip.start();
			inGameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
		}
	}

	public GameWindow getGameWindow() {
		return gameWindow;
	}
}
