package me.gravityio.easyrename.mixins.impl.client;


import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.gravityio.easyrename.EditableTextLabelWidget;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.GlobalData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Modifies the HandledScreen in order to
 *  <ul>
 *      <li>
 *          When the screen is of a nameable container
 *          <ul>
 *              <li>Hide the vanilla container title</li>
 *              <li>Add our own title widget that is also editable when clicked</li>
 *              <li>Disallow Closing the screen when Escape is hit in order for the editable name to be cancellable</li>
 *          </ul>
 *     </li>
 * </ul>
 */
@Mixin(HandledScreen.class)
public class HandledScreenMixin extends Screen {
    @Shadow protected int titleX;
    @Shadow protected int x;
    @Shadow protected int titleY;
    @Shadow protected int y;
    @Unique
    EditableTextLabelWidget label;
    @Unique
    boolean isNameable = false;

    protected HandledScreenMixin(Text title) {
        super(title);
    }


    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        this.isNameable = GlobalData.isNameable;

        RenameMod.LOGGER.info("Init Handled Screen");
        if (!this.isNameable) return;
        RenameMod.LOGGER.info("Nameable Screen");

        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
        var isCentered = false;
        var x = this.titleX + this.x;
        var y = this.titleY + this.y;
        if (self instanceof AbstractFurnaceScreen<?> || self instanceof BrewingStandScreen) {
            isCentered = true;
            x = this.width / 2;
        }

        this.label = new EditableTextLabelWidget(super.client, super.textRenderer, super.title, x, y, isCentered);
        this.addDrawableChild(this.label);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (this.isNameable)
            return this.label != null && !this.label.isTyping;
        return true;
    }

    @WrapWithCondition(
            method = "drawForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I", ordinal = 0)
    )
    private boolean drawTitleIf(TextRenderer rend, MatrixStack matrices, Text title, float titleX, float titleY, int color) {
        return !this.isNameable;
    }
}
