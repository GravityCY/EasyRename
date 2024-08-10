package me.gravityio.easyrename.network.c2s;

import me.gravityio.easyrename.RenameEvents;
import me.gravityio.easyrename.RenameMod;
import me.gravityio.easyrename.mixins.accessors.DoubleInventoryAccessor;
import me.gravityio.easyrename.mixins.accessors.LockableContainerBlockEntityAccessor;
import me.gravityio.easyrename.network.s2c.RenameResponsePayload;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.jetbrains.annotations.NotNull;

//? if >=1.20.5 {
/*import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
*///?} else {
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
//?}

//? if =1.20 {

//?} else {
/*import net.minecraft.network.chat.ComponentSerialization;
*///?}

import static me.gravityio.easyrename.RenameMod.DEBUG;

/**
 * A Packet sent from the client to the server that renames the currently opened container.
 */
//? if >=1.20.5 {
/*public class RenamePayload implements CustomPacketPayload {
    public static final Type<RenamePayload> ID = new CustomPacketPayload.Type<>(RenameMod.id("rename"));
    public static final StreamCodec<FriendlyByteBuf, RenamePayload> CODEC = StreamCodec.ofMember(RenamePayload::write, RenamePayload::new);
*///?} else {
    public class RenamePayload implements FabricPacket {
        public static final PacketType<RenamePayload> TYPE = PacketType.create(RenameMod.id("rename"), RenamePayload::new);

//?}
    private final Component text;

    public RenamePayload(FriendlyByteBuf buf) {
        //? if =1.20 {
        this.text = buf.readComponent();
        //?} else {
        /*this.text = buf.readJsonWithCodec(ComponentSerialization.CODEC);
        *///?}
    }

    public RenamePayload(Component text) {
        this.text = text;
    }

    public void write(FriendlyByteBuf buf) {
        //? if =1.20 {
        buf.writeComponent(this.text);
        //?} else {
        /*buf.writeJsonWithCodec(ComponentSerialization.CODEC, this.text);
        *///?}
    }

    //? if >=1.20.5 {
    /*@Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
    *///?} else {

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
    //?}

    /*
     * Renames the container associated with the player's current screen handler.
     * It checks if the container is a double chest or a lockable block entity.
     * If it is a double chest, it attempts to rename both chests.
     * If it is a lockable block entity, it renames it.
     */
    public void apply(ServerPlayer serverPlayerEntity, PacketSender packetSender) {
        if (serverPlayerEntity.containerMenu == null) return;
        if (serverPlayerEntity.containerMenu.slots.isEmpty()) return;
        var inv = serverPlayerEntity.containerMenu.slots.getFirst().container;

        boolean isValid = inv instanceof CompoundContainer || inv instanceof BaseContainerBlockEntity;
        if (!isValid) return;

        Level world;
        BlockPos pos;
        Component previous;
        Runnable run;

        if (inv instanceof DoubleInventoryAccessor doubleInventory) {
            DEBUG("[RenamePacket] Applying as a DoubleInventory");
            DEBUG("Setting Both to {}", this.text.getString());

            var first = (BaseContainerBlockEntity) doubleInventory.easyRename$getFirst();
            var second = (BaseContainerBlockEntity) doubleInventory.easyRename$getSecond();
            var firstAccess = (LockableContainerBlockEntityAccessor) first;
            var secondAccess = (LockableContainerBlockEntityAccessor) second;
            world = first.getLevel();
            pos = first.getBlockPos();
            previous = first.getCustomName() == null ? first.getName() : first.getCustomName();
            run = () -> {
                firstAccess.easyRename$setCustomName(this.text);
                secondAccess.easyRename$setCustomName(this.text);
                first.setChanged();
                second.setChanged();
            };
        } else {
            DEBUG("[RenamePacket] Applying as a LockableContainerBlockEntity");
            DEBUG("[RenamePacket] Setting Container to '{}'", this.text.getString());
            var lockable = (BaseContainerBlockEntity) inv;
            var lockableAccess = (LockableContainerBlockEntityAccessor) inv;
            world = lockable.getLevel();
            previous = lockable.getCustomName() == null ? lockable.getName() : lockable.getCustomName();
            pos = lockable.getBlockPos();
            run = () -> {
                lockableAccess.easyRename$setCustomName(this.text);
                lockable.setChanged();
            };
        }

        var data = new RenameEvents.RenameData(serverPlayerEntity, world, pos, previous, this.text);
        var success = RenameEvents.ON_RENAME.invoker().onRename(data);
        if (success)
            run.run();
        DEBUG("Sending rename response of '{}'", success);
        ServerPlayNetworking.send(serverPlayerEntity, new RenameResponsePayload(success));
    }
}
