package com.hirensharma.astralium;

import com.hirensharma.astralium.registry.ModBlocks;
import com.hirensharma.astralium.block.VoidFloorBlock;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class VoidFloorHandler {
    private static final int TILE_RADIUS = 3;
    private static final String SUPPORTED_LOGOUT_TAG = "AstraliumSupportedOnLogout";
    private static final String SUPPORTED_LOGOUT_DIMENSION_TAG = "AstraliumSupportedLogoutDimension";
    private static final String SUPPORTED_LOGOUT_FLOOR_Y_TAG = "AstraliumSupportedLogoutFloorY";
    private static final String ITEM_ACTIVE_TAG = "AstraliumItemVoidFloorActive";
    private static final String ITEM_PREVIOUS_NO_GRAVITY_TAG = "AstraliumItemVoidFloorPreviousNoGravity";
    private static final TagKey<Item> VOID_FLOOR_ITEMS = RuntimeMappings.tagKey(
        RuntimeMappings.registryKey("ITEM", "f_256913_"),
        new ResourceLocation(AstraliumMod.MOD_ID, "void_protected_items"));
    private static final Map<UUID, ManagedTiles> PLAYER_TILES = new HashMap<>();
    private static final Map<ResourceLocation, Map<BlockPos, Integer>> TILE_OWNERS = new HashMap<>();
    private static final Set<UUID> LOGIN_RESCUE_PENDING = new HashSet<>();

    private VoidFloorHandler() {}

    public static void tickPlayer(ServerPlayer player) {
        if (!canUseFloor(player)) {
            clearPlayerTiles(player);
            return;
        }
        updatePlayerTiles(player);
    }

    public static void armLoginRestore(ServerPlayer player) {
        LOGIN_RESCUE_PENDING.add(RuntimeMappings.uuid(player));
    }

    public static boolean rescueLoginPlayer(ServerPlayer player) {
        if (!LOGIN_RESCUE_PENDING.remove(RuntimeMappings.uuid(player))) return false;
        if (!canUseFloor(player)
            || Math.abs(player.getBoundingBox().minY - floorY(RuntimeMappings.level(player))) > reconnectTolerance(RuntimeMappings.level(player))) return false;
        updatePlayerTiles(player);
        alignPlayerToFloor(player);
        return true;
    }

    public static void tickItem(ItemEntity item) {
        if (RuntimeMappings.isRemoved(item)
            || !item.getItem().is(VOID_FLOOR_ITEMS)
            || !isEnabledFor(RuntimeMappings.level(item))
            || !protectAstraliumItems(RuntimeMappings.level(item))) {
            releaseItemFloor(item);
            return;
        }

        double floorY = floorY(RuntimeMappings.level(item));
        double bottomY = item.getBoundingBox().minY;
        if (bottomY > floorY + 0.05D) {
            releaseItemFloor(item);
            return;
        }
        if (bottomY < floorY) item.setPos(item.getX(), item.getY() + floorY - bottomY, item.getZ());
        if (!item.getPersistentData().getBoolean(ITEM_ACTIVE_TAG)) {
            item.getPersistentData().putBoolean(ITEM_PREVIOUS_NO_GRAVITY_TAG, item.isNoGravity());
            item.getPersistentData().putBoolean(ITEM_ACTIVE_TAG, true);
        }
        item.setNoGravity(true);
        item.setOnGround(true);
        Vec3 motion = item.getDeltaMovement();
        item.setDeltaMovement(motion.x * 0.6D, 0.0D, motion.z * 0.6D);
        item.fallDistance = 0.0F;
    }

    public static void handleKnockback(Player player) {
        if (player instanceof ServerPlayer serverPlayer && canUseFloor(serverPlayer)) {
            updatePlayerTiles(serverPlayer);
        }
    }

    public static boolean isSupported(Player player) {
        if (!canUseFloor(player)) return false;
        if (Math.abs(player.getBoundingBox().minY - floorY(RuntimeMappings.level(player))) > 0.05D) return false;
        BlockPos floorTile = BlockPos.containing(player.getX(), floorBlockY(RuntimeMappings.level(player)), player.getZ());
        return RuntimeMappings.level(player).getBlockState(floorTile).is(ModBlocks.VOID_FLOOR.get());
    }

    public static BlockPos floorPlacementTarget(Player player) {
        BlockHitResult hit = floorPlacementHit(player);
        return hit == null ? null : hit.getBlockPos();
    }

    public static BlockHitResult floorPlacementHit(Player player) {
        if (!canUseFloor(player)) return null;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        if (look.y >= -1.0E-5D) return null;
        double distance = (floorY(RuntimeMappings.level(player)) - eye.y) / look.y;
        if (distance < 0.0D || distance > player.getBlockReach()) return null;
        Vec3 hitLocation = eye.add(look.scale(distance));
        BlockPos target = new BlockPos(
            net.minecraft.util.Mth.floor(hitLocation.x),
            floorBlockY(RuntimeMappings.level(player)),
            net.minecraft.util.Mth.floor(hitLocation.z));
        return new BlockHitResult(hitLocation, Direction.UP, target, false);
    }

    public static boolean blocksPlacement(Player player, BlockPos target) {
        return new AABB(target).intersects(player.getBoundingBox());
    }

    public static void handleTeleport(Player player, Vec3 target) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        clearPlayerTiles(serverPlayer);
        if (canUseFloor(serverPlayer)) {
            updatePlayerTiles(
                serverPlayer,
                net.minecraft.util.Mth.floor(target.x),
                net.minecraft.util.Mth.floor(target.z));
        }
    }

    public static void prepareForDisconnect(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (isSupported(player) || Math.abs(player.getY() - floorY(RuntimeMappings.level(player))) <= reconnectTolerance(RuntimeMappings.level(player))) {
            data.putBoolean(SUPPORTED_LOGOUT_TAG, true);
            data.putString(SUPPORTED_LOGOUT_DIMENSION_TAG, RuntimeMappings.dimension(RuntimeMappings.level(player)).location().toString());
            data.putDouble(SUPPORTED_LOGOUT_FLOOR_Y_TAG, floorY(RuntimeMappings.level(player)));
        } else {
            clearSupportedLogoutMarker(player);
        }
        clearPlayerTiles(player);
    }

    public static boolean restoreSupportedLogout(ServerPlayer player) {
        return restoreSupportedLogout(player, false);
    }

    public static boolean restoreSupportedLogout(ServerPlayer player, boolean consumeMarker) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(SUPPORTED_LOGOUT_TAG)) return false;
        boolean sameDimension = RuntimeMappings.dimension(RuntimeMappings.level(player)).location().toString()
            .equals(data.getString(SUPPORTED_LOGOUT_DIMENSION_TAG));
        boolean matchingFloor = Math.abs(data.getDouble(SUPPORTED_LOGOUT_FLOOR_Y_TAG) - floorY(RuntimeMappings.level(player))) < 0.01D;
        if (!sameDimension || !matchingFloor || !canUseFloor(player)) {
            if (consumeMarker) clearSupportedLogoutMarker(player);
            return false;
        }
        updatePlayerTiles(player);
        if (LOGIN_RESCUE_PENDING.remove(RuntimeMappings.uuid(player))) alignPlayerToFloor(player);
        if (consumeMarker) clearSupportedLogoutMarker(player);
        return true;
    }

    public static void clearSupportedLogoutMarker(Player player) {
        CompoundTag data = player.getPersistentData();
        data.remove(SUPPORTED_LOGOUT_TAG);
        data.remove(SUPPORTED_LOGOUT_DIMENSION_TAG);
        data.remove(SUPPORTED_LOGOUT_FLOOR_Y_TAG);
    }

    public static void clearPlayer(Player player) {
        clearPlayerTiles(player);
    }

    public static void clearAll(MinecraftServer server) {
        for (Map.Entry<ResourceLocation, Map<BlockPos, Integer>> dimensionEntry : new HashMap<>(TILE_OWNERS).entrySet()) {
            ServerLevel level = server.getLevel(ResourceKey.create(RuntimeMappings.registryKey("DIMENSION", "f_256787_"), dimensionEntry.getKey()));
            if (level == null) continue;
            for (BlockPos pos : new HashSet<>(dimensionEntry.getValue().keySet())) {
                removeOwnedBlock(level, pos);
            }
        }
        PLAYER_TILES.clear();
        TILE_OWNERS.clear();
        LOGIN_RESCUE_PENDING.clear();
    }

    public static void clearDimension(ServerLevel level) {
        ResourceLocation dimension = RuntimeMappings.dimension(level).location();
        Map<BlockPos, Integer> owners = TILE_OWNERS.remove(dimension);
        if (owners != null) {
            for (BlockPos pos : owners.keySet()) {
                removeOwnedBlock(level, pos);
            }
        }
        PLAYER_TILES.entrySet().removeIf(entry -> entry.getValue().dimension.equals(dimension));
    }

    public static void onConfigReload(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!canUseFloor(player)) clearPlayerTiles(player);
            else updatePlayerTiles(player);
        }
    }

    public static boolean canUseFloor(Player player) {
        return ArmorAbilityHandler.hasFullSet(player)
            && isEnabledFor(RuntimeMappings.level(player))
            && !player.isSpectator()
            && !player.isPassenger()
            && !player.isFallFlying()
            && !player.getAbilities().flying;
    }

    public static boolean isEnabledFor(Level level) {
        if (level.isClientSide) return VoidFloorConfigSnapshot.client().enabledFor(level);
        if (!AstraliumConfig.VOID_FLOOR_ENABLED.get()) return false;
        String dimension = RuntimeMappings.dimension(level).location().toString();
        for (String configured : AstraliumConfig.VOID_FLOOR_DIMENSIONS.get()) {
            if (ResourceLocation.tryParse(configured) != null && dimension.equals(configured)) return true;
        }
        return false;
    }

    public static int floorY(Level level) {
        return floorBlockY(level);
    }

    public static boolean isOwned(ServerLevel level, BlockPos pos) {
        Map<BlockPos, Integer> owners = TILE_OWNERS.get(RuntimeMappings.dimension(level).location());
        return owners != null && owners.getOrDefault(pos, 0) > 0;
    }

    public static boolean isOwnedBy(Player player, BlockPos pos) {
        ManagedTiles tiles = PLAYER_TILES.get(RuntimeMappings.uuid(player));
        return tiles != null
            && tiles.dimension.equals(RuntimeMappings.dimension(RuntimeMappings.level(player)).location())
            && tiles.positions.contains(pos);
    }

    public static void removeIfOrphaned(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.is(ModBlocks.VOID_FLOOR.get()) && !isOwned(level, pos)) {
            restoreUnderlyingFluid(level, pos, state);
        }
    }

    private static void updatePlayerTiles(ServerPlayer player) {
        updatePlayerTiles(
            player,
            net.minecraft.util.Mth.floor(player.getX()),
            net.minecraft.util.Mth.floor(player.getZ()));
    }

    private static void updatePlayerTiles(ServerPlayer player, int centerX, int centerZ) {
        ServerLevel level = player.serverLevel();
        ResourceLocation dimension = RuntimeMappings.dimension(level).location();
        int blockY = floorBlockY(level);
        Set<BlockPos> desired = new HashSet<>();
        for (int x = centerX - TILE_RADIUS; x <= centerX + TILE_RADIUS; x++) {
            for (int z = centerZ - TILE_RADIUS; z <= centerZ + TILE_RADIUS; z++) {
                BlockPos pos = new BlockPos(x, blockY, z);
                BlockState state = level.getBlockState(pos);
                if (level.hasChunkAt(pos)
                    && (state.isAir()
                        || state.is(ModBlocks.VOID_FLOOR.get())
                        || state.getFluidState().is(Fluids.WATER))) {
                    desired.add(pos);
                }
            }
        }

        ManagedTiles previous = PLAYER_TILES.get(RuntimeMappings.uuid(player));
        if (previous != null && !previous.dimension.equals(dimension)) {
            ServerLevel previousLevel = getManagedLevel(player, previous.dimension);
            if (previousLevel != null) removeTiles(previousLevel, previous.dimension, previous.positions);
        }
        Set<BlockPos> previousPositions = previous != null && previous.dimension.equals(dimension)
            ? previous.positions : Set.of();
        Set<BlockPos> removed = new HashSet<>(previousPositions);
        removed.removeAll(desired);
        removeTiles(level, dimension, removed);

        Set<BlockPos> added = new HashSet<>(desired);
        added.removeAll(previousPositions);
        addTiles(level, dimension, added);
        PLAYER_TILES.put(RuntimeMappings.uuid(player), new ManagedTiles(dimension, desired));
    }

    private static void addTiles(ServerLevel level, ResourceLocation dimension, Set<BlockPos> positions) {
        Map<BlockPos, Integer> owners = TILE_OWNERS.computeIfAbsent(dimension, ignored -> new HashMap<>());
        for (BlockPos pos : positions) {
            int count = owners.getOrDefault(pos, 0);
            BlockState state = level.getBlockState(pos);
            if (count == 0 && state.isAir()) {
                level.setBlock(pos, ModBlocks.VOID_FLOOR.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            } else if (count == 0 && state.getFluidState().is(Fluids.WATER)) {
                level.setBlock(
                    pos,
                    RuntimeMappings.setValue(ModBlocks.VOID_FLOOR.get().defaultBlockState(), VoidFloorBlock.WATERLOGGED, true),
                    Block.UPDATE_CLIENTS);
            }
            if (level.getBlockState(pos).is(ModBlocks.VOID_FLOOR.get())) owners.put(pos, count + 1);
        }
    }

    private static void removeTiles(ServerLevel level, ResourceLocation dimension, Set<BlockPos> positions) {
        Map<BlockPos, Integer> owners = TILE_OWNERS.get(dimension);
        if (owners == null) return;
        for (BlockPos pos : positions) {
            int remaining = owners.getOrDefault(pos, 0) - 1;
            if (remaining <= 0) {
                owners.remove(pos);
                BlockState state = level.getBlockState(pos);
                if (state.is(ModBlocks.VOID_FLOOR.get())) {
                    restoreUnderlyingFluid(level, pos, state);
                }
            } else {
                owners.put(pos, remaining);
            }
        }
        if (owners.isEmpty()) TILE_OWNERS.remove(dimension);
    }

    private static void clearPlayerTiles(Player player) {
        ManagedTiles tiles = PLAYER_TILES.remove(RuntimeMappings.uuid(player));
        if (tiles == null || !(player instanceof ServerPlayer serverPlayer)) return;
        ServerLevel managedLevel = getManagedLevel(serverPlayer, tiles.dimension);
        if (managedLevel != null) removeTiles(managedLevel, tiles.dimension, tiles.positions);
    }

    private static ServerLevel getManagedLevel(ServerPlayer player, ResourceLocation dimension) {
        return player.server.getLevel(ResourceKey.create(RuntimeMappings.registryKey("DIMENSION", "f_256787_"), dimension));
    }

    private static void releaseItemFloor(ItemEntity item) {
        if (!item.getPersistentData().getBoolean(ITEM_ACTIVE_TAG)) return;
        item.setNoGravity(item.getPersistentData().getBoolean(ITEM_PREVIOUS_NO_GRAVITY_TAG));
        item.getPersistentData().remove(ITEM_ACTIVE_TAG);
        item.getPersistentData().remove(ITEM_PREVIOUS_NO_GRAVITY_TAG);
    }

    private static void alignPlayerToFloor(ServerPlayer player) {
        double surfaceY = floorY(RuntimeMappings.level(player));
        if (player.getBoundingBox().minY >= surfaceY) return;
        Vec3 motion = player.getDeltaMovement();
        player.setPos(player.getX(), player.getY() + surfaceY - player.getBoundingBox().minY, player.getZ());
        player.setDeltaMovement(motion.x, Math.max(0.0D, motion.y), motion.z);
        player.setOnGround(true);
        player.fallDistance = 0.0F;
    }

    private static void removeOwnedBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModBlocks.VOID_FLOOR.get())) restoreUnderlyingFluid(level, pos, state);
    }

    private static void restoreUnderlyingFluid(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getValue(VoidFloorBlock.WATERLOGGED)) {
            level.setBlock(pos, Fluids.WATER.defaultFluidState().createLegacyBlock(), Block.UPDATE_CLIENTS);
        } else {
            level.removeBlock(pos, false);
        }
    }

    private static boolean protectAstraliumItems(Level level) {
        return level.isClientSide
            ? VoidFloorConfigSnapshot.client().protectAstraliumItems()
            : AstraliumConfig.VOID_FLOOR_PROTECT_ASTRALIUM_ITEMS.get();
    }

    private static double reconnectTolerance(Level level) {
        return level.isClientSide
            ? VoidFloorConfigSnapshot.client().resetMargin()
            : AstraliumConfig.VOID_FLOOR_RESET_MARGIN.get();
    }

    private static int floorBlockY(Level level) {
        int offset = level.isClientSide
            ? VoidFloorConfigSnapshot.client().floorOffset()
            : AstraliumConfig.VOID_FLOOR_OFFSET.get();
        return net.minecraft.util.Mth.clamp(
            level.getMinBuildHeight() + offset,
            level.getMinBuildHeight(),
            level.getMaxBuildHeight() - 1);
    }

    private record ManagedTiles(ResourceLocation dimension, Set<BlockPos> positions) {}
}
