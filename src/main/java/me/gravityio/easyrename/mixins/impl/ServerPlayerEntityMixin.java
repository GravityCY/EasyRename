package me.gravityio.easyrename.mixins.impl;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.inter.NameableAccessor;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Debug(export = true)
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Unique
    private ScreenHandler newOne;
    @Redirect(
            method = "openHandledScreen",
            at = @At(value = "NEW", target = "(ILnet/minecraft/screen/ScreenHandlerType;Lnet/minecraft/text/Text;)Lnet/minecraft/network/packet/s2c/play/OpenScreenS2CPacket;")
    )
    private OpenScreenS2CPacket addCustomData(int syncId, ScreenHandlerType<?> type, Text name) {
        var packet = new OpenScreenS2CPacket(syncId, type, name);
        if (this.newOne == null) return packet;

        var inv = this.newOne.slots.get(0).inventory;

        var nameable = inv instanceof LockableContainerBlockEntity || inv instanceof DoubleInventory;

        RenameMod.LOGGER.info("Adding Custom Data of nameable: {}", nameable);
        ((NameableAccessor)packet).easyRename$setNameable(nameable);
        this.newOne = null;
        return packet;
    }

    @ModifyExpressionValue(method = "openHandledScreen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/NamedScreenHandlerFactory;createMenu(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/screen/ScreenHandler;")
    )
    private ScreenHandler setTransitiveData(ScreenHandler original) {
        this.newOne = original;
        return original;
    }
}
