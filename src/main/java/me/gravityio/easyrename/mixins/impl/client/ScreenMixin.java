package me.gravityio.easyrename.mixins.impl.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.gravityio.easyrename.GlobalData;
import me.gravityio.easyrename.RenameEvents;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.gui.EditableLabelWidget;
import me.gravityio.easyrename.mixins.inter.NameableAccessor;
import me.gravityio.easyrename.network.c2s.RenamePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Modifies HandledScreens in order to
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
@Mixin(Screen.class)
public abstract class ScreenMixin implements NameableAccessor {
    @Unique
    private boolean isNameable;
    @Unique
    private EditableLabelWidget label;

    @Shadow protected abstract <T extends Element & Drawable> T addDrawableChild(T drawableElement);

    @Shadow @Nullable protected MinecraftClient client;

    @Shadow protected TextRenderer textRenderer;

    @Shadow protected Text title;

    @Override
    public void easyRename$setNameable(boolean n) {
        this.isNameable = n;
    }

    @Override
    public boolean easyRename$isNameable() {
        return this.isNameable;
    }

    /**
     * Right after the screen is initialized we add our own title,
     * if it's a furnace or brewing stand, we make the text centered to the screen.
     */
    @Inject(
            method = "init(Lnet/minecraft/client/MinecraftClient;II)V",
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screen/Screen;init()V",
                    shift = At.Shift.AFTER
            )}
    )
    private void onAfterInitDoSetup(MinecraftClient client, int width, int height, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreen<?> handled) || GlobalData.SCREEN_POS == null) return;

        var screenBlock = this.client.world.getBlockEntity(GlobalData.SCREEN_POS);
        this.isNameable = screenBlock instanceof LockableContainerBlockEntity;
        if (!this.isNameable) return;
        RenameMod.DEBUG("[ScreenMixin] Initializing Nameable Screen with Custom Stuff");

        var renameBlock = (LockableContainerBlockEntity) screenBlock;
        var isCentered = false;
        var x = handled.titleX + handled.x;
        var y = handled.titleY + handled.y;
        if (self instanceof AbstractFurnaceScreen<?> || self instanceof BrewingStandScreen) {
            isCentered = true;
            x = handled.x + handled.backgroundWidth / 2;
        }

        this.label = new EditableLabelWidget(this.client, this.textRenderer, this.title, x, y, isCentered);
        this.label.onChanged((newText) -> {
            renameBlock.setCustomName(newText);
            RenameEvents.ON_RENAME.invoker().onRename(screenBlock.getWorld(), screenBlock.getPos(), newText);
            ClientPlayNetworking.send(new RenamePacket(newText));
        });
        this.label.onTypingChanged(isTyping -> GlobalData.IS_TYPING = isTyping);
        this.addDrawableChild(this.label);
    }

    /**
     * This gets called when the screen is resized, so we just re-add the
     * label to the screen, and let the render inject to do the resizing for us.
     */
    @Inject(method = "clearAndInit", at = @At("TAIL"))
    private void onClearDoSetup(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreen<?>) || !this.isNameable) return;
        this.addDrawableChild(this.label);
    }

    /**
     * Everytime it renders I just try to center the label to
     * where it probably should be, in case the x and y positions change. <br><br>
     *
     * Eg. When the screen gets resized, when the recipe book is opened, stuff gets shifted etc.
     */

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderDoUpdatePositions(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreen<?> handled) || !this.isNameable) return;

        var x = handled.titleX + handled.x;
        var y = handled.titleY + handled.y;
        if (self instanceof AbstractFurnaceScreen<?> || self instanceof BrewingStandScreen) {
            x = handled.x + handled.backgroundWidth / 2;
        }
        this.label.setX(x);
        this.label.setY(y);
    }

    /**
     * Do not close the screen when Escape is hit and is currently typing.
     */
    @ModifyReturnValue(method = "shouldCloseOnEsc", at = @At("RETURN"))
    private boolean shouldNotCloseIfTyping(boolean original) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreen<?>) || !this.isNameable) return original;
        return this.label != null && !this.label.isTyping;
    }
}
