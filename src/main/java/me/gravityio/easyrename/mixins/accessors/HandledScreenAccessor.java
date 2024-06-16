package me.gravityio.easyrename.mixins.accessors;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int easyRename$getX();
    @Accessor("y")
    int easyRename$getY();
    @Accessor("backgroundWidth")
    int easyRename$getBackgroundWidth();
}
