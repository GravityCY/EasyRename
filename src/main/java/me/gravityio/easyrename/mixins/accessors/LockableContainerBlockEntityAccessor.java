package me.gravityio.easyrename.mixins.accessors;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BaseContainerBlockEntity.class)
public interface LockableContainerBlockEntityAccessor {
    @Accessor("name")
    void easyRename$setCustomName(Component name);
}
