package me.gravityio.easyrename.mixins.impl.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.gravityio.easyrename.GlobalData;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.gui.TextFieldLabel;
import me.gravityio.easyrename.mixins.inter.NameableAccessor;
import me.gravityio.easyrename.network.c2s.RenamePayload;
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
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
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
 *          </ul>
 *     </li>
 * </ul>
 */
@Mixin(Screen.class)
public abstract class ScreenMixin implements NameableAccessor {
    @Unique
    private boolean isNameable;
    @Unique
    private TextFieldLabel field;

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
        GlobalData.SCREEN_POS = null;
        if (!this.isNameable) return;
        RenameMod.DEBUG("[ScreenMixin] Initializing Nameable Screen with Custom Stuff");

        var renameBlock = (LockableContainerBlockEntity) screenBlock;
        var x = handled.x;
        var y = handled.y + 6;
        this.field = new TextFieldLabel(this.textRenderer, x, y, handled.backgroundWidth, this.textRenderer.fontHeight, this.title);
        this.field.padding(8);
        if (handled instanceof AbstractFurnaceScreen<?> || handled instanceof BrewingStandScreen || handled instanceof Generic3x3ContainerScreen) {
            this.field.align(0.5f);
        }

        this.field.onEnterCB = (str) -> {
            var text = Text.literal(str);
            renameBlock.customName = text;
            ClientPlayNetworking.send(new RenamePayload(text));
        };

        this.addDrawableChild(this.field);
    }

    /**
     * We re-evauluate every frame, because we can't control any side effects in regard to the screen ever changing.<br>
     * Prime example is the furnace with the recipe book, as soon as the recipe book shifts the whole UI, our text is just hanging
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        GlobalData.IS_TYPING = false;
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreen<?> handled) || !this.isNameable) return;
        GlobalData.IS_TYPING = this.field.isFocused();
        this.reval(handled);
    }

    /**
     * This gets called when the screen is resized, so we just re-add the
     * label to the screen, and let the render inject to do the resizing for us.
     */
    @Inject(method = "clearAndInit", at = @At("TAIL"))
    private void onClearDoSetup(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreen<?> handled) || !this.isNameable) return;
        this.reval(handled);
        this.addDrawableChild(this.field);
    }

    @Unique
    private void reval(HandledScreen<?> handled) {
        var x = handled.x;
        var y = handled.y + 6;

        this.field.setWidth(handled.backgroundWidth);

        this.field.setX(x);
        this.field.setY(y);
    }

    /**
     * Do not close the screen when Escape is hit and is currently typing.
     */
    @ModifyReturnValue(method = "shouldCloseOnEsc", at = @At("RETURN"))
    private boolean shouldNotCloseIfTyping(boolean original) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreen<?>) || !this.isNameable) return original;
        return this.field != null && this.field.isDisabled();
    }
}
