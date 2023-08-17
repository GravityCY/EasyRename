package me.gravityio.easyrename.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * A Scuffed Text Field that's supposed to allow for
 * <ul>
 *     <li>Centering text</li>
 *     <li>Having some callbacks when enter is pressed</li>
 * </ul>
 */
public class ScuffedTextField extends TextFieldWidget {
    Runnable onEscape;
    Runnable onEnter;

    final MinecraftClient client;
    final TextRenderer textRenderer;
    boolean isCentered;
    int sx;

    public ScuffedTextField(MinecraftClient client, TextRenderer textRenderer, int x, int y, Text text, boolean isCentered) {
        super(textRenderer, x, y, 10, textRenderer.fontHeight, text);
        this.setDrawsBackground(false);

        this.client = client;
        this.textRenderer = textRenderer;

        this.isCentered = isCentered;
        this.sx = x;

        this.setText(text.getString());
        if (!this.isCentered) {
            super.setX(x);
        }
        super.setY(y);
    }

    public void onEscape(Runnable onEscape) {
        this.onEscape = onEscape;
    }

    public void onEnter(Runnable onEnter) {
        this.onEnter = onEnter;
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        this.setSelectionEnd(0);
        this.doResize();
        if (this.isCentered) this.doCenter();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers))
            return true;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.setFocused(false);
            this.doEscape();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            this.setFocused(false);
            this.doEnter();
            return true;
        }

        for (net.minecraft.client.option.KeyBinding key : this.client.options.hotbarKeys) {
            if (!key.matchesKey(keyCode, scanCode)) continue;
            return true;
        }

        return this.client.options.inventoryKey.matchesKey(keyCode, scanCode)
                || this.client.options.chatKey.matchesKey(keyCode, scanCode);
    }

    @Override
    public void write(String text) {
        var p = super.getText();
        super.write(text);
        var n = super.getText();
        if (!p.equals(n)) {
            this.doResize();
            if (isCentered) this.doCenter();
        }
    }

    @Override
    public void eraseCharacters(int characterOffset) {
        super.eraseCharacters(characterOffset);
        this.doResize();
        if (this.isCentered) this.doCenter();
    }

    private void doResize() {
        super.width = this.textRenderer.getWidth(super.getText()) + 15;
    }

    private void doCenter() {
        super.setX(this.sx - super.width / 2);
    }

    private void doEscape() {
        if (this.onEscape == null) return;
        this.onEscape.run();
    }

    private void doEnter() {
        if (this.onEnter == null) return;
        this.onEnter.run();
    }

    @Override
    public void setX(int x) {
        if (this.isCentered) {
            this.sx = x;
            this.doResize();
            if (this.isCentered) this.doCenter();
        } else {
            super.setX(x);
        }
    }
}
