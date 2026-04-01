package me.TreeOfSelf.PandaPlacer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;

public class HeadPlacerIntegration {
	public static void placeHead(Level world, BlockPos infront, ItemStack itemStack) {
		try {
			Class<?> headPlacerClass = Class.forName("me.TreeOfSelf.PandaHeads.HeadPlacer");
			Method placeMethod = headPlacerClass.getMethod("place", Level.class, BlockPos.class, ItemStack.class);
			placeMethod.invoke(null, world, infront, itemStack);
		} catch (Exception ignored) {
		}
	}
}
