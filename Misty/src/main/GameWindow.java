
package main;

import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;

public class GameWindow {
    private JFrame jframe;
    private GraphicsDevice gd;
    private boolean isFullscreen = false;
    private boolean isMaximized = false; // Track maximize state
    private GamePanel gamePanel;

    public GameWindow(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        jframe = new JFrame();
        gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.add(gamePanel);
        jframe.setResizable(true); // Allow resizing for maximize feature

        jframe.setSize(Game.GAME_WIDTH, Game.GAME_HEIGHT);
        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);

        jframe.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (isFullscreen) {
                    gd.setFullScreenWindow(null);
                }
                System.exit(0);
            }
        });

        jframe.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                gamePanel.getGame().windowFocusLost();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
            }
        });

        jframe.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F11) {
                    toggleFullscreen();
                } else if (e.getKeyCode() == KeyEvent.VK_M) {
                    toggleMaximize();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    public void toggleFullscreen() {
        if (isFullscreen) {
            gd.setFullScreenWindow(null);
            jframe.dispose();
            jframe.setUndecorated(false);
            jframe.setSize(Game.GAME_WIDTH, Game.GAME_HEIGHT);
            jframe.setLocationRelativeTo(null);
            jframe.setVisible(true);
            gamePanel.setPreferredSize(new Dimension(Game.GAME_WIDTH, Game.GAME_HEIGHT));
            gamePanel.revalidate();
            gamePanel.repaint();
        } else {
            jframe.dispose();
            jframe.setUndecorated(true);
            gamePanel.setPreferredSize(new Dimension(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight()));
            gamePanel.revalidate();
            gamePanel.repaint();
            jframe.setVisible(true);
            gd.setFullScreenWindow(jframe);
        }
        gamePanel.requestFocus();
        isFullscreen = !isFullscreen;
    }

    public void toggleMaximize() {
        if (isMaximized) {
            jframe.setExtendedState(JFrame.NORMAL); // Restore to normal state
        } else {
            jframe.setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize the window
        }
        isMaximized = !isMaximized;
    }
}
