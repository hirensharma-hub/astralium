package com.hirensharma.astralium.network;

import com.hirensharma.astralium.client.AstraliumClient;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class DoubleJumpApprovedPacket {
    public void encode(FriendlyByteBuf buffer) {
    }

    public static DoubleJumpApprovedPacket decode(FriendlyByteBuf buffer) {
        return new DoubleJumpApprovedPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> AstraliumClient::applyApprovedDoubleJump));
        context.setPacketHandled(true);
    }
}
