package com.hirensharma.astralium;

import com.hirensharma.astralium.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public final class AstraletPreservationHandler {
    private static final String SNAPSHOT_TAG = "AstraliumAstraletInventory";

    private AstraletPreservationHandler() {}

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
            .map(handler -> handler.isEquipped(ModItems.ASTRALET_OF_PRESERVATION.get()))
            .orElse(false);
    }

    public static void captureOnDeath(ServerPlayer player) {
        if (!isEquipped(player)) return;
        ListTag inventory = new ListTag();
        player.getInventory().save(inventory);
        player.getPersistentData().put(SNAPSHOT_TAG, inventory);
    }

    public static boolean restoreOnClone(ServerPlayer original, ServerPlayer clone) {
        CompoundTag data = original.getPersistentData();
        if (!data.contains(SNAPSHOT_TAG)) return false;
        ListTag inventory = data.getList(SNAPSHOT_TAG, 10);
        clone.getInventory().clearContent();
        clone.getInventory().load(inventory);
        data.remove(SNAPSHOT_TAG);
        return true;
    }

    public static boolean shouldSuppressDrops(Player player) {
        return player.getPersistentData().contains(SNAPSHOT_TAG);
    }
}
