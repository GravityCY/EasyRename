package me.gravityio.easyrename;

import me.gravityio.easyrename.network.c2s.RenamePayload;
import me.gravityio.easyrename.network.s2c.RenameResponsePayload;
import me.gravityio.easyrename.network.s2c.ScreenBlockDataPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Fabric Mod to make containers renameable
 */
public class RenameMod implements ModInitializer {
    public static final String MOD_ID = "easyrename";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final ResourceLocation RENAME_DENY_ID = id("ui.rename.fail");
    public static final SoundEvent RENAME_DENY = SoundEvent.createVariableRangeEvent(RENAME_DENY_ID);
    private static boolean IS_DEBUG = false;

    public static ResourceLocation id(String str) {
        //? if >=1.20.5 && <=1.20.6 {
         return new ResourceLocation(MOD_ID, str);
        //?} else {
        /*return ResourceLocation.fromNamespaceAndPath(MOD_ID, str);
        *///?}
    }
    public static void DEBUG(String s, Object... args) {
        if (!IS_DEBUG) return;
        LOGGER.info(s, args);
    }

    public static boolean isInBlacklist(Screen screen) {
        return !(screen instanceof AbstractContainerScreen<?>) || screen instanceof InventoryScreen || screen instanceof CreativeModeInventoryScreen;
    }

    @Override
    public void onInitialize() {
        ModConfig.HANDLER.load();
        ModConfig.INSTANCE = ModConfig.HANDLER.instance();

        IS_DEBUG = FabricLoader.getInstance().isDevelopmentEnvironment();

        Registry.register(BuiltInRegistries.SOUND_EVENT, RENAME_DENY_ID, RENAME_DENY);

        PayloadTypeRegistry.playC2S().register(RenamePayload.ID, RenamePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RenameResponsePayload.TYPE,RenameResponsePayload.CODEC);
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
        Player player = data.player();
        if (ModConfig.INSTANCE.useLevels) {
            DEBUG("Trying to use {} levels from player with {} levels", ModConfig.INSTANCE.cost, player.experienceLevel);
            if (player.experienceLevel < ModConfig.INSTANCE.cost) {
                DEBUG("Player '{}' did not have enough levels ({}) to rename ({})", player.getName(), player.experienceLevel, ModConfig.INSTANCE.cost);
                return false;
            }
            player.giveExperienceLevels(-ModConfig.INSTANCE.cost);
        } else {
            var experience = Helper.getTotalExperience(player);
            DEBUG("Trying to use {} xp from player with {} xp", ModConfig.INSTANCE.cost, experience);
            if (experience < ModConfig.INSTANCE.cost) {
                DEBUG("Player '{}' did not have enough xp ({}) to rename (config: {})", player.getName(), experience, ModConfig.INSTANCE.cost);
                return false;
            }
            player.giveExperienceLevels(-ModConfig.INSTANCE.cost);
        }
        return true;
    }

    private void doRenameItemFrame(RenameEvents.RenameData data) {
        if (!ModConfig.INSTANCE.syncItemFrame) return;

        Level world = data.world();
        BlockPos pos = data.pos();
        Component newName = data.newName();

        if (world.isClientSide) return;

        Vec3 lookPos = pos.getCenter();
        var ox = 1.1;
        var oy = 1.1;
        var oz = 1.1;
        if (Helper.isDouble(world, pos)) {
            var dir = Helper.getChestDirection(world, pos);
            var axis = dir.getAxis();
            lookPos = lookPos.relative(dir, 0.5d);
            if (axis == Direction.Axis.Z) oz *= 2;
            if (axis == Direction.Axis.X) ox *= 2;
        }

        var box = AABB.ofSize(lookPos, ox, oy, oz);
        var frames = world.getEntitiesOfClass(ItemFrame.class, box, frame -> true);
        for (ItemFrame frame : frames) {
            var stack = frame.getItem();
            if (stack.isEmpty()) continue;
            stack.set(DataComponents.CUSTOM_NAME, newName);
            frame.setItem(stack);
        }
    }


}
