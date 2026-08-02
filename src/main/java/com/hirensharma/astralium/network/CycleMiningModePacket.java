package com.hirensharma.astralium.network;

import com.hirensharma.astralium.MiningMode;
import com.hirensharma.astralium.ExcavationTool;
import com.hirensharma.astralium.AstraliumConfig;
import com.hirensharma.astralium.AstraliumMod;
import com.hirensharma.astralium.item.AstraliumPickaxeItem;
import com.hirensharma.astralium.registry.ModNetworking;
import java.util.EnumSet;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class CycleMiningModePacket {
    private static final int CYCLE_COOLDOWN_TICKS = 3;
    private static final java.util.Map<java.util.UUID, EnumSet<ExcavationTool>> WARNED_DISABLED = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, Long> LAST_CYCLE_TICKS = new java.util.HashMap<>();
    private static final EnumSet<ExcavationTool> LOGGED_DISABLED = EnumSet.noneOf(ExcavationTool.class);
    private final int slot;
    private final int toolOrdinal;

    public CycleMiningModePacket(int slot, ExcavationTool tool) {
        this.slot = slot;
        this.toolOrdinal = tool.ordinal();
    }

    private CycleMiningModePacket(int slot, int toolOrdinal) {
        this.slot = slot;
        this.toolOrdinal = toolOrdinal;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeVarInt(slot);
        buffer.writeVarInt(toolOrdinal);
    }

    public static CycleMiningModePacket decode(FriendlyByteBuf buffer) {
        return new CycleMiningModePacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (slot < 0 || slot >= 9 || toolOrdinal < 0 || toolOrdinal >= ExcavationTool.values().length) return;
            long gameTime = player.serverLevel().getGameTime();
            ExcavationTool expectedTool = ExcavationTool.values()[toolOrdinal];
            ItemStack stack = player.getInventory().getItem(slot);
            ExcavationTool tool = ExcavationTool.from(stack);
            if (tool != expectedTool) return;
            MiningMode currentMode = AstraliumPickaxeItem.getMode(stack);
            Long previousCycle = LAST_CYCLE_TICKS.get(player.getUUID());
            if (previousCycle != null && gameTime - previousCycle < CYCLE_COOLDOWN_TICKS) {
                ModNetworking.sendMiningMode(player, slot, tool, currentMode);
                return;
            }
            LAST_CYCLE_TICKS.put(player.getUUID(), gameTime);
            if (!AstraliumConfig.EXCAVATION_ENABLED.get()) return;
            if (!tool.isEnabled()) return;
            if (!tool.hasAnyEnabledMode()) {
                AstraliumPickaxeItem.setMode(stack, MiningMode.NORMAL);
                ModNetworking.sendMiningMode(player, slot, tool, MiningMode.NORMAL);
                if (LOGGED_DISABLED.add(tool)) {
                    AstraliumMod.LOGGER.warn(
                        "All Shifting Excavation modes are disabled for the Astralium {}; forcing Normal mode.",
                        tool.translationKey());
                }
                EnumSet<ExcavationTool> warned = WARNED_DISABLED.computeIfAbsent(
                    player.getUUID(), ignored -> EnumSet.noneOf(ExcavationTool.class));
                if (warned.add(tool)) {
                    player.displayClientMessage(Component.translatable("message.astralium.excavation_no_modes"), true);
                }
                return;
            }
            MiningMode mode = AstraliumPickaxeItem.cycleMode(stack, tool);
            ModNetworking.sendMiningMode(player, slot, tool, mode);
            player.displayClientMessage(Component.translatable("message.astralium.excavation_mode", Component.translatable("item.astralium.astralium_" + tool.translationKey()), mode.displayName()), true);
            player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 0.45F, 1.45F);
        });
        context.setPacketHandled(true);
    }

    public static void clear(ServerPlayer player) {
        WARNED_DISABLED.remove(player.getUUID());
        LAST_CYCLE_TICKS.remove(player.getUUID());
    }

    public static void clearAll() {
        WARNED_DISABLED.clear();
        LAST_CYCLE_TICKS.clear();
        LOGGED_DISABLED.clear();
    }

    public static void resetWarnings() {
        WARNED_DISABLED.clear();
        LAST_CYCLE_TICKS.clear();
        LOGGED_DISABLED.clear();
    }

    public static void validateConfiguration() {
        for (ExcavationTool tool : ExcavationTool.values()) {
            if (!tool.hasAnyEnabledMode() && LOGGED_DISABLED.add(tool)) {
                AstraliumMod.LOGGER.warn(
                    "All Shifting Excavation modes are disabled for the Astralium {}; Normal mode will be forced.",
                    tool.translationKey());
            }
        }
    }

}
