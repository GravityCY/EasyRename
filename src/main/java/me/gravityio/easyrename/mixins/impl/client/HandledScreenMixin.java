package me.gravityio.easyrename.mixins.impl.client;


import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import me.gravityio.easyrename.mixins.inter.INameableScreen;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * We do not allow drawing the vanilla title if the screen is
 * a nameable container, we handled that ourselves
 */
@Mixin(AbstractContainerScreen.class)
public class HandledScreenMixin extends Screen {

    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @WrapWithCondition(
            method = "renderLabels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I", ordinal = 0)
    )
    private boolean drawTitleIf(GuiGraphics instance, Font textRenderer, Component text, int x, int y, int color, boolean shadow) {
        INameableScreen accessor = (INameableScreen) this;
        return !accessor.easyRename$isNameable();
    }
}
