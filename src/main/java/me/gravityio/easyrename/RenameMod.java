package me.gravityio.easyrename;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import me.gravityio.easyrename.network.c2s.RenamePayload;
import me.gravityio.easyrename.network.s2c.ScreenBlockDataPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
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
    private static boolean IS_DEBUG = false;

    public static void DEBUG(String s, Object... args) {
        if (!IS_DEBUG) return;
        LOGGER.info(s, args);
    }

    public static boolean isInBlacklist(Screen screen) {
        return !(screen instanceof HandledScreen<?>) || screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen;
    }

    @Override
    public void onPreLaunch() {
        MixinExtrasBootstrap.init();
        ModConfig.HANDLER.load();
        ModConfig.INSTANCE = ModConfig.HANDLER.instance();
    }

    @Override
    public void onInitialize() {
        IS_DEBUG = FabricLoader.getInstance().isDevelopmentEnvironment();

        PayloadTypeRegistry.playC2S().register(RenamePayload.ID, RenamePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ScreenBlockDataPayload.ID, ScreenBlockDataPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RenamePayload.ID, (payload, context) -> payload.apply(context.player(), context.responseSender()));
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
            stack.set(DataComponentTypes.CUSTOM_NAME, newName);
            frame.setHeldItemStack(stack);
        }
    }


}
