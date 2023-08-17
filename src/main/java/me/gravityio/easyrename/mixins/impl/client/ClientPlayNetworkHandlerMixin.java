package me.gravityio.easyrename.mixins.impl.client;

import me.gravityio.easyrename.GlobalData;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.inter.BlockPosAccessor;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Retrieves the block position of the current screen to be opened from the packet,
 * allowing the client to identify the block it is interacting with.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onOpenScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/OpenScreenS2CPacket;getScreenHandlerType()Lnet/minecraft/screen/ScreenHandlerType;"))
    private void setNameableScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
        GlobalData.SCREEN_POS = ((BlockPosAccessor)packet).easyRename$getBlockPos();
        RenameMod.LOGGER.debug("[ClientPlayNetworkHandlerMixin] Setting Global Data of: {}", GlobalData.SCREEN_POS);
    }
}
