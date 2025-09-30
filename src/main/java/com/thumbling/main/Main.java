package com.thumbling.main;

import com.thumbling.helpers.FontRenderer;
import com.thumbling.screen.MenuScreen;
import com.thumbling.window.Window;

import org.lwjgl.opengl.GL11;

public class Main implements Runnable {
    private FontRenderer fontConsolas;
    private final Window window;

    private MenuScreen menu;

    public Main() {
        window = new Window(1024, 768, "2D Physics", false);
    }

    public static void main(String[] args) {
        new Main().run();
    }

    @Override
    public void run() {
        window.init();
        fontConsolas = new FontRenderer("fonts/consolas.png", 81, 9, 51, 94);
        menu = new MenuScreen(window.getHandle(), this);

        int targetUPS = 60;
        int targetFPS = 60;

        double updateInterval = 1_000_000_000.0 / targetUPS;
        double frameInterval = 1_000_000_000.0 / targetFPS;

        long lastUpdateTime = System.nanoTime();
        long lastFrameTime = System.nanoTime();
        long timer = System.currentTimeMillis();

        int updates = 0;
        int frames = 0;

        while (!window.shouldClose()) {
            long now = System.nanoTime();

            if (now - lastUpdateTime >= updateInterval) {
                update(); // physics, input, game logic
                lastUpdateTime += updateInterval;
                updates++;
            }

            if (now - lastFrameTime >= frameInterval) {
                render(); // drawing OpenGL
                window.update(); // swap buffers + poll events
                lastFrameTime += frameInterval;
                frames++;
            }

            if (System.currentTimeMillis() - timer >= 1000) {
                System.out.println("UPS: " + updates + " | FPS: " + frames);
                updates = 0;
                frames = 0;
                timer += 1000;
            }
        }

        window.destroy();
    }

    private void update() {
        menu.update();
    }

    private void render() {
        GL11.glClearColor(0f, 0f, 0f, 0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-512, 512, -384, 384, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        menu.render();
    }

    public FontRenderer getFontConsolas() {
        return fontConsolas;
    }
}
