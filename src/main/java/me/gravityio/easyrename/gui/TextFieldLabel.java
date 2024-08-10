package me.gravityio.easyrename.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.gravityio.easyrename.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

//? if <1.20.5 {
import net.minecraft.SharedConstants;
//?}

/**
 * Pretty much just some simple rendered text that is also clickable and then editable.
 */
public class TextFieldLabel extends AbstractWidget {
    public Consumer<String> onEnterCB;
    public Consumer<String> onChangedCB;
    public String text;

    protected String original;
    protected final Font textRenderer;
    protected int color = 0xFF_404040;
    protected boolean shadow;
    protected int caretIndex;
    protected int caretEndIndex;
    protected int caretColor = 0xFF_FFFFFF;
    protected float align = 0;
    protected int caretPosition = 0;
    protected int caretEndPosition = 0;
    protected int textWidth = 0;
    protected long lastRenderCaret = System.currentTimeMillis();
    protected boolean doRenderCaret;
    protected int padding = 0;
    protected int renderX;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

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
    public static int getIndexAt(Font textRenderer, int x, int sx, String text) {
        int i = Mth.floor(x) - sx;
        return textRenderer.plainSubstrByWidth(text, i).length();
    }

    public TextFieldLabel(Font textRenderer, int x, int y, int width, int height, @NotNull Component message) {
        super(x, y, width, height, message);
        this.textRenderer = textRenderer;
        this.setText(super.getMessage().getString());
    }

    public boolean fits(String str) {
        return this.textRenderer.width(str) < super.width - padding * 2;
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
            this.write(Minecraft.getInstance().keyboardHandler.getClipboard());
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        } else if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.text.substring(this.getSmallCaret(), this.getBigCaret()));
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        } else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.text.substring(this.getSmallCaret(), this.getBigCaret()));
            this.remove(0, true);
            this.playDownSound(Minecraft.getInstance().getSoundManager());
        } else if (Screen.isSelectAll(keyCode)) {
            this.setCaretIndex(0);
            this.setCaretEndIndex(this.text.length());
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (this.isDisabled()) return false;

        //? if >=1.20.5 {
        /*if (StringUtil.isAllowedChatCharacter(c)) {
        *///?} else {
        if (SharedConstants.isAllowedChatCharacter(c)) {
        //?}
            this.write(String.valueOf(c));
            return true;
        }

        return false;
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (!super.visible) return;

        PoseStack s = context.pose();
        s.pushPose();
        s.translate(this.renderX, super.getY(), 0);

        context.drawString(this.textRenderer, this.text, 0, 0, this.color, this.shadow);
        if (super.isFocused()) {
            if (System.currentTimeMillis() - lastRenderCaret >= 500) {
                lastRenderCaret = System.currentTimeMillis();
                doRenderCaret = !doRenderCaret;
            }
            if (this.caretIndex == this.caretEndIndex) {
                if (doRenderCaret) {
                    if (this.caretIndex == this.text.length()) {
                        context.fill(RenderType.guiOverlay(), this.caretPosition, this.textRenderer.lineHeight - 2, this.caretPosition + 5, this.textRenderer.lineHeight - 1, this.caretColor);
                    } else {
                        context.fill(RenderType.guiOverlay(), this.caretPosition - 1, 0, this.caretPosition, this.textRenderer.lineHeight + 1, this.caretColor);
                    }
                }
            } else {
                int a = Math.min(this.caretPosition, this.caretEndPosition);
                int b = Math.max(this.caretPosition, this.caretEndPosition);
                context.fill(RenderType.guiTextHighlight(), a, 0, b, this.textRenderer.lineHeight + 1, -16776961);
            }
        }

        s.popPose();
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        Component text = this.getMessage();
        return Component.translatable("gui.narrate.editBox", text, this.text);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, this.createNarrationMessage());
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
        return Math.max(this.caretIndex, this.caretEndIndex);
    }

    private void updateRenderX() {
        this.renderX = (int) (super.getX() + this.padding + (super.width - this.padding * 2 - this.textWidth) * align);
    }

    private void updateWidth() {
        this.textWidth = this.textRenderer.width(this.text);
    }

    private void updateCaretPosition() {
        this.caretPosition = this.textRenderer.width(this.text.substring(0, this.caretIndex));
        this.caretEndPosition = this.caretPosition;
    }

    private void updateCaretEndPosition() {
        this.caretEndPosition = this.textRenderer.width(this.text.substring(0, this.caretEndIndex));
    }

    private void setCaretIndex(int i) {
        i = Mth.clamp(i, 0, this.text.length());

        this.caretIndex = i;
        this.caretEndIndex = i;
        this.updateCaretPosition();
    }

    private void setCaretEndIndex(int i) {
        i = Mth.clamp(i, 0, this.text.length());

        this.caretEndIndex = i;
        this.updateCaretEndPosition();
    }
}
