package me.gravityio.easyrename.gui;

import me.gravityio.easyrename.StringHelper;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class TextField extends ClickableWidget {
    public Runnable onEnter;
    public String text;

    protected final TextRenderer textRenderer;
    protected int color = 0x404040;
    protected boolean shadow;
    protected int caretPosition;
    protected int caretPositionEnd;
    protected int caretColor = 0xffffffff;
    protected float align = 0;
    protected int caretPositionX = 0;
    protected int textWidth = 0;
    protected long lastRenderCaret = System.currentTimeMillis();
    protected boolean doRenderCaret;

    public TextField(TextRenderer textRenderer, int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
        this.setText(super.getMessage().getString());
    }

    public void setText(String text) {
        this.text = text;
        this.updateWidth();
        this.setCaretPosition(this.text.length());
    }

    public boolean isDisabled() {
        return !super.visible || !super.isFocused();
    }

    public int getDelete(boolean forward) {
        int ci = StringHelper.getWord(this.caretPosition, this.text, forward);
        return Math.abs(this.caretPosition - ci);
    }

    /**
     * Writes a string at the current caret position, and then moves the caret by the length of the given string.
     */
    public void write(String str) {
        if (this.textRenderer.getWidth(this.text + str) > super.width) return;

        if (this.caretPosition == this.text.length()) {
            this.text += str;
        } else if (this.caretPosition == 0) {
            this.text = str + this.text;
        } else {
            this.text = this.text.substring(0, this.caretPosition) + str + this.text.substring(this.caretPosition);
        }

        this.updateWidth();
        this.setCaretPosition(this.caretPosition + str.length());
    }

    public void remove(int amount, boolean flip) {
        amount = flip ? Math.min(this.text.length() - caretPosition, amount) : Math.min(caretPosition, amount);

        if (flip) {
            if (this.caretPosition == 0) {
                this.text = this.text.substring(amount);
            } else {
                this.text = this.text.substring(0, this.caretPosition) + this.text.substring(this.caretPosition + amount);
            }
        } else {
            if (this.caretPosition == this.text.length()) {
                this.text = this.text.substring(0, this.caretPosition - amount);
            } else if (this.caretPosition > 0 && this.caretPosition < this.text.length()) {
                this.text = this.text.substring(0, caretPosition - amount) + this.text.substring(caretPosition);
            }
            this.setCaretPosition(this.caretPosition - amount);
        }
        this.updateWidth();
    }

    private void updateWidth() {
        this.textWidth = this.textRenderer.getWidth(this.text);
    }

    private void updateCaretPosition() {
        this.caretPositionX = this.textRenderer.getWidth(this.text.substring(0, this.caretPosition));
    }

    private void setCaretPosition(int i) {
        this.caretPosition = i;
        this.updateCaretPosition();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isDisabled()) return false;

        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (this.text.isEmpty()) return true;
            int amount = ctrl ? getDelete(false) : 1;
            amount = Math.max(amount, 1);

            this.remove(amount, false);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (this.text.isEmpty()) return true;
            int amount = ctrl ? getDelete(true) : 1;
            amount = Math.max(amount, 1);

            this.remove(amount, true);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            super.setFocused(false);
            this.onEnter.run();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            super.setFocused(false);
            this.setText(super.getMessage().getString());
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_E) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (this.caretPosition == this.text.length()) return false;

            int p;
            if (Screen.hasControlDown()) {
                p = StringHelper.getStartWord(this.caretPosition, this.text, true);
            } else {
                p = this.caretPosition + 1;
            }

            this.setCaretPosition(p);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (this.caretPosition == 0) return false;
            int p;
            if (Screen.hasControlDown()) {
                p = StringHelper.getStartWord(this.caretPosition, this.text, false);
            } else {
                p = this.caretPosition - 1;
            }

            this.setCaretPosition(p);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            this.setCaretPosition(0);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            this.setCaretPosition(this.text.length());
            return true;
        } else if (Screen.isPaste(keyCode)) {
            this.write(MinecraftClient.getInstance().keyboard.getClipboard());
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            return true;
        } else if (Screen.isCopy(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.text);
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (this.isDisabled()) return false;

        if (SharedConstants.isValidChar(c)) {
            this.write(String.valueOf(c));
            return true;
        }

        return false;
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!super.visible) return;

        MatrixStack s = context.getMatrices();
        s.push();
        s.translate(super.getX() + (super.width - this.textWidth) * align, super.getY(), 0);

        context.drawText(this.textRenderer, this.text, 0, 0, this.color, this.shadow);
        if (super.isFocused()) {
            if (System.currentTimeMillis() - lastRenderCaret >= 500) {
                lastRenderCaret = System.currentTimeMillis();
                doRenderCaret = !doRenderCaret;
            }
            if (doRenderCaret) {
                if (this.caretPosition == this.text.length()) {
                    context.fill(RenderLayer.getGuiOverlay(), this.caretPositionX, this.textRenderer.fontHeight - 2, this.caretPositionX + 5, this.textRenderer.fontHeight - 1, this.caretColor);
                } else {
                    context.fill(RenderLayer.getGuiOverlay(), this.caretPositionX - 1, 0, this.caretPositionX, this.textRenderer.fontHeight + 1, this.caretColor);
                }
            }
        }

//        context.fill(RenderLayer.getGuiTextHighlight(), 0, 0, this.textWidth, this.textRenderer.fontHeight + 1, -16776961);
        s.pop();
    }

    @Override
    protected MutableText getNarrationMessage() {
        Text text = this.getMessage();
        return Text.translatable("gui.narrate.editBox", text, this.text);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage());
    }

    public void align(float v) {
        this.align = v;
    }
}
