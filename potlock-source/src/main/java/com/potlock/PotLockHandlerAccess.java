package com.potlock;

/** Interfaz que implementa (via mixin) el CookingPotItemHandler de Farmer's Delight. */
public interface PotLockHandlerAccess {
    void potlock$setOwner(PotLockAccess owner);
}
