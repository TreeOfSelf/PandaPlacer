package me.TreeOfSelf.PandaPlacer;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.*;
import net.minecraft.component.ComponentMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import static me.TreeOfSelf.PandaPlacer.PandaPlacer.*;


public class PlacerBlock extends DispenserBlock implements PolymerBlock {



    public static final IntProperty EXTRA_FACING = Properties.ROTATION;
    public static final IntProperty NESW_FACING = Properties.AGE_15;

    static BlockState applyAllProperties(BlockState fromState, BlockState toState) {
        for (Property<?> property : fromState.getProperties()) {
            toState = applyProperty(toState, property, fromState.get(property));
        }
        return toState;
    }

    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> property, Comparable<?> value) {
        return state.with(property, property.getType().cast(value));
    }

    Direction RotationToFacing(Integer rotation){
        return switch (rotation) {
            case 0, 3 -> Direction.SOUTH;
            case 4, 7 -> Direction.WEST;
            case 8 , 11 -> Direction.NORTH;
            case 12 , 15 -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }

    public PlacerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(TRIGGERED, false).with(EXTRA_FACING, 0).with(NESW_FACING,0));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite())
                .with(EXTRA_FACING, RotationPropertyHelper.fromYaw(ctx.getPlayerYaw()))
                .with(NESW_FACING, RotationPropertyHelper.fromDirection(ctx.getHorizontalPlayerFacing()));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED, EXTRA_FACING, NESW_FACING);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        world.setBlockState(pos, state);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.DISPENSER.getDefaultState().with(FACING, state.get(FACING));
    }

    @Override
    public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return false;
    }

    /*@Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(PLACER_ITEM);
    }*/

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PlacerBlockEntity(pos, state);
    }

    @Override
    protected void dispense(ServerWorld world, BlockState state, BlockPos pos) {
        PlacerBlockEntity dispenserBlockEntity = (PlacerBlockEntity) world.getBlockEntity(pos);
        BlockPointer blockPointer = new BlockPointer(world, pos, state, dispenserBlockEntity);
        int i = dispenserBlockEntity.chooseNonEmptySlot(world.random);
        if (i < 0) {
            world.syncWorldEvent(1001, pos, 0);
            world.emitGameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Emitter.of(dispenserBlockEntity.getCachedState()));
        } else {
            ItemStack itemStack = dispenserBlockEntity.getStack(i);
            BlockPos infront = pos.offset(state.get(FACING));

            if (itemStack.getItem() instanceof BlockItem) {

                Block block = ((BlockItem) itemStack.getItem()).getBlock();
                BlockState blockState = block.getDefaultState();
                BlockState infrontBlockState = world.getBlockState(infront);
                ComponentMap prevComponentMap = null;
                if (world.getBlockEntity(infront) != null) prevComponentMap = world.getBlockEntity(infront).getComponents();
                BlockSoundGroup soundGroup = block.getDefaultState().getSoundGroup();
                SoundEvent placeSound = soundGroup.getPlaceSound();


                //Handle properties

                //Facing Property
                if (blockState.getProperties().contains(Properties.FACING)) {
                    if (!blockState.isIn(FLIP_BLOCKS)) {
                        blockState = blockState.with(Properties.FACING, state.get(FACING));
                    } else {
                        blockState = blockState.with(Properties.FACING, state.get(FACING).getOpposite());
                    }
                }


                //Horizontal Facing
                if (blockState.getProperties().contains(Properties.HORIZONTAL_FACING)) {
                    if(state.get(FACING) != Direction.UP && state.get(FACING) != Direction.DOWN){
                        if (!blockState.isIn(FLIP_BLOCKS)) {
                            blockState = blockState.with(Properties.HORIZONTAL_FACING, state.get(FACING).getOpposite());
                        } else {
                            blockState = blockState.with(Properties.HORIZONTAL_FACING, state.get(FACING));
                        }
                    } else {
                        if (!blockState.isIn(FLIP_BLOCKS)) {
                            blockState = blockState.with(Properties.HORIZONTAL_FACING, RotationToFacing(state.get(NESW_FACING)));
                        } else {
                            blockState = blockState.with(Properties.HORIZONTAL_FACING, RotationToFacing(state.get(NESW_FACING)).getOpposite());
                        }
                    }
                }

                //Rotation
                if (blockState.getProperties().contains(Properties.ROTATION)) {
                    if (blockState.isIn(BlockTags.BANNERS)) {
                        blockState = blockState.with(Properties.ROTATION, (state.get(EXTRA_FACING) + 8) % 16);
                    } else {
                        blockState = blockState.with(Properties.ROTATION, state.get(EXTRA_FACING));
                    }
                }

                //Hopper Facing
                if (blockState.getProperties().contains(Properties.HOPPER_FACING) &&
                    state.get(FACING) != Direction.UP) {
                    blockState = blockState.with(Properties.HOPPER_FACING, state.get(FACING));
                }

                //Block Half
                if (blockState.getProperties().contains(Properties.BLOCK_HALF) &&
                    (state.get(FACING) == Direction.UP || (state.get(FACING) == Direction.DOWN))) {
                    if (state.get(FACING) == Direction.UP) {
                        blockState = blockState.with(Properties.BLOCK_HALF, BlockHalf.TOP);
                    } else {
                        blockState = blockState.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM);;
                    }
                }

                //Slab
                if (blockState.getProperties().contains(Properties.SLAB_TYPE) &&
                        (state.get(FACING) == Direction.UP || (state.get(FACING) == Direction.DOWN))) {
                    if (state.get(FACING) == Direction.UP) {
                        blockState = blockState.with(Properties.SLAB_TYPE, SlabType.BOTTOM);
                    } else {
                        blockState = blockState.with(Properties.SLAB_TYPE, SlabType.TOP);
                    }
                }

                //Block Face
                if (blockState.getProperties().contains(Properties.BLOCK_FACE)){
                    blockState = switch (state.get(FACING)) {
                        case Direction.UP -> blockState.with(Properties.BLOCK_FACE, BlockFace.CEILING);
                        case Direction.DOWN -> blockState.with(Properties.BLOCK_FACE, BlockFace.FLOOR);
                        default -> blockState.with(Properties.BLOCK_FACE, BlockFace.WALL);
                    };
                }

                //Axis
                if (blockState.getProperties().contains(Properties.AXIS)){
                    blockState = switch (state.get(FACING)) {
                        case Direction.WEST, Direction.EAST -> blockState.with(Properties.AXIS, Direction.Axis.X);
                        case Direction.NORTH, Direction.SOUTH -> blockState.with(Properties.AXIS, Direction.Axis.Z);
                        case Direction.UP, Direction.DOWN -> blockState.with(Properties.AXIS, Direction.Axis.Y);
                    };
                }

                //Horizontal Axis
                if (blockState.getProperties().contains(Properties.HORIZONTAL_AXIS)){
                    blockState = switch (RotationToFacing(state.get(NESW_FACING))) {
                        case Direction.WEST, Direction.EAST -> blockState.with(Properties.HORIZONTAL_AXIS, Direction.Axis.X);
                        case Direction.NORTH, Direction.SOUTH -> blockState.with(Properties.HORIZONTAL_AXIS, Direction.Axis.Z);
                        case Direction.UP, Direction.DOWN -> blockState.with(Properties.HORIZONTAL_AXIS, Direction.Axis.X);
                    };
                }

                //Waterlogged
                if (blockState.getProperties().contains(Properties.WATERLOGGED)){
                    if ( (world.getFluidState(infront).getFluid() == Fluids.WATER)) {
                        blockState = blockState.with(Properties.WATERLOGGED, true);
                    } else {
                        blockState = blockState.with(Properties.WATERLOGGED, false);
                    }
                }

                if (blockState.isIn(MUST_BE_PLACED_IN_WATER)){
                    FluidState fluidState = world.getFluidState(infront);
                    if (!fluidState.isIn(FluidTags.WATER) || fluidState.getLevel() != 8) {
                        world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        return;
                    }
                }

                BlockState secondBlockState = null;

                //Double Block Half
                if (blockState.getProperties().contains(Properties.DOUBLE_BLOCK_HALF)) {
                    if (state.get(FACING) == Direction.DOWN) {
                        blockState = blockState.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                        secondBlockState = block.getDefaultState();
                        secondBlockState = applyAllProperties(blockState, secondBlockState);
                        secondBlockState = secondBlockState.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
                    } else {
                        blockState = blockState.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
                        secondBlockState = block.getDefaultState();
                        secondBlockState = applyAllProperties(blockState, secondBlockState);
                        secondBlockState = secondBlockState.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                    }
                }

                //Double Bed Part
                if (blockState.getProperties().contains(Properties.BED_PART)) {
                    blockState = blockState.with(Properties.BED_PART, BedPart.HEAD);
                    secondBlockState = block.getDefaultState();
                    secondBlockState = applyAllProperties(blockState, secondBlockState);
                    secondBlockState = secondBlockState.with(Properties.BED_PART, BedPart.FOOT);
                }

                //Handle Candles
                if (blockState.getProperties().contains(Properties.CANDLES)) {
                    BlockState infrontState = world.getBlockState(infront);
                    if (infrontState.getBlock() == block) {
                        if (infrontState.get(Properties.CANDLES) < 4) {
                            world.setBlockState(infront, infrontState.with(Properties.CANDLES, infrontState.get(Properties.CANDLES) + 1));
                            world.playSound(null, pos, placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            itemStack.setCount(itemStack.getCount() - 1);
                            BlockNameIntegration.place(world, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
                        } else {
                            world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                        return;
                    }
                }

                //Handle Sea Pickles
                if (blockState.getProperties().contains(Properties.PICKLES)) {
                    BlockState infrontState = world.getBlockState(infront);
                    if (infrontState.getBlock() == block) {
                        if (infrontState.get(Properties.PICKLES) < 4) {
                            world.setBlockState(infront, infrontState.with(Properties.PICKLES, infrontState.get(Properties.PICKLES) + 1));
                            world.playSound(null, pos, placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            BlockNameIntegration.place(world, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
                            itemStack.setCount(itemStack.getCount() - 1);
                        } else {
                            world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                        return;
                    }
                }

                //Handle Layers (snow)
                if (blockState.getProperties().contains(Properties.LAYERS)) {
                    BlockState infrontState = world.getBlockState(infront);
                    if (infrontState.getBlock() == block) {
                        if (infrontState.get(Properties.LAYERS) < 8) {
                            world.setBlockState(infront, infrontState.with(Properties.LAYERS, infrontState.get(Properties.LAYERS) + 1));
                            world.playSound(null, pos, placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            BlockNameIntegration.place(world, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
                            itemStack.setCount(itemStack.getCount() - 1);
                        } else {
                            world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                        return;
                    }
                }

                //Handle multi face growth blocks (vines, glow lichen, skulk veins)
                if (blockState.isIn(MULTI_FACE_GROWTH)) {
                    if (world.getBlockState(infront).getBlock() == block) {
                        blockState = world.getBlockState(infront);
                    }
                    MultiFaceGrowthUtil.Result result = new MultiFaceGrowthUtil().getPlacementShape(blockState, world, infront, state.get(FACING));
                    blockState = result.state;

                    if (result.canGrow && world.getBlockState(infront).isIn(MULTI_FACE_GROWTH) && world.getBlockState(infront).getBlock() != block) world.setBlockState(infront, Blocks.AIR.getDefaultState());

                    if (result.canGrow && (world.getBlockState(infront).isReplaceable() || world.getBlockState(infront).getBlock() == block)) {
                        world.setBlockState(infront, blockState);
                        world.playSound(null, pos, placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        BlockNameIntegration.place(world, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
                        itemStack.setCount(itemStack.getCount() - 1);
                    } else {
                        world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                    return;
                }

                //Handle double places
                if (secondBlockState != null) {
                    //Get Position of second block
                    BlockPos.Mutable secondInfrontMutable = infront.mutableCopy();
                    BlockPos secondInfront = null;
                    BlockPos checkPos = infront;
                    BlockState checkState = blockState;
                    if (blockState.getProperties().contains(Properties.BED_PART)) {
                        secondInfront = secondInfrontMutable.offset(state.get(FACING));
                    } else {
                        if (blockState.get(Properties.DOUBLE_BLOCK_HALF).equals(DoubleBlockHalf.UPPER)) {
                            secondInfront = secondInfrontMutable.offset(Direction.DOWN);
                            checkPos = secondInfront;
                            checkState = secondBlockState;
                        } else {
                            secondInfront = secondInfrontMutable.offset(Direction.UP);
                        }
                    }

                    //Check if both are placable
                    if (checkState.canPlaceAt(world,checkPos) && world.getBlockState(infront).isReplaceable() &&
                            world.getBlockState(secondInfront).isReplaceable()){
                        world.setBlockState(secondInfront, secondBlockState);
                        world.setBlockState(infront, blockState);
                        world.playSound(null, pos, placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        BlockNameIntegration.place(world, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
                        itemStack.setCount(itemStack.getCount() - 1);
                    } else {
                        world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                    return;
                }


                //Handle double slab
                if (blockState.isIn(BlockTags.SLABS)){
                    BlockState otherState = world.getBlockState(infront);
                    if (otherState.getBlock() == block) {
                        SlabType otherSlabType = otherState.get(Properties.SLAB_TYPE);
                        if (otherSlabType != SlabType.DOUBLE && otherSlabType != blockState.get(Properties.SLAB_TYPE)) {
                            world.setBlockState(infront, otherState.with(Properties.SLAB_TYPE, SlabType.DOUBLE));
                            world.playSound(null, pos, placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                            BlockNameIntegration.place(world, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
                            itemStack.setCount(itemStack.getCount() - 1);
                        } else {
                            world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        }
                        return;
                    }
                }

                // Handle all other place
                if (blockState.canPlaceAt(world,infront) && world.getBlockState(infront).isReplaceable()) {
                    world.setBlockState(infront, blockState);
                    world.playSound(null, pos, placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    if (block == Blocks.PLAYER_HEAD) {
                        BlockEntity blockEntity = world.getBlockEntity(infront);
                        if (blockEntity != null) blockEntity.readComponents(itemStack);
                        HeadPlacerIntegration.placeHead(world, infront, itemStack);
                    }
                    BlockNameIntegration.place(world, infrontBlockState, blockState, infront, itemStack, prevComponentMap);
                    itemStack.setCount(itemStack.getCount() - 1);
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return;
                }


            } else {
                DispenserBehavior dispenserBehavior = this.getBehaviorForItem(world, itemStack);
                if (dispenserBehavior != DispenserBehavior.NOOP) {
                    dispenserBlockEntity.setStack(i, dispenserBehavior.dispense(blockPointer, itemStack));
                }
            }


        }
    }
}