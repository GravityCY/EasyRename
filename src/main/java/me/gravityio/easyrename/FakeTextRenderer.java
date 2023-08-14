package me.gravityio.easyrename;

import net.minecraft.client.font.FontStorage;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Replaces draw with shadow with a normal draw, because vanilla TextField's only draw with shadow and stuffs
 */
public class FakeTextRenderer extends TextRenderer {
    TextRenderer real;
    public FakeTextRenderer(TextRenderer real) {
        super(identifier -> new FontStorage(null, null), false);
        this.real = real;
    }

    @Override
    public int drawWithShadow(MatrixStack matrices, Text text, float x, float y, int color) {
        return this.real.draw(matrices, text, x, y, color);
    }

    @Override
    public int drawWithShadow(MatrixStack matrices, String text, float x, float y, int color) {
        return this.real.draw(matrices, text, x, y, color);
    }

    @Override
    public int drawWithShadow(MatrixStack matrices, String text, float x, float y, int color, boolean rightToLeft) {
        return this.real.draw(matrices, text, x, y, color);
    }

    @Override
    public int draw(MatrixStack matrices, String text, float x, float y, int color) {
        return this.real.draw(matrices, text, x, y, color);
    }

    @Override
    public int drawWithShadow(MatrixStack matrices, OrderedText text, float x, float y, int color) {
        return this.real.draw(matrices, text, x, y, color);
    }

    @Override
    public int draw(MatrixStack matrices, OrderedText text, float x, float y, int color) {
        return this.real.draw(matrices, text, x, y, color);
    }

    @Override
    public int draw(MatrixStack matrices, Text text, float x, float y, int color) {
        return this.real.draw(matrices, text, x, y, color);
    }

    @Override
    public String mirror(String text) {
        return this.real.mirror(text);
    }

    @Override
    public int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light) {
        return this.real.draw(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Override
    public int draw(String text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light, boolean rightToLeft) {
        return this.real.draw(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light, rightToLeft);
    }

    @Override
    public int draw(Text text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light) {
        return this.real.draw(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Override
    public int draw(OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix, VertexConsumerProvider vertexConsumers, TextLayerType layerType, int backgroundColor, int light) {
        return this.real.draw(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Override
    public void drawWithOutline(OrderedText text, float x, float y, int color, int outlineColor, Matrix4f matrix, VertexConsumerProvider vertexConsumers, int light) {
        this.real.drawWithOutline(text, x, y, color, outlineColor, matrix, vertexConsumers, light);
    }

    @Override
    public int getWidth(String text) {
        return this.real.getWidth(text);
    }

    @Override
    public int getWidth(StringVisitable text) {
        return this.real.getWidth(text);
    }

    @Override
    public int getWidth(OrderedText text) {
        return this.real.getWidth(text);
    }

    @Override
    public String trimToWidth(String text, int maxWidth, boolean backwards) {
        return this.real.trimToWidth(text, maxWidth, backwards);
    }

    @Override
    public String trimToWidth(String text, int maxWidth) {
        return this.real.trimToWidth(text, maxWidth);
    }

    @Override
    public StringVisitable trimToWidth(StringVisitable text, int width) {
        return this.real.trimToWidth(text, width);
    }

    @Override
    public void drawTrimmed(MatrixStack matrices, StringVisitable text, int x, int y, int maxWidth, int color) {
        this.real.drawTrimmed(matrices, text, x, y, maxWidth, color);
    }

    @Override
    public int getWrappedLinesHeight(String text, int maxWidth) {
        return this.real.getWrappedLinesHeight(text, maxWidth);
    }

    @Override
    public int getWrappedLinesHeight(StringVisitable text, int maxWidth) {
        return this.real.getWrappedLinesHeight(text, maxWidth);
    }

    @Override
    public List<OrderedText> wrapLines(StringVisitable text, int width) {
        return this.real.wrapLines(text, width);
    }

    @Override
    public boolean isRightToLeft() {
        return this.real.isRightToLeft();
    }

    @Override
    public TextHandler getTextHandler() {
        return this.real.getTextHandler();
    }
}
