package me.gravityio.easyrename.mixins.impl.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.math.Axis;
import me.gravityio.easyrename.GlobalData;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.gui.TextFieldLabel;
import me.gravityio.easyrename.mixins.accessors.HandledScreenAccessor;
import me.gravityio.easyrename.mixins.accessors.LockableContainerBlockEntityAccessor;
import me.gravityio.easyrename.mixins.inter.INameableScreen;
import me.gravityio.easyrename.network.c2s.RenamePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.gravityio.easyrename.RenameMod.DEBUG;

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
public abstract class ScreenMixin implements INameableScreen {
    // Uniques
    @Unique
    private static final long easyRename$ANIMATION_TIME = 2500;
    @Unique
    private boolean easyRename$isNameable;
    @Unique
    private TextFieldLabel easyRename$field;
    @Unique
    private BlockPos easyRename$pos;
    @Unique
    private long easyRename$timeSinceFail = -1;

    // Shadow
    @Shadow
    @Nullable
    protected Minecraft minecraft;
    @Shadow
    protected Font font;
    @Mutable
    @Final
    @Shadow
    protected Component title;
    @Shadow public int height;

    @Shadow protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T guiEventListener);

    /**
     * Right after the screen is initialized we add our own title,
     * if it's a furnace or brewing stand, we make the text centered to the screen.
     */
    @Inject(
            method = "init(Lnet/minecraft/client/Minecraft;II)V",
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;init()V",
                    shift = At.Shift.AFTER
            )}
    )
    private void onAfterInitDoSetup(Minecraft client, int width, int height, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (RenameMod.isInBlacklist(self) || GlobalData.SCREEN_POS == null) return;

        HandledScreenAccessor handled = (HandledScreenAccessor) self;

        this.easyRename$pos = GlobalData.SCREEN_POS;
        GlobalData.SCREEN_POS = null;

        var screenBlock = this.minecraft.level.getBlockEntity(this.easyRename$pos);
        this.easyRename$isNameable = screenBlock instanceof LockableContainerBlockEntityAccessor;

        if (!this.easyRename$isNameable) return;
        DEBUG("[ScreenMixin] Initializing Nameable Screen with Custom Stuff");

        var x = handled.easyRename$getX();
        var y = handled.easyRename$getY() + 6;
        this.easyRename$field = new TextFieldLabel(this.font, x, y, handled.easyRename$getBackgroundWidth(), this.font.lineHeight, this.title);
        this.easyRename$field.padding(8);
        if (handled instanceof AbstractFurnaceScreen<?> || handled instanceof BrewingStandScreen || handled instanceof DispenserScreen) {
            this.easyRename$field.align(0.5f);
        }

        this.easyRename$field.onEnterCB = (str) -> ClientPlayNetworking.send(new RenamePayload(Component.literal(str)));
        this.addRenderableWidget(this.easyRename$field);
    }

    /**
     * We re-evaluate every frame, because we can't control any side effects in regard to the screen ever-changing.<br>
     * Prime example is the furnace with the recipe book, as soon as the recipe book shifts the whole UI, our text is just hanging
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // TODO: maybe do some more tidy rendering?
        GlobalData.IS_TYPING = false;
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreenAccessor handled) || !this.easyRename$isNameable) return;
        GlobalData.IS_TYPING = this.easyRename$field.isFocused();
        this.easyRename$reval(handled);
        this.easyRename$renderXP(context, handled);
    }
    /**
     * This gets called when the screen is resized, so we just re-add the
     * label to the screen, and let the render inject to do the resizing for us.
     */
    @Inject(method = "rebuildWidgets", at = @At("TAIL"))
    private void onClearDoSetup(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreenAccessor handled) || !this.easyRename$isNameable) return;
        this.easyRename$reval(handled);
        this.addRenderableWidget(this.easyRename$field);
    }
    /**
     * Do not close the screen when Escape is hit and is currently typing.
     */
    @ModifyReturnValue(method = "shouldCloseOnEsc", at = @At("RETURN"))
    private boolean shouldNotCloseIfTyping(boolean original) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof AbstractContainerScreen<?>) || !this.easyRename$isNameable) return original;
        return this.easyRename$field != null && this.easyRename$field.isDisabled();
    }

    // Uniques
    @Unique
    private void easyRename$reval(HandledScreenAccessor handled) {
        var x = handled.easyRename$getX();
        var y = handled.easyRename$getY() + 6;

        this.easyRename$field.setWidth(handled.easyRename$getBackgroundWidth());
        this.easyRename$field.setX(x);
        this.easyRename$field.setY(y);
    }
    /**
     * We render the need xp text, with a little sin animation that rotates,
     * and then at a certain point in time we fade it out by animating it off the screen
     */
    @Unique
    private void easyRename$renderXP(GuiGraphics context, HandledScreenAccessor handled) {
        if (this.easyRename$timeSinceFail != -1) {
            long diff = System.currentTimeMillis() - this.easyRename$timeSinceFail;
            if (diff <= easyRename$ANIMATION_TIME) {
                var stack = context.pose();
                stack.pushPose();
                int textWidth = this.font.width("Need XP");
                if (easyRename$ANIMATION_TIME - diff <= 500) {
                    float percent = 1 - (easyRename$ANIMATION_TIME - diff) / 500f;
                    stack.translate(0, -this.height / 2f * percent, 0);
                }
                float x = handled.easyRename$getX() + handled.easyRename$getBackgroundWidth();
                float y = handled.easyRename$getY();
                stack.translate(x, y, 0);
                stack.mulPose(Axis.ZP.rotationDegrees(15));
                stack.mulPose(Axis.ZP.rotationDegrees((float) (15 * Math.sin(diff / 300f))));
                stack.translate(-textWidth / 2f, -4.5f, 0);
                this.font.drawInBatch("Need XP", 0, 0, 0xffff0000, true, stack.last().pose(), context.bufferSource(), Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
                stack.popPose();
            } else {
                this.easyRename$field.setColor(0x404040);
                this.easyRename$timeSinceFail = -1;
            }
        }
    }
    @Override
    public void easyRename$setNameable(boolean n) {
        this.easyRename$isNameable = n;
    }
    @Override
    public boolean easyRename$isNameable() {
        return this.easyRename$isNameable;
    }
    @Override
    public void easyRename$onResponse(boolean success) {
        DEBUG("Received rename response of '{}'", success);
        if (success) {
            var screenBlock = this.minecraft.level.getBlockEntity(this.easyRename$pos);
            if (!(screenBlock instanceof LockableContainerBlockEntityAccessor lockable)) return;
            this.title = Component.literal(this.easyRename$field.text);
            lockable.easyRename$setCustomName(this.title);
            this.minecraft.player.playSound(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 0.2f, 1);
        } else {
            this.easyRename$timeSinceFail = System.currentTimeMillis();
            this.easyRename$field.setText(this.title.getString());
            this.minecraft.player.playSound(RenameMod.RENAME_DENY, 0.6f, 1);
        }
    }
}
