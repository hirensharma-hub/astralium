package com.hirensharma.astralium.network;

import com.hirensharma.astralium.ExcavationTool;
import com.hirensharma.astralium.MiningMode;
import com.hirensharma.astralium.client.AstraliumClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public final class MiningModeSyncPacket {
    private final int slot;
    private final int toolOrdinal;
    private final int modeOrdinal;

    public MiningModeSyncPacket(int slot, ExcavationTool tool, MiningMode mode) {
        this.slot = slot;
        this.toolOrdinal = tool.ordinal();
        this.modeOrdinal = mode.ordinal();
    }

    private MiningModeSyncPacket(int slot, int toolOrdinal, int modeOrdinal) {
        this.slot = slot;
        this.toolOrdinal = toolOrdinal;
        this.modeOrdinal = modeOrdinal;
    }

    public static void encode(MiningModeSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.slot);
        buffer.writeVarInt(packet.toolOrdinal);
        buffer.writeVarInt(packet.modeOrdinal);
    }

    public static MiningModeSyncPacket decode(FriendlyByteBuf buffer) {
        return new MiningModeSyncPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(MiningModeSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.slot < 0 || packet.slot >= 9
                || packet.toolOrdinal < 0 || packet.toolOrdinal >= ExcavationTool.values().length
                || packet.modeOrdinal < 0 || packet.modeOrdinal >= MiningMode.values().length) return;
            ExcavationTool tool = ExcavationTool.values()[packet.toolOrdinal];
            MiningMode mode = MiningMode.values()[packet.modeOrdinal];
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AstraliumClient.applyMiningMode(packet.slot, tool, mode));
        });
        context.setPacketHandled(true);
    }
}
