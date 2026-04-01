package me.TreeOfSelf.PandaPlacer;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PandaPlacer implements ModInitializer {
	public static final String MOD_ID = "panda-placer";
	public static BlockEntityType<PlacerBlockEntity> PLACER_BLOCK_ENTITY_TYPE;
	public static Block PLACER_BLOCK;
	public static Item PLACER_ITEM;

	public static final TagKey<Block> MUST_BE_PLACED_IN_WATER = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "must_be_placed_in_water"));
	public static final TagKey<Block> MULTI_FACE_GROWTH = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "multi_face_growth"));
	public static final TagKey<Block> FLIP_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "flip_blocks"));

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Identifier blockId = Identifier.fromNamespaceAndPath(MOD_ID, "placerblock");
		Identifier itemId = Identifier.fromNamespaceAndPath(MOD_ID, "placeritem");
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, blockId);
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemId);

		BlockBehaviour.Properties blockSettings = BlockBehaviour.Properties.of().strength(3.5F).setId(blockKey);

		PLACER_BLOCK = Registry.register(
				BuiltInRegistries.BLOCK,
				blockId,
				new PlacerBlock(blockSettings)
		);

		Item.Properties itemSettings = new Item.Properties()
				.useBlockDescriptionPrefix()
				.setId(itemKey);

		PLACER_ITEM = Registry.register(
				BuiltInRegistries.ITEM,
				itemId,
				new PlacerItem(PLACER_BLOCK, itemSettings)
		);

		PLACER_BLOCK_ENTITY_TYPE = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				Identifier.fromNamespaceAndPath(MOD_ID, "placerblockentity"),
				FabricBlockEntityTypeBuilder.create(PlacerBlockEntity::new, PLACER_BLOCK).build()
		);

		PolymerBlockUtils.registerBlockEntity(PLACER_BLOCK_ENTITY_TYPE);

		LOGGER.info("PandaPlacer Started!");
	}
}
