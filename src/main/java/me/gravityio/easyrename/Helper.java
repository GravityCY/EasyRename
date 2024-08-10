package me.gravityio.easyrename;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;

public class Helper {

    /**
     * Gets the total experience of a player
     */
    public static int getTotalExperience(Player player) {
        int xp = player.totalExperience == 0 ? (int) (player.experienceProgress * getExperienceForLevel(player.experienceLevel + 1)) : player.totalExperience;
        for (int i = 0; i < player.experienceLevel; i++) {
            xp += getExperienceForLevel(i);
        }
        return xp;
    }

    /**
     * Gets the xp requirement to progress to the next level
     */
    public static int getExperienceForLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }

    /**
     * Lerp colours
     */
    public static int lerp(int argb, int toargb, float delta, boolean alpha) {
        int a = (argb & 0xFF000000) >> 24;
        int r = (argb & 0xFF0000) >> 16;
        int g = (argb & 0xFF00) >> 8;
        int b = argb & 0xFF;

        int ta = (toargb & 0xFF000000) >> 24;
        int tr = (toargb & 0xFF0000) >> 16;
        int tg = (toargb & 0xFF00) >> 8;
        int tb = toargb & 0xFF;

        if (alpha) {
            a = (int) Mth.lerp(delta, a, ta) << 24;
        }
        r = (int) Mth.lerp(delta, r, tr) << 16;
        g = (int) Mth.lerp(delta, g, tg) << 8;
        b = (int) Mth.lerp(delta, b, tb);

        return a | r | g | b;
    }

    /**
     * Checks if a block at the given position in the world is a double chest.
     *
     * @param world    the world in which the block is located
     * @param blockPos the position of the block
     * @return true if the block is a double chest, false otherwise
     */
    public static boolean isDouble(Level world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.hasProperty(BlockStateProperties.CHEST_TYPE)) {
            ChestType type = blockState.getValue(ChestBlock.TYPE);
            return type == ChestType.LEFT || type == ChestType.RIGHT;
        }
        return false;
    }

    /**
     * Gets the direction of the other chest of a double chest relative to the given pos
     *
     * @param world the world in which the chest is located
     * @param pos   the position of the chest in the world
     * @return the direction of the other chest relative to the given position
     */
    public static Direction getChestDirection(Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Direction facing = state.getValue(ChestBlock.FACING);
        ChestType type = state.getValue(ChestBlock.TYPE);

        if (type == ChestType.LEFT) {
            return facing.getClockWise();
        } else {
            return facing.getCounterClockWise();
        }
    }

}
