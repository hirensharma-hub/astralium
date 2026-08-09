package com.hirensharma.astralium.mixin;

import com.hirensharma.astralium.VoidFloorHandler;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void astralium$applyVoidFloor(CallbackInfo callbackInfo) {
        ItemEntity item = (ItemEntity) (Object) this;
        VoidFloorHandler.tickItem(item);
    }
}
