package me.sebastian420.PandaPlacer;

import net.minecraft.block.BlockState;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import static net.minecraft.state.property.Properties.UP;
import static net.minecraft.state.property.Properties.DOWN;
import static net.minecraft.state.property.Properties.NORTH;
import static net.minecraft.state.property.Properties.EAST;
import static net.minecraft.state.property.Properties.SOUTH;
import static net.minecraft.state.property.Properties.WEST;


public class MultiFaceGrowthUtil {

    public static class Result {
        public final boolean canGrow;
        public final BlockState state;

        public Result(boolean canGrow, BlockState state) {
            this.canGrow = canGrow;
            this.state = state;
        }
    }

    public Result getPlacementShape(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        boolean canGrow = MultifaceGrowthBlock.canGrowOn(world, direction, pos.offset(direction), world.getBlockState(pos.offset(direction)));

        if (canGrow) {
            switch (direction) {
                case DOWN:
                    if (!state.getProperties().contains(DOWN) || state.get(DOWN)) {
                        canGrow = false;
                    } else {
                        state = state.with(DOWN, canGrow);
                    }
                    break;
                case UP:
                    if (!state.getProperties().contains(UP) || state.get(UP)) {
                        canGrow = false;
                    } else {
                        state = state.with(UP, canGrow);
                    }
                    break;
                case NORTH:
                    if (!state.getProperties().contains(NORTH) || state.get(NORTH)) {
                        canGrow = false;
                    } else {
                        state = state.with(NORTH, canGrow);
                    }
                    break;
                case EAST:
                    if (!state.getProperties().contains(EAST) || state.get(EAST)) {
                        canGrow = false;
                    } else {
                        state = state.with(EAST, canGrow);
                    }
                    break;
                case SOUTH:
                    if (!state.getProperties().contains(SOUTH) || state.get(SOUTH)) {
                        canGrow = false;
                    } else {
                        state = state.with(SOUTH, canGrow);
                    }
                    break;
                case WEST:
                    if (!state.getProperties().contains(WEST) || state.get(WEST)) {
                        canGrow = false;
                    } else {
                        state = state.with(WEST, canGrow);
                    }
                    break;
            }
        }

        return new Result(canGrow, state);
    }
}
