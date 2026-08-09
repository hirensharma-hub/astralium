package com.hirensharma.astralium.registry;

import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.ExcavationConfigSnapshot;
import com.hirensharma.astralium.ExcavationTool;
import com.hirensharma.astralium.MiningMode;
import com.hirensharma.astralium.VoidFloorConfigSnapshot;
import com.hirensharma.astralium.network.CycleMiningModePacket;
import com.hirensharma.astralium.network.DoubleJumpPacket;
import com.hirensharma.astralium.network.DoubleJumpApprovedPacket;
import com.hirensharma.astralium.network.ExcavationConfigSyncPacket;
import com.hirensharma.astralium.network.MiningModeSyncPacket;
import com.hirensharma.astralium.network.VoidFloorConfigSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.PacketDistributor;

public final class ModNetworking {
    private static final String PROTOCOL = "8";
    public static SimpleChannel CHANNEL;
    private ModNetworking() {}

    public static void register(FMLCommonSetupEvent event) {
        CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(AstraliumMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();
        CHANNEL.messageBuilder(CycleMiningModePacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
            .encoder(CycleMiningModePacket::encode)
            .decoder(CycleMiningModePacket::decode)
            .consumerMainThread(CycleMiningModePacket::handle)
            .add();
        CHANNEL.messageBuilder(DoubleJumpPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
            .encoder(DoubleJumpPacket::encode)
            .decoder(DoubleJumpPacket::decode)
            .consumerMainThread(DoubleJumpPacket::handle)
            .add();
        CHANNEL.messageBuilder(DoubleJumpApprovedPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(DoubleJumpApprovedPacket::encode)
            .decoder(DoubleJumpApprovedPacket::decode)
            .consumerMainThread(DoubleJumpApprovedPacket::handle)
            .add();
        CHANNEL.messageBuilder(ExcavationConfigSyncPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(ExcavationConfigSyncPacket::encode)
            .decoder(ExcavationConfigSyncPacket::decode)
            .consumerMainThread(ExcavationConfigSyncPacket::handle)
            .add();
        CHANNEL.messageBuilder(MiningModeSyncPacket.class, 4, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(MiningModeSyncPacket::encode)
            .decoder(MiningModeSyncPacket::decode)
            .consumerMainThread(MiningModeSyncPacket::handle)
            .add();
        CHANNEL.messageBuilder(VoidFloorConfigSyncPacket.class, 5, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(VoidFloorConfigSyncPacket::encode)
            .decoder(VoidFloorConfigSyncPacket::decode)
            .consumerMainThread(VoidFloorConfigSyncPacket::handle)
            .add();
    }

    public static void sendExcavationConfig(ServerPlayer player) {
        CycleMiningModePacket.validateConfiguration();
        CHANNEL.send(
            PacketDistributor.PLAYER.with(() -> player),
            new ExcavationConfigSyncPacket(ExcavationConfigSnapshot.fromServerConfig()));
        sendVoidFloorConfig(player);
    }

    public static void sendVoidFloorConfig(ServerPlayer player) {
        CHANNEL.send(
            PacketDistributor.PLAYER.with(() -> player),
            new VoidFloorConfigSyncPacket(VoidFloorConfigSnapshot.fromServerConfig()));
    }

    public static void sendMiningMode(ServerPlayer player, int slot, ExcavationTool tool, MiningMode mode) {
        CHANNEL.send(
            PacketDistributor.PLAYER.with(() -> player),
            new MiningModeSyncPacket(slot, tool, mode));
    }
}
