package me.gravityio.easyrename.network.s2c;

import me.gravityio.easyrename.RenameMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Sent in order to let the client know the block its `HandledScreen` belongs to.
 *
 * @param pos
 */
public record ScreenBlockDataPayload(BlockPos pos) implements CustomPayload {
    public final static Id<ScreenBlockDataPayload> ID = CustomPayload.id(new Identifier(RenameMod.MOD_ID, "screen_data").toString());
    public final static PacketCodec<RegistryByteBuf, ScreenBlockDataPayload> CODEC = PacketCodec.of(ScreenBlockDataPayload::write, ScreenBlockDataPayload::new);

    public ScreenBlockDataPayload(PacketByteBuf buf) {
        this((BlockPos) buf.readNullable(buf1 -> buf1.readBlockPos()));
    }

    public void write(RegistryByteBuf buf) {
        buf.writeNullable(this.pos, (buf1, value) -> buf1.writeBlockPos(value));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
