package me.sebastian420.PandaPlacer;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

import java.util.Optional;

public class PlacerInventory extends SimpleInventory {

    private final PlacerBlockEntity placerBlockEntity;

    public PlacerInventory(PlacerBlockEntity placerBlockEntity, int size) {
        super(size);
        this.placerBlockEntity = placerBlockEntity;

    }

    @Override
    public void markDirty() {
        this.placerBlockEntity.markDirty();
        super.markDirty();
    }



}