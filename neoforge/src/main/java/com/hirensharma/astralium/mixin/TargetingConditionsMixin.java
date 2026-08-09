package com.hirensharma.astralium.mixin;

import com.hirensharma.astralium.VoidFloorHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TargetingConditions.class)
public abstract class TargetingConditionsMixin {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void astralium$ignoreSupportedVoidFloorPlayer(
        LivingEntity source,
        LivingEntity target,
        CallbackInfoReturnable<Boolean> callbackInfo) {
        if (source instanceof EnderDragon
            && target instanceof Player player
            && VoidFloorHandler.isSupported(player)) {
            callbackInfo.setReturnValue(false);
        }
    }
}
