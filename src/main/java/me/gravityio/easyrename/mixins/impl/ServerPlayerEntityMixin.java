package me.gravityio.easyrename.mixins.impl;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.inter.BlockPosAccessor;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Modifies the OpenScreenS2CPacket before sent in order to let the client know what is a renameable container
 */
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

        Inventory inv = null;
        BlockPos pos = null;
        if (!this.newOne.slots.isEmpty()) {
            inv = this.newOne.slots.get(0).inventory;
        }

        if (inv instanceof LockableContainerBlockEntity entity) {
            pos = entity.getPos();
        } else if (inv instanceof DoubleInventory dInv) {
            if (dInv.first instanceof LockableContainerBlockEntity entity) {
                pos = entity.getPos();
            } else if (dInv.second instanceof LockableContainerBlockEntity entity) {
                pos = entity.getPos();
            }
        }

        RenameMod.DEBUG("[ServerPlayerEntityMixin] Adding Custom Data of pos: {}", pos);
        ((BlockPosAccessor)packet).easyRename$setBlockPos(pos);
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
