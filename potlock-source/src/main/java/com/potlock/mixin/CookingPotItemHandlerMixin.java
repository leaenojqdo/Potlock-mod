package com.potlock.mixin;

import com.potlock.PotLockAccess;
import com.potlock.PotLockHandlerAccess;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.inventory.CookingPotItemHandler;
import vectorwing.farmersdelight.refabricated.inventory.ItemHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * El CookingPotItemHandler es la "vista" de la olla que ven las tolvas y demas automatizacion.
 * Aqui se restringe en que slot de entrada puede entrar cada item cuando la olla esta bloqueada.
 */
@Mixin(value = CookingPotItemHandler.class, remap = false)
public abstract class CookingPotItemHandlerMixin implements PotLockHandlerAccess {
    @Shadow
    private ItemHandler itemHandler;
    @Shadow
    private Direction side;

    @Unique
    private PotLockAccess potlock$owner;

    @Override
    public void potlock$setOwner(PotLockAccess owner) {
        this.potlock$owner = owner;
    }

    /** Esta vista apunta a los slots de entrada (0-5)? (en FD: lado null o arriba) */
    @Unique
    private boolean potlock$restricted() {
        return this.potlock$owner != null && this.potlock$owner.potlock$isLocked()
                && (this.side == null || this.side == Direction.UP);
    }

    /** API vieja (insertItem): usada por otros mods y por algunas rutas internas. */
    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void potlock$insertItem(int slot, ItemStack stack, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        if (slot >= 0 && slot < PotLockAccess.INPUT_SLOTS && potlock$restricted()
                && !this.potlock$owner.potlock$accepts(slot, stack.getItem())) {
            cir.setReturnValue(stack);
        }
    }

    /**
     * API de transferencia de Fabric: es la que usan las tolvas. Originalmente devuelve TODOS los slots
     * de entrada vacios o con el mismo item (por eso se desparrama). Con la olla bloqueada devolvemos solo
     * los slots cuyo filtro coincide con el item.
     */
    @Inject(method = "getInsertableSlotsFor", at = @At("HEAD"), cancellable = true)
    private void potlock$insertableSlots(ItemVariant variant, CallbackInfoReturnable<Iterator<SingleStackStorage>> cir) {
        if (!potlock$restricted()) return;
        List<SingleStackStorage> result = new ArrayList<>();
        List<SingleSlotStorage<ItemVariant>> all = this.itemHandler.getSlots();
        for (int i = 0; i < PotLockAccess.INPUT_SLOTS && i < all.size(); i++) {
            if (!this.potlock$owner.potlock$accepts(i, variant.getItem())) continue;
            SingleSlotStorage<ItemVariant> slot = all.get(i);
            if (slot.isResourceBlank() || slot.getResource().equals(variant)) {
                result.add((SingleStackStorage) (Object) slot);
            }
        }
        cir.setReturnValue(result.iterator());
    }
}
