package com.hirensharma.astralium.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EndCityPieces.EndCityPiece.class)
public abstract class EndCityPieceMixin {
    @Shadow
    protected abstract ResourceLocation makeTemplateLocation();

    private boolean astralium$templateAssigned;

    @Inject(method = "handleDataMarker", at = @At("HEAD"), cancellable = true)
    private void astralium$assignShipTemplateLoot(String marker, BlockPos position, ServerLevelAccessor level,
                                                   RandomSource random, BoundingBox bounds, CallbackInfo callbackInfo) {
        if (astralium$templateAssigned || !marker.startsWith("Chest") || !isShipTemplate()) {
            return;
        }

        BlockPos chestPosition = position.below();
        if (!bounds.isInside(chestPosition)) {
            return;
        }

        RandomizableContainerBlockEntity.setLootTable(
                level,
                random,
                chestPosition,
                new ResourceLocation("astralium", "chests/end_city_treasure")
        );
        astralium$templateAssigned = true;
        callbackInfo.cancel();
    }

    private boolean isShipTemplate() {
        ResourceLocation location = makeTemplateLocation();
        return location != null && location.getNamespace().equals("minecraft") && location.getPath().equals("end_city/ship");
    }
}
