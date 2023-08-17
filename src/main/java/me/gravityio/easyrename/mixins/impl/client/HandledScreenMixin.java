package me.gravityio.easyrename.mixins.impl.client;


import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.gravityio.easyrename.mixins.inter.NameableAccessor;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
public class HandledScreenMixin extends Screen  {

    protected HandledScreenMixin(Text title) {
        super(title);
    }


//    @Inject(method = "init", at = @At("TAIL"))
//    private void onInit(CallbackInfo ci) {
//        this.isNameable = GlobalData.isNameable;
//
//        RenameMod.LOGGER.info("Init Handled Screen");
//        if (!this.isNameable) return;
//        RenameMod.LOGGER.info("Nameable Screen");
//
//        HandledScreen<?> self = (HandledScreen<?>) (Object) this;
//        var isCentered = false;
//        var x = this.titleX + this.x;
//        var y = this.titleY + this.y;
//        if (self instanceof AbstractFurnaceScreen<?> || self instanceof BrewingStandScreen) {
//            isCentered = true;
//            x = this.x + this.backgroundWidth / 2;
//        }
//
//        this.label = new EditableTextLabelWidget(super.client, super.textRenderer, super.title, x, y, isCentered);
//        this.addDrawableChild(this.label);
//    }

    @WrapWithCondition(
            method = "drawForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/font/TextRenderer;draw(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/text/Text;FFI)I", ordinal = 0)
    )
    private boolean drawTitleIf(TextRenderer rend, MatrixStack matrices, Text title, float titleX, float titleY, int color) {
        var tag = new NbtCompound();
        NameableAccessor accessor = (NameableAccessor) this;
        return !accessor.easyRename$isNameable();
    }
}
