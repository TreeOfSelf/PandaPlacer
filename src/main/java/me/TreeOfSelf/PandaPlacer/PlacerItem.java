package me.TreeOfSelf.PandaPlacer;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.Items;

public class PlacerItem extends PolymerBlockItem  {


    public PlacerItem(Block block, Settings settings) {
        super(block, settings, Items.DISPENSER);
    }


}
