package me.gravityio.easyrename;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
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
    public static Event<OnRename> ON_RENAME = EventFactory.createArrayBacked(OnRename.class,
            listeners -> (world, pos, newName) -> {
                for (OnRename listener : listeners)
                    listener.onRename(world, pos, newName);
            });

    public interface OnRename {
        /**
         * Called when a rename operation is performed.
         *
         * @param world    the world in which the rename operation occurs
         * @param pos      the position of the container being renamed
         * @param newName  the new name for the container
         */
        void onRename(World world, BlockPos pos, Text newName);
    }

}
