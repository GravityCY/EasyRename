package me.gravityio.easyrename;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RenameEvents {
    public static Event<OnRename> ON_RENAME = EventFactory.createArrayBacked(OnRename.class,
            listeners -> (world, pos, newName) -> {
                for (OnRename listener : listeners)
                    listener.onRename(world, pos, newName);
            });

    public interface OnRename {
        void onRename(World world, BlockPos pos, Text newName);
    }

}
