package me.gravityio.easyrename;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Helper {

    public static boolean isDouble(World world, BlockPos blockPos) {
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.contains(Properties.CHEST_TYPE)) {
            ChestType type = blockState.get(ChestBlock.CHEST_TYPE);
            return type == ChestType.LEFT || type == ChestType.RIGHT;
        }
        return false;
    }

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
