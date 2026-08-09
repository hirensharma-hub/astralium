package com.hirensharma.astralium.network;

import com.hirensharma.astralium.ArmorAbilityHandler;
import com.hirensharma.astralium.registry.ModNetworking;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class DoubleJumpPacket {
    public DoubleJumpPacket() {}

    public void encode(FriendlyByteBuf buffer) {
    }

    public static DoubleJumpPacket decode(FriendlyByteBuf buffer) {
        return new DoubleJumpPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && ArmorAbilityHandler.tryDoubleJump(player)) {
                ModNetworking.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new DoubleJumpApprovedPacket());
            }
        });
        context.setPacketHandled(true);
    }
}
