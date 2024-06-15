package me.gravityio.easyrename.network.c2s;

import me.gravityio.easyrename.RenameEvents;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.accessors.DoubleInventoryAccessor;
import me.gravityio.easyrename.mixins.accessors.LockableContainerBlockEntityAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A Packet sent from the client to the server that renames the currently opened container.
 */
public class RenamePayload implements CustomPayload {
    public static final Id<RenamePayload> ID = new CustomPayload.Id<>(Identifier.of(RenameMod.MOD_ID, "rename"));
    public static final PacketCodec<PacketByteBuf, RenamePayload> CODEC = PacketCodec.of(RenamePayload::write, RenamePayload::new);

    private final Text text;

    public RenamePayload(PacketByteBuf buf) {
        this.text = buf.decodeAsJson(TextCodecs.CODEC);
    }

    public RenamePayload(Text text) {
        this.text = text;
    }

    public void write(PacketByteBuf buf) {
        buf.encodeAsJson(TextCodecs.CODEC, this.text);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Renames the container associated with the player's current screen handler.
     * It checks if the container is a double chest or a lockable block entity.
     * If it is a double chest, it attempts to rename both chests.
     * If it is a lockable block entity, it renames it.
     */
    public void apply(ServerPlayerEntity serverPlayerEntity, PacketSender packetSender) {
        if (serverPlayerEntity.currentScreenHandler == null) return;
        if (serverPlayerEntity.currentScreenHandler.slots.isEmpty()) return;
        var inv = serverPlayerEntity.currentScreenHandler.slots.getFirst().inventory;

        boolean isValid = inv instanceof DoubleInventory || inv instanceof LockableContainerBlockEntity;
        if (!isValid) return;

        World world;
        BlockPos pos;

        if (inv instanceof DoubleInventoryAccessor doubleInventory) {
            RenameMod.DEBUG("[RenamePacket] Applying as a DoubleInventory");
            RenameMod.DEBUG("Setting Both to {}", this.text.getString());

            var first = (LockableContainerBlockEntity) doubleInventory.getFirst();
            var second = (LockableContainerBlockEntity) doubleInventory.getSecond();
            var firstAccess = (LockableContainerBlockEntityAccessor) first;
            var secondAccess = (LockableContainerBlockEntityAccessor) second;
            firstAccess.setCustomName(this.text);
            secondAccess.setCustomName(this.text);
            first.markDirty();
            second.markDirty();
            world = first.getWorld();
            pos = first.getPos();
        } else {
            RenameMod.DEBUG("[RenamePacket] Applying as a LockableContainerBlockEntity");
            RenameMod.DEBUG("[RenamePacket] Setting Container to '{}'", this.text.getString());

            var lockable = (LockableContainerBlockEntity) inv;
            var lockableAccess = (LockableContainerBlockEntityAccessor) inv;
            lockableAccess.setCustomName(this.text);
            lockable.markDirty();
            world = lockable.getWorld();
            pos = lockable.getPos();
        }
        RenameEvents.ON_RENAME.invoker().onRename(world, pos, this.text);
    }
}
