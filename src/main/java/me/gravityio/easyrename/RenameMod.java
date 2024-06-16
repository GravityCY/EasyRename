package me.gravityio.easyrename;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import me.gravityio.easyrename.network.c2s.RenamePayload;
import me.gravityio.easyrename.network.s2c.RenameResponsePayload;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
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
    public static final String MOD_ID = "easyrename";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final Identifier RENAME_DENY_ID = id("ui.rename.fail");
    public static final SoundEvent RENAME_DENY = SoundEvent.of(RENAME_DENY_ID);
    private static boolean IS_DEBUG = false;

    public static Identifier id(String str) {
        return Identifier.of(MOD_ID, str);
    }
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

        Registry.register(Registries.SOUND_EVENT, RENAME_DENY_ID, RENAME_DENY);

        PayloadTypeRegistry.playC2S().register(RenamePayload.ID, RenamePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RenameResponsePayload.ID,RenameResponsePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ScreenBlockDataPayload.ID, ScreenBlockDataPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RenamePayload.ID, (payload, context) -> payload.apply(context.player(), context.responseSender()));
        RenameEvents.ON_RENAME.register(this::onRename);
    }

    /**
     * Updates the name of all nearby item frames at the given position in the world. <br><br>
     * <p>
     * If the config option for {@link ModConfig#syncItemFrame syncItemFrames} is Enabled
     */
    private boolean onRename(RenameEvents.RenameData data) {
        var state = data.world().getBlockState(data.pos());
        DEBUG("Changed name of container '{}', from '{}' to '{}'", state.getBlock().getName().getString(), data.oldName().getString(), data.newName().getString());

        this.doRenameItemFrame(data);
        return this.doUseXP(data);
    }

    private boolean doUseXP(RenameEvents.RenameData data) {
        if (!ModConfig.INSTANCE.useXP) return true;
        PlayerEntity player = data.player();
        if (ModConfig.INSTANCE.useLevels) {
            DEBUG("Trying to use {} levels from player with {} levels", ModConfig.INSTANCE.cost, player.experienceLevel);
            if (player.experienceLevel < ModConfig.INSTANCE.cost) {
                DEBUG("Player '{}' did not have enough levels ({}) to rename ({})", player.getName(), player.experienceLevel, ModConfig.INSTANCE.cost);
                return false;
            }
            player.addExperienceLevels(-ModConfig.INSTANCE.cost);
        } else {
            var experience = Helper.getTotalExperience(player);
            DEBUG("Trying to use {} xp from player with {} xp", ModConfig.INSTANCE.cost, experience);
            if (experience < ModConfig.INSTANCE.cost) {
                DEBUG("Player '{}' did not have enough xp ({}) to rename (config: {})", player.getName(), experience, ModConfig.INSTANCE.cost);
                return false;
            }
            player.addExperience(-ModConfig.INSTANCE.cost);
        }
        return true;
    }

    private void doRenameItemFrame(RenameEvents.RenameData data) {
        if (!ModConfig.INSTANCE.syncItemFrame) return;

        World world = data.world();
        BlockPos pos = data.pos();
        Text newName = data.newName();

        if (world.isClient) return;

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
