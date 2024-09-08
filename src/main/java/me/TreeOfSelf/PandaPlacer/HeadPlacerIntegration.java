package me.TreeOfSelf.PandaPlacer;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Method;

public class HeadPlacerIntegration {
    public static void placeHead(World world, BlockPos infront, ItemStack itemStack) {
        try {
            Class<?> headPlacerClass = Class.forName("me.TreeOfSelf.PandaHeads.HeadPlacer");
            Method placeMethod = headPlacerClass.getMethod("place", World.class, BlockPos.class, ItemStack.class);
            placeMethod.invoke(null, world, infront, itemStack);
        } catch (Exception ignored) {}
    }
}
