package com.thumbling.screen;

import com.thumbling.helpers.FontRenderer;
import com.thumbling.main.Main;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class MenuScreen {
    private final long window;
    private final Main main;

    public MenuScreen(long window, Main main) {
        this.window = window;
        this.main = main;
    }

    public void update() {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_1) == GLFW.GLFW_PRESS) {
            System.out.println("Play selected!");
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_2) == GLFW.GLFW_PRESS) {
            System.out.println("Settings selected!");
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_3) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }
    }

    public void render() {
        int buttonWidth = 200;
        int buttonHeight = 50;
        int centerX = 0;
        int startY = 50;

        int[] playText = {69, 65, 72, 60};
        drawButton(centerX, startY, buttonWidth, buttonHeight, playText, 0.4f);

        int[] settingsText = {54, 76, 55, 55, 80, 67, 54};
        drawButton(centerX, startY - 80, buttonWidth, buttonHeight, settingsText, 0.4f);

        int[] exitText = {76, 59, 80, 55};
        drawButton(centerX, startY - 160, buttonWidth, buttonHeight, exitText, 0.4f);
    }

    private void drawButton(int x, int y, int width, int height, int[] text, float scale) {
        GL11.glColor3f(0.2f, 0.6f, 1f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x - width / 2f, y - height / 2f);
        GL11.glVertex2f(x + width / 2f, y - height / 2f);
        GL11.glVertex2f(x + width / 2f, y + height / 2f);
        GL11.glVertex2f(x - width / 2f, y + height / 2f);
        GL11.glEnd();

        main.getFontConsolas().setCanvasProperties(x - width / 2f, y - height / 2f, width, height);
        main.getFontConsolas().drawText(text, scale, "center", "center");
    }
}
