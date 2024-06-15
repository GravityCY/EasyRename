package me.gravityio.easyrename;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

    public record RenameData(PlayerEntity player, World world, BlockPos pos, Text oldName, Text newName){}

}
