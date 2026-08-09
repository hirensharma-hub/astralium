package com.hirensharma.astralium.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(EndCityPieces.EndCityPiece.class)
public abstract class EndCityPieceMixin {
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
        String templateName = astralium$getTemplateName();
        return "ship".equals(templateName)
                || "end_city/ship".equals(templateName)
                || "minecraft:end_city/ship".equals(templateName);
    }

    private String astralium$getTemplateName() {
        Class<?> type = getClass();
        while (type != null) {
            for (String fieldName : new String[] {"f_163658_", "templateName"}) {
                try {
                    Field field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = field.get(this);
                    return value instanceof String ? (String) value : null;
                } catch (NoSuchFieldException ignored) {
                    // Try the other mapping name and then the superclass.
                } catch (IllegalAccessException ignored) {
                    return null;
                }
            }
            type = type.getSuperclass();
        }
        return null;
    }
}
