package com.exodus.core.mixin;

import com.exodus.core.player.inventory.PlayerInventoryComponent;
import com.exodus.core.player.inventory.PlayerInventoryManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerInventoryNbtMixin {

    @Inject(
            method = "addAdditionalSaveData",
            at = @At("TAIL")
    )
    private void saveInventory(CompoundTag nbt, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        PlayerInventoryComponent inventory = PlayerInventoryManager.getComponent(player);

        CompoundTag inventoryNbt = new CompoundTag();
        inventory.writeNbt(inventoryNbt);

        nbt.put("ExodusInventory", inventoryNbt);
    }

    @Inject(
            method = "readAdditionalSaveData",
            at = @At("TAIL")
    )
    private void loadInventory(CompoundTag nbt, CallbackInfo ci) {
        Player player = (Player)(Object)this;

        PlayerInventoryComponent inventory = PlayerInventoryManager.getComponent(player);

        CompoundTag inventoryNbt = nbt.getCompound("ExodusInventory");
        inventory.readNbt(inventoryNbt);
    }
}