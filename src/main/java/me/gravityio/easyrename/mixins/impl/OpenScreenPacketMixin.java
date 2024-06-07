package me.gravityio.easyrename.mixins.impl;

import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.inter.BlockPosAccessor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds some extra data to the OpenScreenS2CPacket in order to let
 * the client know what is a renameable container <br><br>
 *
 * The client doesn't know what block its currently opened screen belongs to,
 * so from what I can see there's no reliable way to tell the client whether its screen should be renameable
 */
@Mixin(OpenScreenS2CPacket.class)
public class OpenScreenPacketMixin implements BlockPosAccessor {

    @Unique
    BlockPos pos;

    @Override
    public BlockPos easyRename$getBlockPos() {
        return this.pos;
    }

    @Override
    public void easyRename$setBlockPos(BlockPos pos) {
        this.pos = pos;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void onSend(PacketByteBuf buf, CallbackInfo ci) {
        this.pos = this.pos == null ? BlockPos.ORIGIN : this.pos;
        RenameMod.DEBUG("[OpenScreenPacketMixin] Sending OpenScreenPacket of pos: {}", this.pos);
        buf.writeNullable(this.pos, PacketByteBuf::writeBlockPos);
    }

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void onReceive(PacketByteBuf buf, CallbackInfo ci) {
        this.pos = buf.readNullable(PacketByteBuf::readBlockPos);
        RenameMod.DEBUG("[OpenScreenPacketMixin] Receiving OpenScreenPacket of pos: {}", this.pos);
    }


}
