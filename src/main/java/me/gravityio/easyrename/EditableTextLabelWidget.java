package me.gravityio.easyrename;

import me.gravityio.easyrename.network.c2s.RenamePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

/**
 * A Text Label that is supposed to spawn a TextField when clicked in order to make it editable
 */
public class EditableTextLabelWidget extends TextWidget {
    MinecraftClient client;
    TextRenderer textRenderer;
    int sx;
    boolean isCentered;
    ScuffedTextField textField;

    public boolean isTyping = false;


    public EditableTextLabelWidget(MinecraftClient client, TextRenderer textRenderer, Text message, int x, int y, boolean isCentered) {
        super(x, y, 10, textRenderer.fontHeight, message, new FakeTextRenderer(textRenderer));
        this.client = client;
        this.textRenderer = super.getTextRenderer();

        super.alignLeft();

        this.isCentered = isCentered;
        this.sx = x;

        this.doResize();
        if (this.isCentered) {
            this.doCenter();
        }
        if (this.isCentered)
            x += 7;
        this.textField = new ScuffedTextField(this.client, this.textRenderer, x, y, message, isCentered);
        this.textField.onEnter(() -> {
            var rename = Text.literal(this.textField.getText());
            this.setMessage(rename);
            ClientPlayNetworking.send(new RenamePacket(rename));
            this.isTyping = false;
        });
        this.textField.onUnfocus(() -> {
            this.isTyping = false;
        });
        this.textField.setEditableColor(0x404040);
        super.setTextColor(0x404040);
        super.active = true;

        RenameMod.LOGGER.info("Made EditableTextLabel. [centered: {}, text: {}]", isCentered, message.getString());
    }

    @Override
    public void setMessage(Text message) {
        super.setMessage(message);
        this.doResize();
        if (this.isCentered)
            this.doCenter();
    }

    private void doCenter() {
        super.setX(this.sx - super.width / 2);
    }

    private void doResize() {
        super.width = Math.max(textRenderer.getWidth(super.getMessage()), 15);
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

        this.isTyping = super.clicked(mouseX, mouseY);
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
