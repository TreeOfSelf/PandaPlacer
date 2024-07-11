package me.sebastian420.PandaPlacer;

import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;

public class BlockNameIntegration {
    public static void place(World world, BlockState prevBlockState, BlockState blockState, BlockPos blockPos, ItemStack itemStack, ComponentMap prevComponentMap ) {
        try {
            Class<?> blockNameClass = Class.forName("me.sebastian420.PandaBlockName.BlockEntityPlacer");
            Method placeMethod = blockNameClass.getMethod("place", World.class,BlockState.class, BlockState.class ,BlockPos.class, ItemStack.class, ComponentMap.class);
            placeMethod.invoke(null, world, prevBlockState, blockState, blockPos,itemStack, prevComponentMap);
        } catch (Exception ignored) {
        }
    }
}
