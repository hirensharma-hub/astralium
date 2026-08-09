package com.hirensharma.astralium.compat.curios;

import com.hirensharma.astralium.item.AstraletOfPreservationItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class CuriosAstraletOfPreservationItem extends AstraletOfPreservationItem implements ICurioItem {
    public CuriosAstraletOfPreservationItem(Properties properties) {
        super(properties);
    }

    @Override
    public ICurio.DropRule getDropRule(SlotContext slotContext, DamageSource source, int level, boolean recentlyHit, ItemStack stack) {
        return ICurio.DropRule.ALWAYS_KEEP;
    }
}
