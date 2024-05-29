package me.gravityio.easyrename.client;

import me.gravityio.easyrename.GlobalData;
import me.gravityio.easyrename.network.s2c.ScreenBlockDataPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RenameModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ScreenBlockDataPayload.ID, (payload, context) -> GlobalData.SCREEN_POS = payload.pos());
    }
}
