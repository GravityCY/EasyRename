package me.gravityio.easyrename;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import me.gravityio.easyrename.network.c2s.RenamePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Fabric Mod to make containers renameable
 */
public class RenameMod implements ModInitializer, PreLaunchEntrypoint {
    public static final String MOD_ID = "renamemod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final boolean IS_DEBUG = true;

    public static void DEBUG(String s, Object... args) {
        if (!IS_DEBUG) return;
        LOGGER.info(s, args);
    }

    @Override
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();
        ModConfig.HANDLER.load();
        ModConfig.INSTANCE = ModConfig.HANDLER.instance();
    }

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(RenamePacket.TYPE, RenamePacket::apply);
        RenameEvents.ON_RENAME.register(this::onRename);
    }


    /**
     * Updates the name of all nearby item frames at the given position in the world. <br><br>
     *
     * If the config option for {@link ModConfig#syncItemFrame syncItemFrames} is Enabled
     */
    private void onRename(World world, BlockPos pos, Text newName) {
        if (world.isClient || !ModConfig.INSTANCE.syncItemFrame) return;

        Vec3d lookPos = pos.toCenterPos();
        var ox = 1.1;
        var oy = 1.1;
        var oz = 1.1;
        if (Helper.isDouble(world, pos)) {
            var dir = Helper.getChestDirection(world, pos);
            var axis = dir.getAxis();
            lookPos = lookPos.offset(dir, 0.5d);
            if (axis == Direction.Axis.Z) oz *= 2;
            if (axis == Direction.Axis.X) ox *= 2;
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
