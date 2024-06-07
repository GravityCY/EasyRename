package me.gravityio.easyrename.mixins.inter;

import net.minecraft.util.math.BlockPos;

public interface BlockPosAccessor {

    BlockPos easyRename$getBlockPos();

    void easyRename$setBlockPos(BlockPos pos);

}
