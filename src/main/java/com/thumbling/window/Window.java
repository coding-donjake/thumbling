package com.thumbling.window;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

public class Window {
    private long window;
    private int width;
    private int height;
    private String title;
    private boolean fullscreen;

    public Window(int width, int height, String title, boolean fullscreen) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.fullscreen = fullscreen;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);

        long monitor = MemoryUtil.NULL;

        if (fullscreen) {
            monitor = GLFW.glfwGetPrimaryMonitor();
            var videoMode = GLFW.glfwGetVideoMode(monitor);
            if (videoMode != null) {
                width = videoMode.width();
                height = videoMode.height();
            }
        }

        window = GLFW.glfwCreateWindow(width, height, title, fullscreen ? monitor : MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        if (!fullscreen) {
            var videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            if (videoMode != null) {
                GLFW.glfwSetWindowPos(
                        window,
                        (videoMode.width() - width) / 2,
                        (videoMode.height() - height) / 2
                );
            }
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        GL.createCapabilities();
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }

    public void update() {
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public long getHandle() {
        return window;
    }
}
