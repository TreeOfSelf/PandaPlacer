package me.sebastian420.PandaPlacer;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;


import static me.sebastian420.PandaPlacer.PandaPlacer.PLACER_BLOCK_ENTITY_TYPE;


public class PlacerBlockEntity extends DispenserBlockEntity {

    protected PlacerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(PLACER_BLOCK_ENTITY_TYPE, blockPos, blockState);
    }
    
    @Override
    protected Text getContainerName() {
        return Text.of("Placer");
    }

}
