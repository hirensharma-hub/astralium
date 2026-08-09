package com.hirensharma.astralium;

import com.hirensharma.astralium.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import java.util.Collection;

public final class AstraletPreservationHandler {
    private static final String SNAPSHOT_TAG = "AstraliumAstraletInventory";

    private AstraletPreservationHandler() {}

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
            .map(handler -> handler.isEquipped(ModItems.ASTRALET_OF_PRESERVATION.get()))
            .orElse(false);
    }

    public static void captureOnDeath(ServerPlayer player) {
        clearSnapshot(player);
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
        clearSnapshot(original);
        return true;
    }

    public static void removeProtectedDrops(Player player, Collection<ItemEntity> drops) {
        if (!player.getPersistentData().contains(SNAPSHOT_TAG)) return;
        ListTag inventory = player.getPersistentData().getList(SNAPSHOT_TAG, 10);
        drops.removeIf(drop -> {
            ItemStack droppedStack = drop.getItem();
            for (int index = 0; index < inventory.size(); index++) {
                ItemStack savedStack = ItemStack.of(inventory.getCompound(index));
                if (ItemStack.isSameItemSameTags(savedStack, droppedStack)) return true;
            }
            return false;
        });
    }

    public static void clearSnapshot(Player player) {
        player.getPersistentData().remove(SNAPSHOT_TAG);
    }
}
