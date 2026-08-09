package com.hirensharma.astralium.mixin;

import com.hirensharma.astralium.AstralFarmlandSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BushBlock.class)
public abstract class StemBlockMixin {
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void astralium$allowAstralFarmland(BlockState state, LevelReader level, BlockPos position, CallbackInfoReturnable<Boolean> callbackInfo) {
        if (state.getBlock() instanceof StemBlock
            && AstralFarmlandSupport.canSurviveOnAstralFarmland(state, level, position)
            && (level.getRawBrightness(position, 0) >= 8 || level.canSeeSky(position))) {
            callbackInfo.setReturnValue(true);
        }
    }
}
