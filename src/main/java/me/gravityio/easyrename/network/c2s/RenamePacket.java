package me.gravityio.easyrename.network.c2s;

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

    public void apply(ServerPlayerEntity serverPlayerEntity, PacketSender packetSender) {
        var inv = serverPlayerEntity.currentScreenHandler.slots.get(0).inventory;
        if (inv instanceof DoubleInventory doubleInventory) {
            RenameMod.LOGGER.info("Double Inventory");
            var first = (LockableContainerBlockEntity) doubleInventory.first;
            var second = (LockableContainerBlockEntity)doubleInventory.second;
            RenameMod.LOGGER.info("Setting Both to {}", this.text.getString());
            first.setCustomName(this.text);
            second.setCustomName(this.text);
            first.markDirty();
            second.markDirty();
        } else if (inv instanceof LockableContainerBlockEntity lockable) {
            RenameMod.LOGGER.info("Lootable Container Block Entity");
            RenameMod.LOGGER.info("Setting Container to {}", this.text.getString());
            lockable.setCustomName(this.text);
            lockable.markDirty();
        }
    }
}
