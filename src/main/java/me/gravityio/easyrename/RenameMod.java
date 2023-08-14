package me.gravityio.easyrename;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import me.gravityio.easyrename.network.c2s.RenamePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.screen.ScreenHandlerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.throwables.MixinException;

public class RenameMod implements ModInitializer, PreLaunchEntrypoint {
    public static final String MOD_ID = "renamemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();
    }

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(RenamePacket.TYPE, RenamePacket::apply);
    }


}
