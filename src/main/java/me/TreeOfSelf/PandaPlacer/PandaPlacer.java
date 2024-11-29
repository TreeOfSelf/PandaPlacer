package me.TreeOfSelf.PandaPlacer;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaPlacer implements ModInitializer {
	public static final String MOD_ID = "panda-placer";
	public static BlockEntityType<PlacerBlockEntity> PLACER_BLOCK_ENTITY_TYPE;
	public static Block PLACER_BLOCK;
	public static Item PLACER_ITEM;

	public static final TagKey<Block> MUST_BE_PLACED_IN_WATER = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "must_be_placed_in_water"));
	public static final TagKey<Block> MULTI_FACE_GROWTH = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "multi_face_growth"));
	public static final TagKey<Block> FLIP_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "flip_blocks"));

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Identifier blockId = Identifier.of(MOD_ID, "placerblock");
		Identifier itemId = Identifier.of(MOD_ID, "placeritem");
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, blockId);
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, itemId);

		// Initialize block settings
		Block.Settings blockSettings = Block.Settings.create()
				.registryKey(blockKey).hardness(3.5F);

		// Create and register the block
		PLACER_BLOCK = Registry.register(
				Registries.BLOCK,
				blockId,
				new PlacerBlock(blockSettings)
		);

		// Initialize item settings
		Item.Settings itemSettings = new Item.Settings()
				.useBlockPrefixedTranslationKey()
				.registryKey(itemKey);



		// Create and register the item
		PLACER_ITEM = Registry.register(
				Registries.ITEM,
				itemId,
				new PlacerItem(PLACER_BLOCK, itemSettings)
		);

		// Register block entity
		PLACER_BLOCK_ENTITY_TYPE = Registry.register(
				Registries.BLOCK_ENTITY_TYPE,
				Identifier.of(MOD_ID, "placerblockentity"),
				FabricBlockEntityTypeBuilder.create(PlacerBlockEntity::new, PLACER_BLOCK).build()
		);

		PolymerBlockUtils.registerBlockEntity(PLACER_BLOCK_ENTITY_TYPE);

		LOGGER.info("PandaPlacer Started!");
	}
}