package me.gravityio.easyrename.mixins.accessors;

import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DoubleInventory.class)
public interface DoubleInventoryAccessor {
    @Accessor("first")
    Inventory easyRename$getFirst();
    @Accessor("second")
    Inventory easyRename$getSecond();
}
