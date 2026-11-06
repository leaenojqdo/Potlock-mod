package com.potlock;

import net.minecraft.world.item.Item;

/** Interfaz que implementa (via mixin) la CookingPotBlockEntity de Farmer's Delight. */
public interface PotLockAccess {
    int INPUT_SLOTS = 6;

    boolean potlock$isLocked();

    /** Item al que esta fijado el slot; Items.AIR significa "slot deshabilitado". */
    Item potlock$getFilter(int slot);

    void potlock$setLock(boolean locked, Item[] filters);

    /** Si la automatizacion puede meter este item en este slot de entrada. */
    boolean potlock$accepts(int slot, Item item);
}
