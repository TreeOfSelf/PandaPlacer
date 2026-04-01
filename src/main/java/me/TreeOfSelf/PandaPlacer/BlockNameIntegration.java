package me.TreeOfSelf.PandaPlacer;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;

public class BlockNameIntegration {
	public static void place(Level world, BlockState prevBlockState, BlockState blockState, BlockPos blockPos, ItemStack itemStack, DataComponentMap prevComponentMap) {
		try {
			Class<?> blockNameClass = Class.forName("me.TreeOfSelf.PandaBlockName.BlockEntityPlacer");
			Method placeMethod = blockNameClass.getMethod("place", Level.class, BlockState.class, BlockState.class, BlockPos.class, ItemStack.class, DataComponentMap.class);
			placeMethod.invoke(null, world, prevBlockState, blockState, blockPos, itemStack, prevComponentMap);
		} catch (Exception ignored) {
		}
	}
}
