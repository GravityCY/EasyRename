package me.gravityio.easyrename.gui;

import me.gravityio.easyrename.RenameMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * A Text Label that is supposed to spawn a TextField when clicked in order to make it editable
 */
public class EditableTextLabelWidget extends TextWidget {
    MinecraftClient client;
    TextRenderer textRenderer;
    int cx;
    boolean isCentered;
    ScuffedTextField textField;

    public boolean isTyping = false;
    private Consumer<Text> onChanged;
    private Consumer<Boolean> onTypingChanged;


    public EditableTextLabelWidget(MinecraftClient client, TextRenderer textRenderer, Text message, int x, int y, boolean isCentered) {
        super(x, y, 10, textRenderer.fontHeight, message, new FakeTextRenderer(textRenderer));
        this.client = client;
        this.textRenderer = super.getTextRenderer();
        this.isCentered = isCentered;

        if (this.isCentered) x += 7;
        this.textField = new ScuffedTextField(this.client, this.textRenderer, x, y, message, isCentered);
        this.textField.onEnter(this::onRename);
        this.textField.onEscape(() -> this.setTyping(false));
        this.textField.setEditableColor(0x404040);

        this.setX(x);
        this.onUpdateText();

        super.alignLeft();
        super.setTextColor(0x404040);
        super.active = true;

        RenameMod.LOGGER.debug("Made EditableTextLabel. [centered: {}, text: {}]", isCentered, message.getString());
    }

    /**
     * A Listener for when the actual text label changes
     */
    public void onChanged(Consumer<Text> onChanged) {
        this.onChanged = onChanged;
    }

    /**
     * A Listener for when the user starts typing or stops
     */
    public void onTypingChanged(Consumer<Boolean> onTypingChanged) {
        this.onTypingChanged = onTypingChanged;
    }

    @Override
    public void setX(int x) {
        if (this.isCentered) {
            this.onUpdateCenterPos(x);
        } else {
            this.textField.setX(x);
            super.setX(x);
        }
    }

    @Override
    public void setY(int y) {
        this.textField.setY(y);
        super.setY(y);
    }

    /**
    * Update where this elements center should be
    */

    private void onRename() {
        var rename = Text.literal(this.textField.getText());
        this.setMessage(rename);
        this.setTyping(false);
        if (this.onChanged != null) this.onChanged.accept(rename);
    }

    /**
    * Re-centers this element according to the new center position
    */
    private void onUpdateCenterPos(int nx) {
        if (this.cx == nx) return;
        this.cx = nx;

        this.textField.setX(this.cx + 7);
        this.doCenter();
    }

    /**
     * Resizes this element according to the texts width
     */
    private void onUpdateText() {
        this.doResize();
        if (this.isCentered) this.doCenter();
    }

    private void setTyping(boolean v) {
        this.isTyping = v;
        if (this.onTypingChanged != null) this.onTypingChanged.accept(v);
    }

    /**
     * Centers this element according to the center position and its current width
     */
    private void doCenter() {
        super.setX(this.cx - super.width / 2);
    }

    /**
     * Resizes this element according to the texts size
     */
    private void doResize() {
        super.width = Math.max(textRenderer.getWidth(super.getMessage()), 15);
    }

    @Override
    public void setMessage(Text message) {
        super.setMessage(message);
        this.onUpdateText();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.isTyping) {
            this.textField.render(matrices, mouseX, mouseY, delta);
        } else {
            super.render(matrices, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (this.isTyping) {
            return this.textField.isMouseOver(mouseX, mouseY);
        }
        return super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isTyping) {
            return this.textField.mouseClicked(mouseX, mouseY, button);
        }

        this.setTyping(super.clicked(mouseX, mouseY));
        this.textField.setText(super.getMessage().getString());
        this.textField.setFocused(true);
        return this.isTyping;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isTyping) {
            return this.textField.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (this.isTyping) {
            return this.textField.charTyped(chr, modifiers);
        }
        return false;
    }
}
