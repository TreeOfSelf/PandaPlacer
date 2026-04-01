package me.TreeOfSelf.PandaPlacer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.UP;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST;

public class MultiFaceGrowthUtil {

	public static class Result {
		public final boolean canGrow;
		public final BlockState state;

		public Result(boolean canGrow, BlockState state) {
			this.canGrow = canGrow;
			this.state = state;
		}
	}

	public Result getPlacementShape(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		boolean canGrow = MultifaceBlock.canAttachTo(world, direction, pos.relative(direction), world.getBlockState(pos.relative(direction)));

		if (canGrow) {
			switch (direction) {
				case DOWN:
					if (!state.getProperties().contains(DOWN) || state.getValue(DOWN)) {
						canGrow = false;
					} else {
						state = state.setValue(DOWN, true);
					}
					break;
				case UP:
					if (!state.getProperties().contains(UP) || state.getValue(UP)) {
						canGrow = false;
					} else {
						state = state.setValue(UP, true);
					}
					break;
				case NORTH:
					if (!state.getProperties().contains(NORTH) || state.getValue(NORTH)) {
						canGrow = false;
					} else {
						state = state.setValue(NORTH, true);
					}
					break;
				case EAST:
					if (!state.getProperties().contains(EAST) || state.getValue(EAST)) {
						canGrow = false;
					} else {
						state = state.setValue(EAST, true);
					}
					break;
				case SOUTH:
					if (!state.getProperties().contains(SOUTH) || state.getValue(SOUTH)) {
						canGrow = false;
					} else {
						state = state.setValue(SOUTH, true);
					}
					break;
				case WEST:
					if (!state.getProperties().contains(WEST) || state.getValue(WEST)) {
						canGrow = false;
					} else {
						state = state.setValue(WEST, true);
					}
					break;
			}
		}

		return new Result(canGrow, state);
	}
}
