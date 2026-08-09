package com.hirensharma.astralium.compat.curios;

import com.hirensharma.astralium.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public final class CuriosAstraletBridge {
    private CuriosAstraletBridge() {}

    public static boolean isEquipped(Player player) {
        return CuriosApi.getCuriosInventory(player)
            .map(handler -> handler.findFirstCurio(ModItems.ASTRALET_OF_PRESERVATION.get()).isPresent())
            .orElse(false);
    }
}
