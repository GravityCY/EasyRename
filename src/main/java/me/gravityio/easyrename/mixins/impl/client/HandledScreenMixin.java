package me.gravityio.easyrename.mixins.impl.client;


import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.gravityio.easyrename.mixins.inter.NameableAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * We do not allow drawing the vanilla title if the screen is
 * a nameable container, we handled that ourselves
 */
@Mixin(HandledScreen.class)
public class HandledScreenMixin extends Screen  {

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    @WrapWithCondition(
            method = "drawForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I", ordinal = 0)
    )
    private boolean drawTitleIf(TextRenderer rend, MatrixStack matrices, Text title, float titleX, float titleY, int color) {
        NameableAccessor accessor = (NameableAccessor) this;
        return !accessor.easyRename$isNameable();
    }
}
