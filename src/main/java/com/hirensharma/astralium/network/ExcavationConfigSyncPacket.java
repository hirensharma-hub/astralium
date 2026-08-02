package com.hirensharma.astralium.network;

import com.hirensharma.astralium.ExcavationConfigSnapshot;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public final class ExcavationConfigSyncPacket {
    private final ExcavationConfigSnapshot snapshot;

    public ExcavationConfigSyncPacket(ExcavationConfigSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public static void encode(ExcavationConfigSyncPacket packet, FriendlyByteBuf buffer) {
        ExcavationConfigSnapshot snapshot = packet.snapshot;
        buffer.writeBoolean(snapshot.enabled());
        buffer.writeBoolean(snapshot.pickaxeEnabled());
        buffer.writeVarInt(snapshot.pickaxeModes());
        buffer.writeBoolean(snapshot.shovelEnabled());
        buffer.writeVarInt(snapshot.shovelModes());
        buffer.writeBoolean(snapshot.protectBlockEntities());
        buffer.writeBoolean(snapshot.creativeAreaMining());
    }

    public static ExcavationConfigSyncPacket decode(FriendlyByteBuf buffer) {
        return new ExcavationConfigSyncPacket(new ExcavationConfigSnapshot(
            buffer.readBoolean(),
            buffer.readBoolean(),
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readBoolean()));
    }

    public static void handle(ExcavationConfigSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ExcavationConfigSnapshot.applyClient(packet.snapshot));
        context.setPacketHandled(true);
    }
}
