package me.TreeOfSelf.PandaPlacer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;

public class PlacerBlockEntity extends DispenserBlockEntity {

	protected PlacerBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(PandaPlacer.PLACER_BLOCK_ENTITY_TYPE, blockPos, blockState);
	}

	@Override
	protected Component getDefaultName() {
		return Component.literal("Placer");
	}

}
