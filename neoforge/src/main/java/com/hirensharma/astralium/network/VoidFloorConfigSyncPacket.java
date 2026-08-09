package com.hirensharma.astralium.network;

import com.hirensharma.astralium.VoidFloorConfigSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public final class VoidFloorConfigSyncPacket {
    private final VoidFloorConfigSnapshot snapshot;

    public VoidFloorConfigSyncPacket(VoidFloorConfigSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    public static void encode(VoidFloorConfigSyncPacket packet, FriendlyByteBuf buffer) {
        VoidFloorConfigSnapshot snapshot = packet.snapshot;
        List<String> dimensions = snapshot.dimensions().stream().limit(256).toList();
        buffer.writeBoolean(snapshot.enabled());
        buffer.writeVarInt(dimensions.size());
        dimensions.forEach(dimension -> buffer.writeUtf(dimension, 256));
        buffer.writeVarInt(snapshot.floorOffset());
        buffer.writeDouble(snapshot.resetMargin());
        buffer.writeBoolean(snapshot.protectAstraliumItems());
    }

    public static VoidFloorConfigSyncPacket decode(FriendlyByteBuf buffer) {
        boolean enabled = buffer.readBoolean();
        int count = Math.max(0, Math.min(buffer.readVarInt(), 256));
        List<String> dimensions = new ArrayList<>(count);
        for (int index = 0; index < count; index++) dimensions.add(buffer.readUtf(256));
        return new VoidFloorConfigSyncPacket(new VoidFloorConfigSnapshot(
            enabled,
            dimensions,
            buffer.readVarInt(),
            buffer.readDouble(),
            buffer.readBoolean()));
    }

    public static void handle(VoidFloorConfigSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> VoidFloorConfigSnapshot.applyClient(packet.snapshot));
        context.setPacketHandled(true);
    }
}
