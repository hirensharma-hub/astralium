package com.hirensharma.astralium;

import com.hirensharma.astralium.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class AstralMomentumHandler {
    public enum Chain { AXE_MINING, AXE_COMBAT, SWORD_COMBAT }
    private static final UUID AXE_SPEED_UUID = UUID.fromString("77af5271-d37d-4f1d-a528-9ce8cc21b3cd");
    private static final Map<UUID, PlayerMomentum> MOMENTUM = new HashMap<>();
    private static final Set<UUID> PENDING_SWORD_ADVANCES = new HashSet<>();

    private AstralMomentumHandler() {}

    public static int level(ServerPlayer player, Chain chain) {
        if (!AstraliumConfig.MOMENTUM_ENABLED.get()) return 0;
        return state(player).chain(chain).level;
    }

    public static float bonusMultiplier(ServerPlayer player, Chain chain) {
        if (!AstraliumConfig.MOMENTUM_ENABLED.get()) return 1.0F;
        return 1.0F + level(player, chain) * AstraliumConfig.MOMENTUM_PERCENT_PER_LEVEL.get() / 100.0F;
    }

    public static void advance(ServerPlayer player, Chain chain) {
        if (!AstraliumConfig.MOMENTUM_ENABLED.get()) return;
        ChainState state = state(player).chain(chain);
        int max = AstraliumConfig.MOMENTUM_MAX_LEVEL.get();
        int old = state.level;
        state.level = Math.min(max, state.level + 1);
        state.ticksLeft = AstraliumConfig.MOMENTUM_RESET_TICKS.get();
        if (chain == Chain.AXE_COMBAT) applyAxeSpeed(player, state.level);
        if (state.level != old) showLevel(player, chain, state.level);
        if (state.level == 5 && old != 5) {
            ModCriteriaTriggers.PERFECT_RHYTHM.trigger(player);
            if (AstraliumConfig.MOMENTUM_LEVEL_FIVE_EFFECT.get()) {
                ServerLevel level = player.serverLevel();
                level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.55F, 1.35F);
            }
        }
    }

    public static void queueSwordAdvance(ServerPlayer player) {
        if (AstraliumConfig.MOMENTUM_ENABLED.get()) PENDING_SWORD_ADVANCES.add(player.getUUID());
    }

    public static void tick(ServerPlayer player) {
        if (!AstraliumConfig.MOMENTUM_ENABLED.get()) {
            clear(player);
            return;
        }
        if (PENDING_SWORD_ADVANCES.remove(player.getUUID())) advance(player, Chain.SWORD_COMBAT);
        PlayerMomentum state = state(player);
        for (Chain chain : Chain.values()) {
            ChainState chainState = state.chain(chain);
            if (chainState.ticksLeft > 0 && --chainState.ticksLeft <= 0) {
                chainState.level = 0;
                if (chain == Chain.AXE_COMBAT) removeAxeSpeed(player);
            }
        }
        ItemStack held = player.getMainHandItem();
        ChainState axeCombat = state.chain(Chain.AXE_COMBAT);
        if (held.is(ModItems.ASTRALIUM_AXE.get()) && axeCombat.level > 0) {
            applyAxeSpeed(player, axeCombat.level);
        } else {
            removeAxeSpeed(player);
        }
    }

    public static void clear(ServerPlayer player) {
        MOMENTUM.remove(player.getUUID());
        PENDING_SWORD_ADVANCES.remove(player.getUUID());
        removeAxeSpeed(player);
    }

    public static void clearAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            removeAxeSpeed(player);
        }
        MOMENTUM.clear();
        PENDING_SWORD_ADVANCES.clear();
    }

    private static PlayerMomentum state(ServerPlayer player) {
        return MOMENTUM.computeIfAbsent(player.getUUID(), id -> new PlayerMomentum());
    }

    private static void applyAxeSpeed(ServerPlayer player, int level) {
        AttributeInstance attribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attribute == null) return;
        double amount = level * AstraliumConfig.MOMENTUM_PERCENT_PER_LEVEL.get() / 100.0D;
        AttributeModifier existing = attribute.getModifier(AXE_SPEED_UUID);
        if (existing != null && Double.compare(existing.getAmount(), amount) == 0) return;
        attribute.removeModifier(AXE_SPEED_UUID);
        if (amount > 0.0D) attribute.addTransientModifier(new AttributeModifier(AXE_SPEED_UUID, "Astralium axe combat momentum", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void removeAxeSpeed(ServerPlayer player) {
        AttributeInstance attribute = player.getAttribute(Attributes.ATTACK_SPEED);
        if (attribute != null) attribute.removeModifier(AXE_SPEED_UUID);
    }

    private static void showLevel(ServerPlayer player, Chain chain, int level) {
        if (!AstraliumConfig.MOMENTUM_MESSAGES.get()) return;
        int percent = level * AstraliumConfig.MOMENTUM_PERCENT_PER_LEVEL.get();
        String key = switch (chain) {
            case AXE_MINING -> "message.astralium.momentum.axe_mining";
            case AXE_COMBAT -> "message.astralium.momentum.axe_combat";
            case SWORD_COMBAT -> "message.astralium.momentum.sword";
        };
        player.displayClientMessage(Component.translatable(key, roman(level), percent).withStyle(ChatFormatting.LIGHT_PURPLE), true);
    }

    private static String roman(int level) {
        return switch (level) { case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V"; default -> "0"; };
    }

    private static final class PlayerMomentum {
        private final ChainState axeMining = new ChainState();
        private final ChainState axeCombat = new ChainState();
        private final ChainState swordCombat = new ChainState();
        ChainState chain(Chain chain) {
            return switch (chain) {
                case AXE_MINING -> axeMining;
                case AXE_COMBAT -> axeCombat;
                case SWORD_COMBAT -> swordCombat;
            };
        }
    }

    private static final class ChainState {
        int level;
        int ticksLeft;
    }
}
