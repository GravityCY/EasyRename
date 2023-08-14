package me.gravityio.easyrename.mixins.impl.client;

import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.TransitiveData;
import me.gravityio.easyrename.mixins.inter.NameableAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onOpenScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/OpenScreenS2CPacket;getScreenHandlerType()Lnet/minecraft/screen/ScreenHandlerType;"))
    private void setNameableScreen(OpenScreenS2CPacket packet, CallbackInfo ci) {
        TransitiveData.isNameable = ((NameableAccessor)packet).easyRename$isNameable();
        RenameMod.LOGGER.info("Set Transitive Data of: {}", TransitiveData.isNameable);
    }
}
