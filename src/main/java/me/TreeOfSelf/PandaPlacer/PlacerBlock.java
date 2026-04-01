package me.TreeOfSelf.PandaPlacer;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jspecify.annotations.Nullable;

import static me.TreeOfSelf.PandaPlacer.PandaPlacer.FLIP_BLOCKS;
import static me.TreeOfSelf.PandaPlacer.PandaPlacer.MULTI_FACE_GROWTH;
import static me.TreeOfSelf.PandaPlacer.PandaPlacer.MUST_BE_PLACED_IN_WATER;
import static net.minecraft.world.level.block.DispenserBlock.FACING;
import static net.minecraft.world.level.block.DispenserBlock.TRIGGERED;

public class PlacerBlock extends DropperBlock implements PolymerBlock {

	public static final IntegerProperty EXTRA_FACING = BlockStateProperties.ROTATION_16;
	public static final IntegerProperty NESW_FACING = BlockStateProperties.AGE_15;

	static BlockState applyAllProperties(BlockState fromState, BlockState toState) {
		for (Property<?> property : fromState.getProperties()) {
			toState = applyProperty(toState, property, fromState.getValue(property));
		}
		return toState;
	}

	private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> property, Comparable<?> value) {
		return state.setValue(property, property.getValueClass().cast(value));
	}

	Direction RotationToFacing(Integer rotation) {
		return switch (rotation) {
			case 0, 3 -> Direction.SOUTH;
			case 4, 7 -> Direction.WEST;
			case 8, 11 -> Direction.NORTH;
			case 12, 15 -> Direction.EAST;
			default -> Direction.NORTH;
		};
	}

	public PlacerBlock(BlockBehaviour.Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, false).setValue(EXTRA_FACING, 0).setValue(NESW_FACING, 0));
	}

	private void setInhabited(ServerLevel level, BlockPos infront) {
		ChunkAccess chunk = level.getChunkAt(infront);
		if (chunk.getInhabitedTime() < 6000L) {
			chunk.setInhabitedTime(6000L);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Player player = ctx.getPlayer();
		Direction facing = ctx.getNearestLookingDirection().getOpposite();
		int extra = player != null ? RotationSegment.convertToSegment(player.getYRot()) : 0;
		int nesw = player != null ? RotationSegment.convertToSegment(player.getDirection()) : 0;
		return this.defaultBlockState().setValue(FACING, facing).setValue(EXTRA_FACING, extra).setValue(NESW_FACING, nesw);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, TRIGGERED, EXTRA_FACING, NESW_FACING);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		level.setBlock(pos, state, 3);
	}

	@Override
	public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
		return Blocks.DROPPER.defaultBlockState().setValue(FACING, state.getValue(FACING));
	}

	@Override
	public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayer player) {
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PlacerBlockEntity(pos, state);
	}

	@Override
	protected void dispenseFrom(ServerLevel level, BlockState state, BlockPos pos) {
		PlacerBlockEntity dispenserBlockEntity = (PlacerBlockEntity) level.getBlockEntity(pos);
		BlockSource blockSource = new BlockSource(level, pos, state, dispenserBlockEntity);
		int i = dispenserBlockEntity.getRandomSlot(level.getRandom());
		if (i < 0) {
			level.levelEvent(1001, pos, 0);
			level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(dispenserBlockEntity.getBlockState()));
		} else {
			ItemStack itemStack = dispenserBlockEntity.getItem(i);
			BlockPos infront = pos.relative(state.getValue(FACING));

			if (itemStack.getItem() instanceof BlockItem blockItem) {

				Block block = blockItem.getBlock();
				BlockState blockState = block.defaultBlockState();
				BlockState infrontBlockState = level.getBlockState(infront);
				net.minecraft.core.component.DataComponentMap prevComponentMap = null;
				if (level.getBlockEntity(infront) != null) {
					prevComponentMap = level.getBlockEntity(infront).components();
				}
				SoundType soundGroup = block.defaultBlockState().getSoundType();
				SoundEvent placeSound = soundGroup.getPlaceSound();

				if (blockState.getProperties().contains(BlockStateProperties.FACING)) {
					if (!blockState.is(FLIP_BLOCKS)) {
						blockState = blockState.setValue(BlockStateProperties.FACING, state.getValue(FACING));
					} else {
						blockState = blockState.setValue(BlockStateProperties.FACING, state.getValue(FACING).getOpposite());
					}
				}

				if (blockState.getProperties().contains(BlockStateProperties.HORIZONTAL_FACING)) {
					if (state.getValue(FACING) != Direction.UP && state.getValue(FACING) != Direction.DOWN) {
						if (!blockState.is(FLIP_BLOCKS)) {
							blockState = blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(FACING).getOpposite());
						} else {
							blockState = blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, state.getValue(FACING));
						}
					} else {
						if (!blockState.is(FLIP_BLOCKS)) {
							blockState = blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, RotationToFacing(state.getValue(NESW_FACING)));
						} else {
							blockState = blockState.setValue(BlockStateProperties.HORIZONTAL_FACING, RotationToFacing(state.getValue(NESW_FACING)).getOpposite());
						}
					}
				}

				if (blockState.getProperties().contains(BlockStateProperties.ROTATION_16)) {
					if (blockState.is(BlockTags.BANNERS)) {
						blockState = blockState.setValue(BlockStateProperties.ROTATION_16, (state.getValue(EXTRA_FACING) + 8) % 16);
					} else {
						blockState = blockState.setValue(BlockStateProperties.ROTATION_16, state.getValue(EXTRA_FACING));
					}
				}

				if (blockState.getProperties().contains(BlockStateProperties.FACING_HOPPER)
						&& state.getValue(FACING) != Direction.UP) {
					blockState = blockState.setValue(BlockStateProperties.FACING_HOPPER, state.getValue(FACING));
				}

				if (blockState.getProperties().contains(BlockStateProperties.HALF)
						&& (state.getValue(FACING) == Direction.UP || state.getValue(FACING) == Direction.DOWN)) {
					if (state.getValue(FACING) == Direction.UP) {
						blockState = blockState.setValue(BlockStateProperties.HALF, Half.TOP);
					} else {
						blockState = blockState.setValue(BlockStateProperties.HALF, Half.BOTTOM);
					}
				}

				if (blockState.getProperties().contains(BlockStateProperties.SLAB_TYPE)
						&& (state.getValue(FACING) == Direction.UP || state.getValue(FACING) == Direction.DOWN)) {
					if (state.getValue(FACING) == Direction.UP) {
						blockState = blockState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.BOTTOM);
					} else {
						blockState = blockState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.TOP);
					}
				}

				if (blockState.getProperties().contains(BlockStateProperties.ATTACH_FACE)) {
					blockState = switch (state.getValue(FACING)) {
						case UP -> blockState.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.CEILING);
						case DOWN -> blockState.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR);
						default -> blockState.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL);
					};
				}

				if (blockState.getProperties().contains(BlockStateProperties.AXIS)) {
					blockState = switch (state.getValue(FACING)) {
						case WEST, EAST -> blockState.setValue(BlockStateProperties.AXIS, Direction.Axis.X);
						case NORTH, SOUTH -> blockState.setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
						case UP, DOWN -> blockState.setValue(BlockStateProperties.AXIS, Direction.Axis.Y);
					};
				}

				if (blockState.getProperties().contains(BlockStateProperties.HORIZONTAL_AXIS)) {
					blockState = switch (RotationToFacing(state.getValue(NESW_FACING))) {
						case WEST, EAST -> blockState.setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X);
						case NORTH, SOUTH -> blockState.setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z);
						case UP, DOWN -> blockState.setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X);
					};
				}

				if (blockState.getProperties().contains(BlockStateProperties.WATERLOGGED)) {
					if (level.getFluidState(infront).getType() == Fluids.WATER) {
						blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, true);
					} else {
						blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);
					}
				}

				if (blockState.is(MUST_BE_PLACED_IN_WATER)) {
					FluidState fluidState = level.getFluidState(infront);
					if (!fluidState.is(FluidTags.WATER) || fluidState.getAmount() != 8) {
						level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
						return;
					}
				}

				BlockState secondBlockState = null;

				if (blockState.getProperties().contains(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
					if (state.getValue(FACING) == Direction.DOWN) {
						blockState = blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
						secondBlockState = block.defaultBlockState();
						secondBlockState = applyAllProperties(blockState, secondBlockState);
						secondBlockState = secondBlockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
					} else {
						blockState = blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
						secondBlockState = block.defaultBlockState();
						secondBlockState = applyAllProperties(blockState, secondBlockState);
						secondBlockState = secondBlockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
					}
				}

				if (blockState.getProperties().contains(BlockStateProperties.BED_PART)) {
					blockState = blockState.setValue(BlockStateProperties.BED_PART, BedPart.HEAD);
					secondBlockState = block.defaultBlockState();
					secondBlockState = applyAllProperties(blockState, secondBlockState);
					secondBlockState = secondBlockState.setValue(BlockStateProperties.BED_PART, BedPart.FOOT);
				}

				if (blockState.getProperties().contains(BlockStateProperties.CANDLES)) {
					BlockState infrontState = level.getBlockState(infront);
					if (infrontState.getBlock() == block) {
						if (infrontState.getValue(BlockStateProperties.CANDLES) < 4) {
							level.setBlock(infront, infrontState.setValue(BlockStateProperties.CANDLES, infrontState.getValue(BlockStateProperties.CANDLES) + 1), 3);
							level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
							itemStack.shrink(1);
							BlockNameIntegration.place(level, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
							setInhabited(level, infront);
						} else {
							level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
						}
						return;
					}
				}

				if (blockState.getProperties().contains(BlockStateProperties.PICKLES)) {
					BlockState infrontState = level.getBlockState(infront);
					if (infrontState.getBlock() == block) {
						if (infrontState.getValue(BlockStateProperties.PICKLES) < 4) {
							level.setBlock(infront, infrontState.setValue(BlockStateProperties.PICKLES, infrontState.getValue(BlockStateProperties.PICKLES) + 1), 3);
							level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
							BlockNameIntegration.place(level, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
							setInhabited(level, infront);
							itemStack.shrink(1);
						} else {
							level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
						}
						return;
					}
				}

				if (blockState.getProperties().contains(BlockStateProperties.LAYERS)) {
					BlockState infrontState = level.getBlockState(infront);
					if (infrontState.getBlock() == block) {
						if (infrontState.getValue(BlockStateProperties.LAYERS) < 8) {
							level.setBlock(infront, infrontState.setValue(BlockStateProperties.LAYERS, infrontState.getValue(BlockStateProperties.LAYERS) + 1), 3);
							level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
							BlockNameIntegration.place(level, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
							setInhabited(level, infront);
							itemStack.shrink(1);
						} else {
							level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
						}
						return;
					}
				}

				if (blockState.is(MULTI_FACE_GROWTH)) {
					if (level.getBlockState(infront).getBlock() == block) {
						blockState = level.getBlockState(infront);
					}
					MultiFaceGrowthUtil.Result result = new MultiFaceGrowthUtil().getPlacementShape(blockState, level, infront, state.getValue(FACING));
					blockState = result.state;

					if (result.canGrow && level.getBlockState(infront).is(MULTI_FACE_GROWTH) && level.getBlockState(infront).getBlock() != block) {
						level.setBlock(infront, Blocks.AIR.defaultBlockState(), 3);
					}

					if (result.canGrow && (level.getBlockState(infront).canBeReplaced() || level.getBlockState(infront).getBlock() == block)) {
						level.setBlock(infront, blockState, 3);
						level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
						BlockNameIntegration.place(level, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
						setInhabited(level, infront);
						itemStack.shrink(1);
					} else {
						level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
					}
					return;
				}

				if (secondBlockState != null) {
					BlockPos.MutableBlockPos secondInfrontMutable = infront.mutable();
					BlockPos secondInfront = null;
					BlockPos checkPos = infront;
					BlockState checkState = blockState;
					if (blockState.getProperties().contains(BlockStateProperties.BED_PART)) {
						secondInfront = secondInfrontMutable.relative(state.getValue(FACING));
					} else {
						if (blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)) {
							secondInfront = secondInfrontMutable.relative(Direction.DOWN);
							checkPos = secondInfront;
							checkState = secondBlockState;
						} else {
							secondInfront = secondInfrontMutable.relative(Direction.UP);
						}
					}

					if (checkState.canSurvive(level, checkPos) && level.getBlockState(infront).canBeReplaced()
							&& level.getBlockState(secondInfront).canBeReplaced()) {
						level.setBlock(secondInfront, secondBlockState, 3);
						level.setBlock(infront, blockState, 3);
						level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
						BlockNameIntegration.place(level, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
						setInhabited(level, infront);
						itemStack.shrink(1);
					} else {
						level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
					}
					return;
				}

				if (blockState.is(BlockTags.SLABS)) {
					BlockState otherState = level.getBlockState(infront);
					if (otherState.getBlock() == block) {
						SlabType otherSlabType = otherState.getValue(BlockStateProperties.SLAB_TYPE);
						if (otherSlabType != SlabType.DOUBLE && otherSlabType != blockState.getValue(BlockStateProperties.SLAB_TYPE)) {
							level.setBlock(infront, otherState.setValue(BlockStateProperties.SLAB_TYPE, SlabType.DOUBLE), 3);
							level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
							BlockNameIntegration.place(level, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
							setInhabited(level, infront);
							itemStack.shrink(1);
						} else {
							level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
						}
						return;
					}
				}

				if (blockState.canSurvive(level, infront) && level.getBlockState(infront).canBeReplaced()) {
					level.setBlock(infront, blockState, 3);
					level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
					if (block == Blocks.PLAYER_HEAD) {
						HeadPlacerIntegration.placeHead(level, infront, itemStack);
					}

					BlockEntity blockEntity = level.getBlockEntity(infront);
					if (blockEntity != null) {
						blockEntity.applyComponentsFromItemStack(itemStack);
					}

					BlockNameIntegration.place(level, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
					setInhabited(level, infront);
					itemStack.shrink(1);
				} else {
					level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
					return;
				}

			} else {
				DispenseItemBehavior dispenserBehavior = this.getDispenseMethod(level, itemStack);
				if (dispenserBehavior != DispenseItemBehavior.NOOP) {
					dispenserBlockEntity.setItem(i, dispenserBehavior.dispense(blockSource, itemStack));
				}
			}

		}
	}

}
