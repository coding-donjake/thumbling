package com.thumbling.helpers;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class FontRenderer {

    private final int textureID;
    private final int[][] charCoords; // store x,y,width,height for each character
    private final int charsPerRow;
    private final int charsPerColumn;
    private final int totalChars;
    private final int charWidth;
    private final int charHeight;

    /**
     * @param imagePath PNG with letters
     * @param totalChars Total characters in the PNG (e.g., 96 for ASCII 32-127)
     * @param charsPerRow How many characters horizontally
     */
    public FontRenderer(String imagePath, int totalChars, int charsPerRow) {
        this.totalChars = totalChars;
        this.charsPerRow = charsPerRow;

        // Load image
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        ByteBuffer image;
        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];

        stbi_set_flip_vertically_on_load(true);
        image = stbi_load("fonts/consolas.png", width, height, channels, 4);
        if (image == null) {
            throw new RuntimeException("Failed to load font image: " + stbi_failure_reason());
        }

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0,
                GL_RGBA, GL_UNSIGNED_BYTE, image);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        stbi_image_free(image);

        // Compute character cropping
        charsPerColumn = (int) Math.ceil(totalChars / (float) charsPerRow);
        charWidth = width[0] / charsPerRow;
        charHeight = height[0] / charsPerColumn;

        charCoords = new int[totalChars][4]; // x, y, w, h for each character
        cropChars();
    }

    private void cropChars() {
        for (int i = 0; i < totalChars; i++) {
            int row = i / charsPerRow;
            int col = i % charsPerRow;

            charCoords[i][0] = col * charWidth;
            charCoords[i][1] = row * charHeight;
            charCoords[i][2] = charWidth;
            charCoords[i][3] = charHeight;
        }
    }

    public int getTextureID() {
        return textureID;
    }

    public int[] getCharCoords(int index) {
        if (index < 0 || index >= totalChars)
            throw new IllegalArgumentException("Invalid character index");
        return charCoords[index];
    }
}
