package me.sebastian420.PandaPlacer;

import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class PlacerItem extends BlockItem implements PolymerItem {


    public PlacerItem(Block block, Settings settings) {
        super(block, settings);
        settings.component(DataComponentTypes.ITEM_NAME, Text.of("FUck"));

    }

    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.DISPENSER;
    }

}
