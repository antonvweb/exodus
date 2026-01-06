package com.exodus.core.api.player.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class InventorySlot {
    private ItemStack itemStack = ItemStack.EMPTY;

    public boolean isEmpty(){
        return itemStack.isEmpty();
    }

    public ItemStack getItemStack(){
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void clear(){
        itemStack = ItemStack.EMPTY;
    }

    public CompoundTag writeNbt(CompoundTag nbt){
        return itemStack.save(nbt);
    }

    public void readNbt(CompoundTag nbt) {
        this.itemStack = ItemStack.of(nbt);
    }
}
