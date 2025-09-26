package com.thumbling.screen;

import com.thumbling.helpers.FontRenderer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class MenuScreen {
    private final long window;
    private final FontRenderer font;

    public MenuScreen(long window, FontRenderer font) {
        this.window = window;
        this.font = font;
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

        drawButton(centerX, startY, buttonWidth, buttonHeight);
        drawButton(centerX, startY - 80, buttonWidth, buttonHeight);
        drawButton(centerX, startY - 160, buttonWidth, buttonHeight);

        font.drawText("Play", centerX - 25, startY - 10, 28);
        font.drawText("Settings", centerX - 45, (startY - 80) - 10, 24);
        font.drawText("Exit", centerX - 20, (startY - 160) - 10, 28);
    }

    private void drawButton(int x, int y, int width, int height) {
        GL11.glColor3f(0.2f, 0.6f, 1f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x - width / 2f, y - height / 2f);
        GL11.glVertex2f(x + width / 2f, y - height / 2f);
        GL11.glVertex2f(x + width / 2f, y + height / 2f);
        GL11.glVertex2f(x - width / 2f, y + height / 2f);
        GL11.glEnd();
    }
}
