package me.gravityio.easyrename.network.c2s;

import me.gravityio.easyrename.RenameEvents;
import me.gravityio.easyrename.RenameMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A Packet sent from the client to the server that renames the currently opened container.
 */
public class RenamePacket implements FabricPacket {
    public static final PacketType<RenamePacket> TYPE = PacketType.create(
            new Identifier(RenameMod.MOD_ID, "rename"),
            RenamePacket::new);

    private final Text text;

    public RenamePacket(PacketByteBuf buf) {
        this(buf.readText());
    }

    public RenamePacket(Text text) {
        this.text = text;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeText(this.text);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    /**
     * Renames the container associated with the player's current screen handler.
     * It checks if the container is a double chest or a lockable block entity.
     * If it is a double chest, it attempts to rename both chests.
     * If it is a lockable block entity, it renames it.
     */
    public void apply(ServerPlayerEntity serverPlayerEntity, PacketSender packetSender) {
        var inv = serverPlayerEntity.currentScreenHandler.slots.get(0).inventory;

        boolean isValid = inv instanceof DoubleInventory || inv instanceof LockableContainerBlockEntity;

        if (!isValid) return;

        World world;
        BlockPos pos;

        if (inv instanceof DoubleInventory doubleInventory) {
            RenameMod.DEBUG("[RenamePacket] Applying as a DoubleInventory");
            RenameMod.DEBUG("Setting Both to {}", this.text.getString());

            var first = (LockableContainerBlockEntity) doubleInventory.first;
            var second = (LockableContainerBlockEntity)doubleInventory.second;
            first.setCustomName(this.text);
            second.setCustomName(this.text);
            first.markDirty();
            second.markDirty();
            world = first.getWorld();
            pos = first.getPos();
        } else {
            RenameMod.DEBUG("[RenamePacket] Applying as a LockableContainerBlockEntity");
            RenameMod.DEBUG("[RenamePacket] Setting Container to '{}'", this.text.getString());

            LockableContainerBlockEntity lockable = (LockableContainerBlockEntity) inv;
            lockable.setCustomName(this.text);
            lockable.markDirty();
            world = lockable.getWorld();
            pos = lockable.getPos();
        }
        RenameEvents.ON_RENAME.invoker().onRename(world, pos, this.text);
    }
}
