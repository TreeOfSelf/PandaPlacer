package me.TreeOfSelf.PandaPlacer;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class PlacerItem extends PolymerBlockItem {
	public PlacerItem(Block block, Properties settings) {
		super(block, settings, Items.DROPPER);
	}
}
