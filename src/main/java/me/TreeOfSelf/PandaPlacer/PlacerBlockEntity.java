package me.TreeOfSelf.PandaPlacer;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;


public class PlacerBlockEntity extends DispenserBlockEntity {

    protected PlacerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(PandaPlacer.PLACER_BLOCK_ENTITY_TYPE, blockPos, blockState);
    }
    
    @Override
    protected Text getContainerName() {
        return Text.of("Placer");
    }

}
