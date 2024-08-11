package me.gravityio.easyrename.network.s2c;

import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.inter.INameableScreen;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

//? if >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
*///?}

/**
 * A Packet sent from the client to the server that renames the currently opened container.
 */
//? if >=1.20.5 {
public class RenameResponsePayload implements CustomPacketPayload {
    public static final Type<RenameResponsePayload> TYPE = new Type<>(RenameMod.id("rename_response"));
    public static final StreamCodec<FriendlyByteBuf, RenameResponsePayload> CODEC = StreamCodec.ofMember(RenameResponsePayload::write, RenameResponsePayload::new);
//?} else {
/*public class RenameResponsePayload implements FabricPacket {
    public static final PacketType<RenameResponsePayload> TYPE = PacketType.create(RenameMod.id("rename_response"), RenameResponsePayload::new);

*///?}

    private final boolean success;

    public RenameResponsePayload(FriendlyByteBuf buf) {
        this.success = buf.readBoolean();
    }

    public RenameResponsePayload(boolean success) {
        this.success = success;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.success);
    }

    //? if >=1.20.5 {
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    //?} else {
    /*@Override
    public PacketType<?> getType() {
        return TYPE;
    }
    *///?}

    public void apply(Minecraft client, PacketSender packetSender) {
        var screen = (INameableScreen) client.screen;
        if (screen == null) return;
        screen.easyRename$onResponse(this.success);
    }
}
