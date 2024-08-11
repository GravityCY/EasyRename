package me.gravityio.easyrename.mixins.impl;

import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.accessors.DoubleInventoryAccessor;
import me.gravityio.easyrename.network.s2c.ScreenBlockDataPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
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
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow public ServerGamePacketListenerImpl connection;

    @Inject(
            method = "openMenu",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    private void addCustomData(MenuProvider factory, CallbackInfoReturnable<OptionalInt> cir, AbstractContainerMenu screenHandler) {
        Container inv = null;
        BlockPos pos = null;
        if (!screenHandler.slots.isEmpty()) {
            inv = screenHandler.slots.get(0).container;
        }

        if (inv instanceof BaseContainerBlockEntity entity) {
            pos = entity.getBlockPos();
        } else if (inv instanceof DoubleInventoryAccessor dInv) {
            if (dInv.easyRename$getFirst() instanceof BaseContainerBlockEntity entity) {
                pos = entity.getBlockPos();
            } else if (dInv.easyRename$getSecond() instanceof BaseContainerBlockEntity entity) {
                pos = entity.getBlockPos();
            }
        }

        ServerPlayNetworking.send(this.connection.player, new ScreenBlockDataPayload(pos));
        RenameMod.DEBUG("[ServerPlayerEntityMixin] Sending 'ScreenBlockDataPayload' with pos: {}", pos);
    }
}
