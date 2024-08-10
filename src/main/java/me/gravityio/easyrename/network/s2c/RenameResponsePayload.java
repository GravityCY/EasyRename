package me.gravityio.easyrename.network.s2c;

import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.inter.INameableScreen;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * A Packet sent from the client to the server that renames the currently opened container.
 */
public class RenameResponsePayload implements CustomPacketPayload {
    public static final Type<RenameResponsePayload> TYPE = new Type<>(RenameMod.id("rename_response"));
    public static final StreamCodec<FriendlyByteBuf, RenameResponsePayload> CODEC = StreamCodec.ofMember(RenameResponsePayload::write, RenameResponsePayload::new);

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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void apply(Minecraft client, PacketSender packetSender) {
        var screen = (INameableScreen) client.screen;
        if (screen == null) return;
        screen.easyRename$onResponse(this.success);
    }
}
