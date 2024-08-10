package me.gravityio.easyrename.mixins.accessors;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor {
    @Accessor("leftPos")
    int easyRename$getX();
    @Accessor("topPos")
    int easyRename$getY();
    @Accessor("imageWidth")
    int easyRename$getBackgroundWidth();
}
