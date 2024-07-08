package me.sebastian420.PandaPlacer;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import static me.sebastian420.PandaPlacer.PandaPlacer.PLACER_ITEM;


public class PlacerBlock extends DispenserBlock implements PolymerBlock {



    public static final IntProperty EXTRA_FACING = Properties.ROTATION;
    public static final IntProperty NESW_FACING = Properties.AGE_15;


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
    public boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return false;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        return new ItemStack(PLACER_ITEM);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PlacerBlockEntity(pos, state);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.DISPENSER.getDefaultState().with(FACING, state.get(FACING));
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

                if (world.getBlockState(infront).isReplaceable()) {
                    Block block = ((BlockItem) itemStack.getItem()).getBlock();
                    BlockState blockState = block.getDefaultState();
                    BlockSoundGroup soundGroup = block.getDefaultState().getSoundGroup();
                    SoundEvent placeSound = soundGroup.getPlaceSound();

                    //Handle properties
                    if (blockState.getProperties().contains(Properties.FACING)) {
                        blockState = blockState.with(Properties.FACING, state.get(FACING));
                    }

                    if (blockState.getProperties().contains(Properties.HORIZONTAL_FACING)) {
                        if(state.get(FACING) != Direction.UP && state.get(FACING) != Direction.DOWN){
                            blockState = blockState.with(Properties.HORIZONTAL_FACING, state.get(FACING).getOpposite());
                        } else {
                            blockState = blockState.with(Properties.HORIZONTAL_FACING, RotationToFacing(state.get(NESW_FACING)));
                        }
                    }

                    if (blockState.getProperties().contains(Properties.ROTATION)) {
                        if (blockState.isIn(BlockTags.BANNERS)) {
                            blockState = blockState.with(Properties.ROTATION, (state.get(EXTRA_FACING) + 8) % 16);
                        } else {
                            blockState = blockState.with(Properties.ROTATION, state.get(EXTRA_FACING));
                        }
                    }

                    if (blockState.getProperties().contains(Properties.HOPPER_FACING) &&
                        state.get(FACING) != Direction.UP) {
                        blockState = blockState.with(Properties.HOPPER_FACING, state.get(FACING));
                    }

                    if (blockState.getProperties().contains(Properties.BLOCK_HALF) &&
                        (state.get(FACING) == Direction.UP || (state.get(FACING) == Direction.DOWN))) {
                        if (state.get(FACING) == Direction.UP) {
                            blockState = blockState.with(Properties.BLOCK_HALF, BlockHalf.TOP);
                        } else {
                            blockState = blockState.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM);
                        }
                    }


                    if (blockState.getProperties().contains(Properties.DOUBLE_BLOCK_HALF) &&
                            (state.get(FACING) == Direction.UP || (state.get(FACING) == Direction.DOWN))) {
                        if (state.get(FACING) == Direction.UP) {
                            blockState = blockState.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                        } else {
                            blockState = blockState.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
                        }
                    }

                    if (blockState.getProperties().contains(Properties.SLAB_TYPE) &&
                            (state.get(FACING) == Direction.UP || (state.get(FACING) == Direction.DOWN))) {
                        if (state.get(FACING) == Direction.UP) {
                            blockState = blockState.with(Properties.SLAB_TYPE, SlabType.BOTTOM);
                        } else {
                            blockState = blockState.with(Properties.SLAB_TYPE, SlabType.TOP);
                        }
                    }

                    // One final check to make sure state is possible to place
                    if (blockState.canPlaceAt(world,infront)) {
                        world.setBlockState(infront, blockState);

                        BlockEntity blockEntity = world.getBlockEntity(infront);
                        if (blockEntity != null) {
                            blockEntity.readComponents(itemStack);
                        }

                        world.playSound(null, pos, placeSound, SoundCategory.BLOCKS, 1.0F, 1.0F);
                        itemStack.setCount(itemStack.getCount() - 1);
                    } else {
                        world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    }
                } else {
                    world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1.0F, 1.0F);
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