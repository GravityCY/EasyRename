package me.gravityio.easyrename.network.s2c;

import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.inter.INameableScreen;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * A Packet sent from the client to the server that renames the currently opened container.
 */
public class RenameResponsePayload implements CustomPayload {
    public static final Id<RenameResponsePayload> ID = new CustomPayload.Id<>(Identifier.of(RenameMod.MOD_ID, "rename_response"));
    public static final PacketCodec<PacketByteBuf, RenameResponsePayload> CODEC = PacketCodec.of(RenameResponsePayload::write, RenameResponsePayload::new);

    private final boolean success;

    public RenameResponsePayload(PacketByteBuf buf) {
        this.success = buf.readBoolean();
    }

    public RenameResponsePayload(boolean success) {
        this.success = success;
    }

    public void write(PacketByteBuf buf) {
        buf.writeBoolean(this.success);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void apply(MinecraftClient client, PacketSender packetSender) {
        var screen = (INameableScreen) client.currentScreen;
        if (screen == null) return;
        screen.easyRename$onResponse(this.success);
    }
}
