package com.thumbling.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class FontRenderer {
    private final int textureID;
    private final int[][] charCoords;
    private final int charsPerRow;
    private final int charsPerColumn;
    private final int totalChars;
    private final int cellWidth;
    private final int cellHeight;

    private float paperX, paperY, paperWidth, paperHeight;

    public FontRenderer(String imagePath, int totalChars, int charsPerRow, int charWidth, int charHeight) {
        this.totalChars = totalChars;
        this.charsPerRow = charsPerRow;

        ByteBuffer image;
        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];

        try (InputStream is = FontRenderer.class.getClassLoader().getResourceAsStream(imagePath)) {
            if (is == null) {
                throw new RuntimeException("Font image not found in resources: " + imagePath);
            }

            Path tempFile = Files.createTempFile("font", ".png");
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);

            stbi_set_flip_vertically_on_load(true);
            image = stbi_load(tempFile.toString(), width, height, channels, 4);

            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font resource", e);
        }

        if (image == null) {
            throw new RuntimeException("Failed to decode font image: " + stbi_failure_reason());
        }

        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0,
                GL_RGBA, GL_UNSIGNED_BYTE, image);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        stbi_image_free(image);

        charsPerColumn = (int) Math.ceil(totalChars / (float) charsPerRow);
        cellWidth = width[0] / charsPerRow;
        cellHeight = height[0] / charsPerColumn;

        charCoords = new int[totalChars][4];
        cropChars(charWidth, charHeight);
    }

    private void cropChars(int charWidth, int charHeight) {
        for (int i = 0; i < totalChars; i++) {
            int row = i / charsPerRow;
            int col = i % charsPerRow;

            // Crop box within the cell
            charCoords[i][0] = col * cellWidth + 30; // X offset in the cell
            charCoords[i][1] = row * cellHeight + 7; // Y offset in the cell
            charCoords[i][2] = charWidth;            // cropped width
            charCoords[i][3] = charHeight;           // cropped height
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

    public void setCanvasProperties(float paperX, float paperY, float paperWidth, float paperHeight) {
        this.paperX = paperX;
        this.paperY = paperY;
        this.paperWidth = paperWidth;
        this.paperHeight = paperHeight;
    }

    public void drawText(int[] charIndexes, float scale, String hAlign, String vAlign) {
        glBindTexture(GL_TEXTURE_2D, textureID);
        glEnable(GL_TEXTURE_2D);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // ---- Step 1: Measure text block ----
        java.util.List<Float> lineWidths = new java.util.ArrayList<>();
        float currentLineWidth = 0;
        int lineCount = 1;

        for (int idx : charIndexes) {
            if (idx == -1) { // explicit newline
                lineWidths.add(currentLineWidth);
                currentLineWidth = 0;
                lineCount++;
                continue;
            }
            if (idx < 0 || idx >= totalChars) continue;

            int[] coords = getCharCoords(idx);
            float w = coords[2] * scale;

            // Check auto-wrap
            if (currentLineWidth + w > paperWidth) {
                lineWidths.add(currentLineWidth);
                currentLineWidth = 0;
                lineCount++;
            }

            currentLineWidth += w + (1 * scale);
        }
        lineWidths.add(currentLineWidth);

        float totalTextHeight = lineCount * (cellHeight * scale);

        // ---- Step 2: Vertical alignment ----
        float startY = paperY;
        if ("center".equalsIgnoreCase(vAlign)) {
            // Center block, then shift up so last line doesn't push text downward
            startY = (paperY + (paperHeight - totalTextHeight) / 2f + (lineCount - 1) * (cellHeight * scale)) + (10 * scale);
        } else if ("bottom".equalsIgnoreCase(vAlign)) {
            startY = paperY + (paperHeight - totalTextHeight) + (lineCount - 1) * (cellHeight * scale);
        }

        // ---- Step 3: Render text ----
        float cursorY = startY;
        int lineIndex = 0;
        float cursorX = 0;

        for (int i = 0; i < charIndexes.length; i++) {
            int idx = charIndexes[i];

            if (idx == -1) { // explicit newline
                lineIndex++;
                cursorY -= cellHeight * scale;
                cursorX = 0;
                continue;
            }
            if (idx < 0 || idx >= totalChars) continue;

            int[] coords = getCharCoords(idx);
            int srcX = coords[0];
            int srcY = coords[1];
            int w = coords[2];
            int h = coords[3];

            float charWidth = w * scale;
            float charHeight = h * scale;

            // Auto-wrap
            if (cursorX + charWidth > paperWidth) {
                lineIndex++;
                cursorY -= cellHeight * scale;
                cursorX = 0;
            }

            // ---- Horizontal alignment (per line) ----
            float lineWidth = lineWidths.get(lineIndex);
            float lineStartX = paperX;
            if ("center".equalsIgnoreCase(hAlign)) {
                lineStartX = paperX + (paperWidth - lineWidth) / 2f;
            } else if ("right".equalsIgnoreCase(hAlign)) {
                lineStartX = paperX + paperWidth - lineWidth;
            }

            float drawX = lineStartX + cursorX;
            float drawY = cursorY;

            // UV conversion
            float u0 = srcX / (float)(charsPerRow * cellWidth);
            float v0 = srcY / (float)(charsPerColumn * cellHeight);
            float u1 = (srcX + w) / (float)(charsPerRow * cellWidth);
            float v1 = (srcY + h) / (float)(charsPerColumn * cellHeight);

            // Draw character quad
            glBegin(GL_QUADS);
            glTexCoord2f(u0, v0); glVertex2f(drawX, drawY);
            glTexCoord2f(u1, v0); glVertex2f(drawX + charWidth, drawY);
            glTexCoord2f(u1, v1); glVertex2f(drawX + charWidth, drawY + charHeight);
            glTexCoord2f(u0, v1); glVertex2f(drawX, drawY + charHeight);
            glEnd();

//            // ---- Debug box (blue outline per character) ----
//            glDisable(GL_TEXTURE_2D);
//            glColor3f(0f, 0f, 1f);
//            glBegin(GL_LINE_LOOP);
//            glVertex2f(drawX, drawY);
//            glVertex2f(drawX + charWidth, drawY);
//            glVertex2f(drawX + charWidth, drawY + charHeight);
//            glVertex2f(drawX, drawY + charHeight);
//            glEnd();
//            glEnable(GL_TEXTURE_2D);
//            glColor3f(1f, 1f, 1f); // reset color

            cursorX += charWidth + (1 * scale);
        }

        glDisable(GL_TEXTURE_2D);
    }
}
