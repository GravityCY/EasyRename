package me.gravityio.easyrename.client;

import me.gravityio.easyrename.GlobalData;
import me.gravityio.easyrename.network.s2c.RenameResponsePayload;
import me.gravityio.easyrename.network.s2c.ScreenBlockDataPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class RenameModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //? if >=1.20.5 {
        /*ClientPlayNetworking.registerGlobalReceiver(ScreenBlockDataPayload.ID, (payload, context) -> GlobalData.SCREEN_POS = payload.pos());
        ClientPlayNetworking.registerGlobalReceiver(RenameResponsePayload.TYPE, (payload, context) -> payload.apply(context.client(),context.responseSender()));
        *///?} else {
            ClientPlayNetworking.registerGlobalReceiver(ScreenBlockDataPayload.TYPE, (packet, player, responseSender) -> GlobalData.SCREEN_POS = packet.pos());
            ClientPlayNetworking.registerGlobalReceiver(RenameResponsePayload.TYPE, (packet, player, responseSender) -> packet.apply(Minecraft.getInstance(),responseSender));
        //?}
    }
}
