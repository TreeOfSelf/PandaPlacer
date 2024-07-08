package me.sebastian420.PandaPlacer;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaPlacer implements ModInitializer {
	public static BlockEntityType<PlacerBlockEntity> PLACER_BLOCK_ENTITY_TYPE;
	public static final Block PLACER_BLOCK = new PlacerBlock(Blocks.DISPENSER.getSettings());
	public static final Item PLACER_ITEM = new PlacerItem(PLACER_BLOCK, new Item.Settings());


    public static final Logger LOGGER = LoggerFactory.getLogger("panda-placer");

	@Override
	public void onInitialize() {
		PLACER_BLOCK_ENTITY_TYPE = Registry.register(
				Registries.BLOCK_ENTITY_TYPE,
				Identifier.of("panda-placer", "placerblockentity"),
				FabricBlockEntityTypeBuilder.create(PlacerBlockEntity::new).build(null));

		PolymerBlockUtils.registerBlockEntity(PLACER_BLOCK_ENTITY_TYPE);

		Registry.register(Registries.BLOCK, Identifier.of("panda-placer", "placerblock"), PLACER_BLOCK);
		Registry.register(Registries.ITEM, Identifier.of("panda-placer", "placeritem"), PLACER_ITEM);

		LOGGER.info("PandaPlacer Started!");
	}
}