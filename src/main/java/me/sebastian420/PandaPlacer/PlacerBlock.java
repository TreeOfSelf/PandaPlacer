package me.sebastian420.PandaPlacer;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationCalculator;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;

import static me.sebastian420.PandaPlacer.PandaPlacer.PLACER_ITEM;


public class PlacerBlock extends DispenserBlock implements PolymerBlock {

    public PlacerBlock(Settings settings) {
        super(settings);
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
                Block block = ((BlockItem) itemStack.getItem()).getBlock();
                if (world.getBlockState(infront).isReplaceable() && block.getDefaultState().canPlaceAt(world,infront)) {
                    BlockSoundGroup soundGroup = block.getDefaultState().getSoundGroup();
                    SoundEvent placeSound = soundGroup.getPlaceSound();
                    if (block.getStateManager().getProperties().contains(Properties.FACING)) {
                        world.setBlockState(infront, block.getDefaultState().with(Properties.FACING, state.get(FACING)));
                    } else if (block.getStateManager().getProperties().contains(Properties.ROTATION)) {
                        world.setBlockState(infront, block.getDefaultState().with(Properties.ROTATION, RotationPropertyHelper.fromDirection(state.get(FACING).getOpposite())));
                    } else {
                        world.setBlockState(infront, block.getDefaultState());
                    }

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
                DispenserBehavior dispenserBehavior = this.getBehaviorForItem(world, itemStack);
                if (dispenserBehavior != DispenserBehavior.NOOP) {
                    dispenserBlockEntity.setStack(i, dispenserBehavior.dispense(blockPointer, itemStack));
                }
            }


        }
    }
}