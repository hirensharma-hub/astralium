package com.hirensharma.astralium;

import com.hirensharma.astralium.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class ArmorAbilityHandler {
    private static final String DOUBLE_JUMP_USED_TAG = "AstraliumDoubleJumpUsed";
    private static final double NORMAL_JUMP_VELOCITY = 0.46D;
    private static final int REQUEST_COOLDOWN_TICKS = 2;
    private static final Map<UUID, Long> LAST_REQUEST_TICKS = new HashMap<>();
    private static final Set<UUID> FULL_SET_ACTIVE = new HashSet<>();

    private ArmorAbilityHandler() {}

    public static void tick(ServerPlayer player) {
        boolean fullSet = hasFullSet(player);
        if (fullSet) {
            if (FULL_SET_ACTIVE.add(player.getUUID())) ModCriteriaTriggers.FULL_ARMOUR.trigger(player);
        } else {
            FULL_SET_ACTIVE.remove(player.getUUID());
        }
        if (player.onGround() || VoidFloorHandler.isSupported(player)) {
            player.getPersistentData().remove(DOUBLE_JUMP_USED_TAG);
        }
    }

    public static boolean tryDoubleJump(ServerPlayer player) {
        if (!hasFullSet(player)
            || player.onGround()
            || player.isPassenger()
            || player.isFallFlying()
            || player.isInWater()
            || player.isInLava()
            || player.onClimbable()
            || player.getAbilities().flying
            || player.getPersistentData().getBoolean(DOUBLE_JUMP_USED_TAG)) {
            return false;
        }
        if (isRateLimited(player)) return false;

        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, NORMAL_JUMP_VELOCITY, motion.z);
        player.fallDistance = 0.0F;
        player.getPersistentData().putBoolean(DOUBLE_JUMP_USED_TAG, true);
        return true;
    }

    public static double normalJumpVelocity() {
        return NORMAL_JUMP_VELOCITY;
    }

    public static void clear(ServerPlayer player) {
        LAST_REQUEST_TICKS.remove(player.getUUID());
        FULL_SET_ACTIVE.remove(player.getUUID());
        player.getPersistentData().remove(DOUBLE_JUMP_USED_TAG);
    }

    public static void clearAll() {
        LAST_REQUEST_TICKS.clear();
        FULL_SET_ACTIVE.clear();
    }

    public static boolean hasFullSet(Player player) {
        return has(player.getInventory().armor.get(3), ModItems.ASTRALIUM_HELMET.get())
            && has(player.getInventory().armor.get(2), ModItems.ASTRALIUM_CHESTPLATE.get())
            && has(player.getInventory().armor.get(1), ModItems.ASTRALIUM_LEGGINGS.get())
            && has(player.getInventory().armor.get(0), ModItems.ASTRALIUM_BOOTS.get());
    }

    private static boolean has(ItemStack stack, net.minecraft.world.item.Item item) {
        return !stack.isEmpty() && stack.is(item);
    }

    private static boolean isRateLimited(ServerPlayer player) {
        long now = player.serverLevel().getGameTime();
        Long previous = LAST_REQUEST_TICKS.get(player.getUUID());
        if (previous != null && now - previous < REQUEST_COOLDOWN_TICKS) return true;
        LAST_REQUEST_TICKS.put(player.getUUID(), now);
        return false;
    }
}
