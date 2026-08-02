package com.hirensharma.astralium;

import com.hirensharma.astralium.item.AstraliumPickaxeItem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;

public final class PickaxeAreaMiningHandler {
    private static final int TARGET_CONTEXT_LIFETIME_TICKS = 20;
    private static final Map<UUID, TargetContext> CLIENT_TARGETS = new HashMap<>();
    private static final Map<UUID, TargetContext> SERVER_TARGETS = new HashMap<>();
    private static final Set<UUID> PROCESSING = new HashSet<>();

    private PickaxeAreaMiningHandler() {}

    public static void rememberTarget(Player player, BlockPos pos, Direction face) {
        pruneExpiredTarget(player);
        targetMap(player).put(player.getUUID(), new TargetContext(pos.immutable(), face, player.getDirection(), player.tickCount));
    }

    public static void afterCentralBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!AstraliumConfig.EXCAVATION_ENABLED.get()) return;
        if (PROCESSING.contains(player.getUUID())) return;
        ItemStack tool = player.getMainHandItem();
        ExcavationTool excavationTool = ExcavationTool.from(tool);
        if (excavationTool == null || !excavationTool.isEnabled()) return;
        if (player.isCreative() && !AstraliumConfig.CREATIVE_AREA_MINING.get()) return;
        if (!canMineExtra(player, event.getPos(), tool, excavationTool)) return;
        MiningMode mode = AstraliumPickaxeItem.getMode(tool, excavationTool);
        if (mode == MiningMode.NORMAL) return;
        TargetContext context = getCurrentTarget(player);
        boolean matchingContext = context != null && context.center.equals(event.getPos());
        Direction face = matchingContext && context.face != null ? context.face : fallbackFace(player);
        Direction facing = matchingContext ? context.facing : player.getDirection();
        PROCESSING.add(player.getUUID());
        boolean brokeExtra = false;
        try {
            for (BlockPos pos : MiningPatternCalculator.calculate(event.getPos(), face, facing, mode)) {
                if (pos.equals(event.getPos())) continue;
                if (!canMineExtra(player, pos, tool, excavationTool)) continue;
                boolean destroyed = player.gameMode.destroyBlock(pos);
                brokeExtra |= destroyed;
                if (!destroyed || tool.isEmpty()) break;
            }
        } finally {
            PROCESSING.remove(player.getUUID());
            SERVER_TARGETS.remove(player.getUUID());
        }
        if (brokeExtra && mode == MiningMode.EXPANDED) ModCriteriaTriggers.SHIFTING_EXCAVATION.trigger(player);
    }

    public static void adjustBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        ExcavationTool excavationTool = ExcavationTool.from(tool);
        if (excavationTool == null || !isEnabledFor(player, excavationTool) || player.isCreative()) return;
        MiningMode mode = player.level().isClientSide
            ? AstraliumPickaxeItem.getPreviewMode(tool, excavationTool)
            : AstraliumPickaxeItem.getMode(tool, excavationTool);
        if (mode == MiningMode.NORMAL) return;

        BlockPos center = event.getPosition().orElse(null);
        if (center == null) return;
        BlockState centerState = event.getState();
        if (!canMineExtra(player, center, tool, excavationTool)) return;

        TargetContext context = getCurrentTarget(player);
        boolean matchingContext = context != null && context.center.equals(center);
        Direction face = matchingContext && context.face != null ? context.face : fallbackFace(player);
        Direction facing = matchingContext ? context.facing : player.getDirection();
        float longestDestroySpeed = centerState.getDestroySpeed(player.level(), center);

        for (BlockPos pos : MiningPatternCalculator.calculate(center, face, facing, mode)) {
            if (!canMineExtra(player, pos, tool, excavationTool)) continue;
            longestDestroySpeed = Math.max(longestDestroySpeed, player.level().getBlockState(pos).getDestroySpeed(player.level(), pos));
        }

        float centerDestroySpeed = centerState.getDestroySpeed(player.level(), center);
        if (longestDestroySpeed > centerDestroySpeed) {
            event.setNewSpeed(event.getNewSpeed() * centerDestroySpeed / longestDestroySpeed);
        }
    }

    public static boolean canMineExtra(Player player, BlockPos pos, ItemStack tool) {
        ExcavationTool excavationTool = ExcavationTool.from(tool);
        return excavationTool != null && canMineExtra(player, pos, tool, excavationTool);
    }

    public static boolean canMineExtra(Player player, BlockPos pos, ItemStack tool, ExcavationTool excavationTool) {
        if (!player.level().isLoaded(pos)) return false;
        BlockState state = player.level().getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(player.level(), pos) < 0.0F) return false;
        boolean protectBlockEntities = player.level().isClientSide
            ? ExcavationConfigSnapshot.client().protectBlockEntities()
            : AstraliumConfig.PROTECT_BLOCK_ENTITIES.get();
        if (protectBlockEntities && player.level().getBlockEntity(pos) != null) return false;
        if (!state.is(excavationTool.mineableTag())) return false;
        return tool.isCorrectToolForDrops(state);
    }

    public static boolean isEnabledFor(Player player, ExcavationTool tool) {
        if (player.level().isClientSide) {
            ExcavationConfigSnapshot snapshot = ExcavationConfigSnapshot.client();
            return snapshot.toolEnabled(tool) && (!player.isCreative() || snapshot.creativeAreaMining());
        }
        return AstraliumConfig.EXCAVATION_ENABLED.get()
            && tool.isEnabled()
            && (!player.isCreative() || AstraliumConfig.CREATIVE_AREA_MINING.get());
    }

    public static void clearPlayer(UUID playerId) {
        CLIENT_TARGETS.remove(playerId);
        SERVER_TARGETS.remove(playerId);
        PROCESSING.remove(playerId);
    }

    public static void clearAll() {
        CLIENT_TARGETS.clear();
        SERVER_TARGETS.clear();
        PROCESSING.clear();
    }

    private static TargetContext getCurrentTarget(Player player) {
        pruneExpiredTarget(player);
        return targetMap(player).get(player.getUUID());
    }

    private static void pruneExpiredTarget(Player player) {
        Map<UUID, TargetContext> targets = targetMap(player);
        TargetContext context = targets.get(player.getUUID());
        if (context != null && player.tickCount - context.tick > TARGET_CONTEXT_LIFETIME_TICKS) {
            targets.remove(player.getUUID());
        }
    }

    private static Map<UUID, TargetContext> targetMap(Player player) {
        return player.level().isClientSide ? CLIENT_TARGETS : SERVER_TARGETS;
    }

    private static Direction fallbackFace(Player player) {
        net.minecraft.world.phys.Vec3 look = player.getViewVector(1.0F);
        return Direction.getNearest((float) look.x, (float) look.y, (float) look.z).getOpposite();
    }

    private record TargetContext(BlockPos center, Direction face, Direction facing, int tick) {}
}
