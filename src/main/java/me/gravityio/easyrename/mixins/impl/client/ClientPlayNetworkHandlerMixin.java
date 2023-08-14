package me.gravityio.easyrename.mixins.impl.client;

import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.GlobalData;
import me.gravityio.easyrename.mixins.inter.NameableAccessor;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When the Packet is received on the client we set if the currently opened container should be nameable
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onOpenScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/OpenScreenS2CPacket;getScreenHandlerType()Lnet/minecraft/screen/ScreenHandlerType;"))
    private void setNameableScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
        GlobalData.isNameable = ((NameableAccessor)packet).easyRename$isNameable();
        RenameMod.LOGGER.info("Set Transitive Data of: {}", GlobalData.isNameable);
    }
}
