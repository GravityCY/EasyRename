package me.gravityio.easyrename;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A class that handles events related to renaming containers.
 */
public class RenameEvents {
    /**
     * Event triggered when a rename operation is performed.
     */
    public static Event<OnRename> ON_RENAME = EventFactory.createArrayBacked(OnRename.class, listeners -> (data) -> {
        boolean allow = true;
        for (OnRename listener : listeners)
            if (!listener.onRename(data))
                allow = false;
        return allow;
    });

    public interface OnRename {
        /**
         * Called when a rename operation is performed.
         *
         * @param data the data of the rename operation
         * @return false to cancel the rename operation
         */
        boolean onRename(RenameData data);
    }

    public record RenameData(Player player, Level world, BlockPos pos, Component oldName, Component newName){}

}
