package me.gravityio.easyrename.mixins.impl;

import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.accessors.DoubleInventoryAccessor;
import me.gravityio.easyrename.network.s2c.ScreenBlockDataPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.OptionalInt;

/**
 * Everytime the player opens a screen on the server we also send our own custom packet that includes the block position that this screen belongs to
 */
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    @Inject(
            method = "openHandledScreen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void addCustomData(NamedScreenHandlerFactory factory, CallbackInfoReturnable<OptionalInt> cir, ScreenHandler screenHandler) {
        Inventory inv = null;
        BlockPos pos = null;
        if (!screenHandler.slots.isEmpty()) {
            inv = screenHandler.slots.getFirst().inventory;
        }

        if (inv instanceof LockableContainerBlockEntity entity) {
            pos = entity.getPos();
        } else if (inv instanceof DoubleInventoryAccessor dInv) {
            if (dInv.easyRename$getFirst() instanceof LockableContainerBlockEntity entity) {
                pos = entity.getPos();
            } else if (dInv.easyRename$getSecond() instanceof LockableContainerBlockEntity entity) {
                pos = entity.getPos();
            }
        }

        ServerPlayNetworking.send(this.networkHandler.player, new ScreenBlockDataPayload(pos));
        RenameMod.DEBUG("[ServerPlayerEntityMixin] Sending 'ScreenBlockDataPayload' with pos: {}", pos);
    }
}
