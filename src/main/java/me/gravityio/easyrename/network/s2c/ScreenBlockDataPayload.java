package me.gravityio.easyrename.network.s2c;

import me.gravityio.easyrename.RenameMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sent in order to let the client know the block its `HandledScreen` belongs to.
 *
 * @param pos
 */
public record ScreenBlockDataPayload(BlockPos pos) implements CustomPacketPayload {
    public final static Type<ScreenBlockDataPayload> ID = new Type<>(RenameMod.id("screen_data"));
    public final static StreamCodec<FriendlyByteBuf, ScreenBlockDataPayload> CODEC = StreamCodec.ofMember(ScreenBlockDataPayload::write, ScreenBlockDataPayload::new);

    public ScreenBlockDataPayload(FriendlyByteBuf buf) {
        this((BlockPos) buf.readNullable(buf1 -> buf1.readBlockPos()));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNullable(this.pos, (buf1, value) -> buf1.writeBlockPos(value));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
