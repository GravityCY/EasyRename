package me.gravityio.easyrename.mixins.impl.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.gravityio.easyrename.RenameEvents;
import me.gravityio.easyrename.gui.EditableTextLabelWidget;
import me.gravityio.easyrename.GlobalData;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.inter.NameableAccessor;
import me.gravityio.easyrename.network.c2s.RenamePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.BrewingStandScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Screen.class)
public abstract class ScreenMixin implements NameableAccessor {
    @Unique
    private boolean isNameable;
    @Unique
    private EditableTextLabelWidget label;

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
        RenameMod.LOGGER.debug("[ScreenMixin] Initializing Nameable Screen with Custom Stuff");

        var renameBlock = (LockableContainerBlockEntity) screenBlock;
        var isCentered = false;
        var x = handled.titleX + handled.x;
        var y = handled.titleY + handled.y;
        if (self instanceof AbstractFurnaceScreen<?> || self instanceof BrewingStandScreen) {
            isCentered = true;
            x = handled.x + handled.backgroundWidth / 2;
        }

        this.label = new EditableTextLabelWidget(this.client, this.textRenderer, this.title, x, y, isCentered);
        this.label.onChanged((newText) -> {
            renameBlock.setCustomName(newText);
            RenameEvents.ON_RENAME.invoker().onRename(screenBlock.getWorld(), screenBlock.getPos(), newText);
            ClientPlayNetworking.send(new RenamePacket(newText));
        });
        this.label.onTypingChanged(isTyping -> GlobalData.IS_TYPING = isTyping);
        this.addDrawableChild(this.label);
    }

    @Inject(method = "clearAndInit", at = @At("TAIL"))
    private void onClearDoSetup(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreen<?>) || !this.isNameable) return;
        this.addDrawableChild(this.label);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderDoUpdatePositions(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
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

    @ModifyReturnValue(method = "shouldCloseOnEsc", at = @At("RETURN"))
    private boolean shouldNotCloseIfTyping(boolean original) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof HandledScreen<?>) || !this.isNameable) return original;
        return this.label != null && !this.label.isTyping;
    }
}
