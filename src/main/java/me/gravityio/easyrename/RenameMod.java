package me.gravityio.easyrename;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import me.gravityio.easyrename.network.c2s.RenamePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.throwables.MixinException;

/**
 * A Fabric Mod to make containers renameable
 */
public class RenameMod implements ModInitializer, PreLaunchEntrypoint {
    public static final String MOD_ID = "renamemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();
        ModConfig.GSON.load();
        ModConfig.INSTANCE = ModConfig.GSON.getConfig();
    }

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(RenamePacket.TYPE, RenamePacket::apply);
        RenameEvents.ON_RENAME.register(this::onRename);
    }

    private void onRename(World world, BlockPos pos, Text newName) {
        if (world.isClient || !ModConfig.INSTANCE.syncItemFrame) return;

        Vec3d lookPos = pos.toCenterPos();
        var ox = 1.2d;
        var oy = 1.2d;
        var oz = 1.2d;
        if (Helper.isDouble(world, pos)) {
            lookPos = lookPos.offset(Helper.getChestDirection(world, pos), 0.5d);
            ox *= 2;
            oz *= 2;
        }

        var box = Box.of(lookPos, ox, oy, oz);
        var frames = world.getEntitiesByClass(ItemFrameEntity.class, box, frame -> true);
        for (ItemFrameEntity frame : frames) {
            if (frame.getHeldItemStack() == null || frame.getHeldItemStack().isEmpty()) continue;
            var stack = frame.getHeldItemStack();
            stack.setCustomName(newName);
            frame.setHeldItemStack(stack);
        }
    }


}
