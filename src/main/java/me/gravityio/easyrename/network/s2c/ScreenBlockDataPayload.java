package me.gravityio.easyrename.network.s2c;

import me.gravityio.easyrename.RenameMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

//? if >=1.20.5 {
/*import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
*///?} else {
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
//?}

/**
 * Sent in order to let the client know the block its `HandledScreen` belongs to.
 *
 */
//? if >=1.20.5 {
/*public record ScreenBlockDataPayload(BlockPos pos) implements CustomPacketPayload {
    public final static Type<ScreenBlockDataPayload> ID = new Type<>(RenameMod.id("screen_data"));
    public final static StreamCodec<FriendlyByteBuf, ScreenBlockDataPayload> CODEC = StreamCodec.ofMember(ScreenBlockDataPayload::write, ScreenBlockDataPayload::new);
*///?} else {
    public record ScreenBlockDataPayload(BlockPos pos) implements FabricPacket {
        public static final PacketType<ScreenBlockDataPayload> TYPE = PacketType.create(RenameMod.id("screen_data"), ScreenBlockDataPayload::new);
//?}

    public ScreenBlockDataPayload(FriendlyByteBuf buf) {
        //? if >=1.20.5 {
        /*this((BlockPos) buf.readNullable(object -> FriendlyByteBuf.readBlockPos(object)));
        *///?} else {
        this(buf.readNullable(FriendlyByteBuf::readBlockPos));
        //?}
    }

    public void write(FriendlyByteBuf buf) {
        //? if >=1.20.5 {
        /*buf.writeNullable(this.pos, (object, object2) -> FriendlyByteBuf.writeBlockPos(object, object2));
        *///?} else {
        buf.writeNullable(this.pos, FriendlyByteBuf::writeBlockPos);
        //?}
    }

    //? if >=1.20.5 {
    /*@Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
    *///?} else {
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
    //?}
}