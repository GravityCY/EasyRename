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
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * My own custom text field widget
 */
public class TextFieldLabel extends ClickableWidget {
    public Consumer<String> onEnterCB;
    public Consumer<String> onChangedCB;
    public String text;

    protected String original;
    protected final TextRenderer textRenderer;
    protected int color = 0x404040;
    protected boolean shadow;
    protected int caretIndex;
    protected int caretEndIndex;
    protected int caretColor = 0xffffffff;
    protected float align = 0;
    protected int caretPosition = 0;
    protected int caretEndPosition = 0;
    protected int textWidth = 0;
    protected long lastRenderCaret = System.currentTimeMillis();
    protected boolean doRenderCaret;
    protected int padding = 0;
    protected int renderX;

    /**
     * Removes
     */
    public static String removeInternal(String target, int amount, int caretIndex, int caretEndIndex, boolean forward, AtomicInteger newIndex) {
        amount = forward ? Math.min(target.length() - caretIndex, amount) : Math.min(caretIndex, amount);

        if (caretIndex != caretEndIndex) {
            int s = Math.min(caretIndex, caretEndIndex);
            int b = Math.max(caretIndex, caretEndIndex);

            target = target.substring(0, s) + target.substring(b);
            newIndex.set(s);
        } else {
            if (amount == 0) return target;

            if (forward) {
                if (caretIndex == 0) {
                    target = target.substring(amount);
                } else {
                    target = target.substring(0, caretIndex) + target.substring(caretIndex + amount);
                }
            } else {
                if (caretIndex == target.length()) {
                    target = target.substring(0, caretIndex - amount);
                } else if (caretIndex > 0 && caretIndex < target.length()) {
                    target = target.substring(0, caretIndex - amount) + target.substring(caretIndex);
                }
                newIndex.set(caretIndex - amount);
            }
        }

        return target;
    }

    /**
     * Writes
     */
    public static String writeInternal(String target, String add, int caretIndex, int caretEndIndex, AtomicInteger newIndex) {
        int s = Math.min(caretIndex, caretEndIndex);
        int b = Math.max(caretIndex, caretEndIndex);

        if (caretIndex == target.length()) {
            if (caretIndex != caretEndIndex) {
                target = target.substring(0, caretEndIndex) + add;
            } else {
                target += add;
            }
        } else if (caretIndex == 0) {
            if (caretIndex != caretEndIndex) {
                target = add + target.substring(caretEndIndex);
            } else {
                target = add + target;
            }
        } else {
            target = target.substring(0, s) + add + target.substring(b);
        }

        newIndex.set(s + add.length());
        return target;
    }

    /**
     * Gets which index an x value sits on in a string
     */
    public static int getIndexAt(TextRenderer textRenderer, int x, int sx, String text) {
        int i = MathHelper.floor(x) - sx;
        return textRenderer.trimToWidth(text, i).length();
    }

    public TextFieldLabel(TextRenderer textRenderer, int x, int y, int width, int height, @NotNull Text message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
        this.setText(super.getMessage().getString());
    }

    public boolean fits(String str) {
        return this.textRenderer.getWidth(str) < super.width - padding * 2;
    }

    public void padding(int newPadding) {
        this.padding = newPadding;
        this.updateRenderX();
    }

    public void align(float v) {
        this.align = v;
        this.updateRenderX();
    }

    public void setText(String text) {
        this.original = text;
        this.text = text;
        this.updateWidth();
        this.updateRenderX();
        this.setCaretIndex(this.text.length());
    }

    public boolean isDisabled() {
        return !super.visible || !super.isFocused();
    }

    /**
     * Returns the amount of characters needed to delete a whole word from the current caret index
     */
    public int getDelete(boolean forward) {
        int ci = StringHelper.getWord(this.caretIndex, this.text, forward);
        return Math.abs(this.caretIndex - ci);
    }

    /**
     * Writes a string at the current caret position, and then moves the caret by the length of the given string.
     */
    public void write(String str) {
        AtomicInteger i = new AtomicInteger();
        String t;
        if (this.caretIndex == this.caretEndIndex) {
            if (!this.fits(this.text + str)) return;
            t = TextFieldLabel.writeInternal(this.text, str, this.caretIndex, this.caretEndIndex, i);
        } else {
            t = TextFieldLabel.writeInternal(this.text, str, this.caretIndex, this.caretEndIndex, i);
            if (!this.fits(t)) return;
        }

        this.text = t;
        this.updateWidth();
        this.onTextChanged();
        this.setCaretIndex(i.get());
    }

    /**
     * Removes an amount of characters at the current caret position either forward or backwards.
     */
    public void remove(int amount, boolean forward) {
        AtomicInteger i = new AtomicInteger();
        this.text = TextFieldLabel.removeInternal(this.text, amount, this.caretIndex, this.caretEndIndex, forward, i);

        this.updateWidth();
        this.onTextChanged();
        this.setCaretIndex(i.get());
    }

    public void prevRemove(int amount, boolean forward) {
        amount = forward ? Math.min(this.text.length() - caretIndex, amount) : Math.min(caretIndex, amount);

        if (this.caretIndex != this.caretEndIndex) {
            int s = Math.min(this.caretIndex, this.caretEndIndex);
            int b = Math.max(this.caretIndex, this.caretEndIndex);

            this.text = this.text.substring(0, s) + this.text.substring(b);
            this.setCaretIndex(this.caretEndIndex);
        } else {
            if (amount == 0) return;

            if (forward) {
                if (this.caretIndex == 0) {
                    this.text = this.text.substring(amount);
                } else {
                    this.text = this.text.substring(0, this.caretIndex) + this.text.substring(this.caretIndex + amount);
                }
            } else {
                if (this.caretIndex == this.text.length()) {
                    this.text = this.text.substring(0, this.caretIndex - amount);
                } else if (this.caretIndex > 0 && this.caretIndex < this.text.length()) {
                    this.text = this.text.substring(0, caretIndex - amount) + this.text.substring(caretIndex);
                }
                this.setCaretIndex(this.caretIndex - amount);
            }
        }

        this.updateWidth();
        this.onTextChanged();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        int i = TextFieldLabel.getIndexAt(this.textRenderer, (int) Math.ceil(mouseX), this.renderX, this.text);
        if (Screen.hasShiftDown()) {
            this.setCaretEndIndex(i);
        } else {
            this.setCaretIndex(i);
        }
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
            this.original = this.text;

            this.onPressEnter();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            super.setFocused(false);
            this.setText(this.original);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_E) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (Screen.hasShiftDown()) {
                if (this.caretEndIndex == this.text.length()) return true;

                int p;
                if (Screen.hasControlDown()) {
                    p = StringHelper.getStartWord(this.caretEndIndex, this.text, true);
                } else {
                    p = this.caretEndIndex + 1;
                }

                this.setCaretEndIndex(p);
            } else {
                if (this.caretIndex == this.text.length()) return true;

                int p;
                if (Screen.hasControlDown()) {
                    p = StringHelper.getStartWord(this.caretEndIndex, this.text, true);
                } else {
                    if (this.caretIndex == this.caretEndIndex) {
                        p = this.caretIndex + 1;
                    } else {
                        p = this.caretEndIndex;
                    }
                }

                this.setCaretIndex(p);
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (Screen.hasShiftDown()) {
                if (this.caretEndIndex == 0) return true;

                int p;
                if (Screen.hasControlDown()) {
                    p = StringHelper.getStartWord(this.caretEndIndex, this.text, false);
                } else {
                    p = this.caretEndIndex - 1;
                }

                this.setCaretEndIndex(p);
            } else {
                if (this.caretIndex == 0) return true;

                int p;
                if (Screen.hasControlDown()) {
                    p = StringHelper.getStartWord(this.caretEndIndex, this.text, false);
                } else {
                    if (this.caretIndex == this.caretEndIndex) {
                        p = this.caretIndex - 1;
                    } else {
                        p = this.caretEndIndex;
                    }
                }

                this.setCaretIndex(p);
            }

            return true;
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            if (Screen.hasShiftDown()) {
                this.setCaretEndIndex(0);
            } else {
                this.setCaretIndex(0);
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            if (Screen.hasShiftDown()) {
                this.setCaretEndIndex(this.text.length());
            } else {
                this.setCaretIndex(this.text.length());
            }
            return true;
        } else if (Screen.isPaste(keyCode)) {
            this.write(MinecraftClient.getInstance().keyboard.getClipboard());
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            return true;
        } else if (Screen.isCopy(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.text.substring(this.getSmallCaret(), this.getBigCaret()));
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            return true;
        } else if (Screen.isCut(keyCode)) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.text.substring(this.getSmallCaret(), this.getBigCaret()));
            this.remove(0, true);
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
        } else if (Screen.isSelectAll(keyCode)) {
            this.setCaretIndex(0);
            this.setCaretEndIndex(this.text.length());
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
        s.translate(this.renderX, super.getY(), 0);

        context.drawText(this.textRenderer, this.text, 0, 0, this.color, this.shadow);
        if (super.isFocused()) {
            if (System.currentTimeMillis() - lastRenderCaret >= 500) {
                lastRenderCaret = System.currentTimeMillis();
                doRenderCaret = !doRenderCaret;
            }
            if (this.caretIndex == this.caretEndIndex) {
                if (doRenderCaret) {
                    if (this.caretIndex == this.text.length()) {
                        context.fill(RenderLayer.getGuiOverlay(), this.caretPosition, this.textRenderer.fontHeight - 2, this.caretPosition + 5, this.textRenderer.fontHeight - 1, this.caretColor);
                    } else {
                        context.fill(RenderLayer.getGuiOverlay(), this.caretPosition - 1, 0, this.caretPosition, this.textRenderer.fontHeight + 1, this.caretColor);
                    }
                }
            } else {
                int a = Math.min(this.caretPosition, this.caretEndPosition);
                int b = Math.max(this.caretPosition, this.caretEndPosition);
                context.fill(RenderLayer.getGuiTextHighlight(), a, 0, b, this.textRenderer.fontHeight + 1, -16776961);
            }
        }

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

    @Override
    public void setX(int x) {
        super.setX(x);
        this.updateRenderX();
    }

    protected void onTextChanged() {
        this.updateRenderX();

        if (this.onChangedCB != null) {
            this.onChangedCB.accept(this.text);
        }
    }

    protected void onPressEnter() {
        if (this.onEnterCB != null) {
            this.onEnterCB.accept(this.text);
        }
    }

    private int getSmallCaret() {
        return Math.min(this.caretIndex, this.caretEndIndex);
    }

    private int getBigCaret() {
        return Math.max(this.caretIndex,this.caretEndIndex);
    }

    private void updateRenderX() {
        this.renderX = (int) (super.getX() + this.padding + (super.width - this.padding * 2 - this.textWidth) * align);
    }

    private void updateWidth() {
        this.textWidth = this.textRenderer.getWidth(this.text);
    }

    private void updateCaretPosition() {
        this.caretPosition = this.textRenderer.getWidth(this.text.substring(0, this.caretIndex));
        this.caretEndPosition = this.caretPosition;
    }

    private void updateCaretEndPosition() {
        this.caretEndPosition = this.textRenderer.getWidth(this.text.substring(0, this.caretEndIndex));
    }

    private void setCaretIndex(int i) {
        i = MathHelper.clamp(i, 0, this.text.length());

        this.caretIndex = i;
        this.caretEndIndex = i;
        this.updateCaretPosition();
    }

    private void setCaretEndIndex(int i) {
        i = MathHelper.clamp(i, 0, this.text.length());

        this.caretEndIndex = i;
        this.updateCaretEndPosition();
    }
}
