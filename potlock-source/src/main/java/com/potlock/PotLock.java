package com.potlock;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class PotLock implements ModInitializer {
    public static final String MOD_ID = "potlock";

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(PotLockNetworking.TOGGLE, PotLockNetworking::handleToggle);
    }
}
