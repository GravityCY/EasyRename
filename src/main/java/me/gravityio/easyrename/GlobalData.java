package me.gravityio.easyrename;

import net.minecraft.util.math.BlockPos;

/**
 * Data that should always associate to some context
 */
public class GlobalData {
    /**
     * Whether the currently opened screen is nameable
     */
    public static BlockPos SCREEN_POS;
    /**
     * Whether the name in the container is being typed into.
     */
    public static boolean IS_TYPING = false;
}
