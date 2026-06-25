
package main;

import java.awt.Graphics;
import java.io.File;
import java.net.URL;

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

		loadMenuMusic("audio/menu.wav");
		loadInGameMusic("audio/ingame.wav");
		startMenuMusic();

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
			// Remove the automatic state change conditions here
			stopMenuMusic();
			startInGameMusic();
			break;
		case GAME_OVER:
			playing.update();
			stopInGameMusic();
			break;
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
			playing.draw(g);
			break;
		case VICTORY:
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

	private void loadMenuMusic(String filepath) {
		menuMusicClip = loadClip(filepath);
	}

	private void loadInGameMusic(String filepath) {
		inGameMusicClip = loadClip(filepath);
	}

	private Clip loadClip(String filepath) {
		try {
			AudioInputStream audioInput = openAudioStream(filepath);
			Clip clip = AudioSystem.getClip();
			clip.open(audioInput);
			return clip;
		} catch (Exception e) {
			System.err.println("Could not load audio file: " + filepath);
			return null;
		}
	}

	private AudioInputStream openAudioStream(String filepath) throws Exception {
		URL resource = getClass().getClassLoader().getResource(filepath);
		if (resource == null) {
			resource = getClass().getClassLoader().getResource(new File(filepath).getName());
		}
		if (resource != null) {
			return AudioSystem.getAudioInputStream(resource);
		}

		File musicFile = new File(System.getProperty("user.dir"), filepath);
		if (!musicFile.exists()) {
			musicFile = new File(System.getProperty("user.dir"), "Misty" + File.separator + filepath);
		}
		return AudioSystem.getAudioInputStream(musicFile);
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

	public GamePanel getGamePanel() {
		return gamePanel;
	}
}
