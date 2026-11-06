package com.potlock;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

public final class PotLockNetworking {
    public static final ResourceLocation TOGGLE = new ResourceLocation(PotLock.MOD_ID, "toggle_lock");

    private PotLockNetworking() {
    }

    public static void handleToggle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                                    FriendlyByteBuf buf, PacketSender responseSender) {
        final BlockPos pos = buf.readBlockPos();
        server.execute(() -> {
            // Solo si el jugador tiene abierta justamente esa olla (evita que un cliente modificado toque ollas lejanas)
            if (!(player.containerMenu instanceof CookingPotMenu menu)) return;
            CookingPotBlockEntity be = menu.blockEntity;
            if (be == null || !be.getBlockPos().equals(pos) || !menu.stillValid(player)) return;

            PotLockAccess access = (PotLockAccess) (Object) be;
            if (access.potlock$isLocked()) {
                access.potlock$setLock(false, null);
            } else {
                Item[] filters = new Item[PotLockAccess.INPUT_SLOTS];
                boolean any = false;
                for (int i = 0; i < filters.length; i++) {
                    Item item = be.getInventory().getStackInSlot(i).getItem();
                    filters[i] = item;
                    if (item != Items.AIR) any = true;
                }
                if (!any) {
                    player.displayClientMessage(Component.translatable("potlock.message.empty"), true);
                    return;
                }
                access.potlock$setLock(true, filters);
            }

            be.setChanged();
            Level level = be.getLevel();
            if (level != null) {
                BlockState state = be.getBlockState();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        });
    }
}
