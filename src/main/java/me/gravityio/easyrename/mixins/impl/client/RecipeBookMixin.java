package me.gravityio.easyrename.mixins.impl.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.gravityio.easyrename.GlobalData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Probably a better way to do this, but if user is typing in the title, we don't allow this to run.
 */
@Mixin(RecipeBookWidget.class)
public class RecipeBookMixin {

    @Shadow protected MinecraftClient client;

    @ModifyExpressionValue(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;matchesKey(II)Z"))
    private boolean disallowFocusWhenTyping(boolean original) {
        return original && !GlobalData.IS_TYPING;
    }

}
