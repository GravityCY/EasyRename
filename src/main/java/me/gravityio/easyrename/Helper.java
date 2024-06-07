package me.gravityio.easyrename;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class Helper {

    /**
     * Checks if a block at the given position in the world is a double chest.
     *
     * @param world    the world in which the block is located
     * @param blockPos the position of the block
     * @return true if the block is a double chest, false otherwise
     */
    public static boolean isDouble(World world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.contains(Properties.CHEST_TYPE)) {
            ChestType type = blockState.get(ChestBlock.CHEST_TYPE);
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
    public static Direction getChestDirection(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Direction facing = state.get(ChestBlock.FACING);
        ChestType type = state.get(ChestBlock.CHEST_TYPE);

        if (type == ChestType.LEFT) {
            return facing.rotateYClockwise();
        } else {
            return facing.rotateYCounterclockwise();
        }
    }

}
