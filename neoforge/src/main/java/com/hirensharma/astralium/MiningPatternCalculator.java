package com.hirensharma.astralium;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class MiningPatternCalculator {
    private MiningPatternCalculator() {}

    public static List<BlockPos> calculate(BlockPos center, Direction hitFace, Direction playerFacing, MiningMode selectedMode) {
        MiningMode mode = selectedMode;
        List<BlockPos> positions = new ArrayList<>();
        positions.add(center);
        if (mode == MiningMode.NORMAL) return positions;
        Direction horizontalAxis;
        Direction verticalAxis;
        if (hitFace == Direction.NORTH || hitFace == Direction.SOUTH) {
            horizontalAxis = Direction.EAST;
            verticalAxis = Direction.UP;
        } else if (hitFace == Direction.EAST || hitFace == Direction.WEST) {
            horizontalAxis = Direction.SOUTH;
            verticalAxis = Direction.UP;
        } else {
            Direction forward = playerFacing.getAxis().isHorizontal() ? playerFacing : Direction.NORTH;
            horizontalAxis = forward.getClockWise();
            verticalAxis = forward;
        }
        int horizontalRadius = mode.width() / 2;
        int verticalRadius = mode.height() / 2;
        for (int v = -verticalRadius; v <= verticalRadius; v++) {
            for (int h = -horizontalRadius; h <= horizontalRadius; h++) {
                if (h == 0 && v == 0) continue;
                BlockPos pos = center.relative(horizontalAxis, h).relative(verticalAxis, v);
                if (!positions.contains(pos)) positions.add(pos);
            }
        }
        return positions;
    }
}
