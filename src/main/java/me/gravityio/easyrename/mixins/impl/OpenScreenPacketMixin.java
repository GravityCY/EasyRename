package me.gravityio.easyrename.mixins.impl;

import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.inter.NameableAccessor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
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
public class OpenScreenPacketMixin implements NameableAccessor {
    @Unique
    boolean nameable = false;
    @Override
    public void easyRename$setNameable(boolean nv) {
        this.nameable = nv;
    }

    @Override
    public boolean easyRename$isNameable() {
        return this.nameable;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void onSend(PacketByteBuf buf, CallbackInfo ci) {
        RenameMod.LOGGER.info("Sending OpenScreenPacket of nameable: {}", this.nameable);
        buf.writeBoolean(this.nameable);
    }

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    private void onReceive(PacketByteBuf buf, CallbackInfo ci) {
        this.nameable = buf.readBoolean();
        RenameMod.LOGGER.info("Receiving OpenScreenPacket of nameable: {}", this.nameable);

    }
}
