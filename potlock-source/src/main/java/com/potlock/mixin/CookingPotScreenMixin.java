package com.potlock.mixin;

import com.potlock.PotLockAccess;
import com.potlock.PotLockNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

@Mixin(CookingPotScreen.class)
public abstract class CookingPotScreenMixin extends AbstractContainerScreen<CookingPotMenu> {
    @Unique
    private Button potlock$button;

    // Constructor ficticio, requerido por el "extends"; nunca se ejecuta.
    protected CookingPotScreenMixin(CookingPotMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void potlock$addButton(CallbackInfo ci) {
        this.potlock$button = Button.builder(Component.translatable("potlock.button.lock"), b -> {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeBlockPos(this.menu.blockEntity.getBlockPos());
            ClientPlayNetworking.send(PotLockNetworking.TOGGLE, buf);
        }).bounds(this.leftPos + (this.imageWidth - 78) / 2, this.topPos + this.imageHeight + 4, 78, 20).build();
        this.potlock$button.setTooltip(Tooltip.create(Component.translatable("potlock.tooltip")));
        this.addRenderableWidget(this.potlock$button);
    }

    /** Mantiene el boton pegado a la GUI (el libro de recetas la desplaza) y actualiza su texto. */
    @Inject(method = "render", at = @At("HEAD"))
    private void potlock$updateButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.potlock$button == null) return;
        this.potlock$button.setX(this.leftPos + (this.imageWidth - this.potlock$button.getWidth()) / 2);
        this.potlock$button.setY(this.topPos + this.imageHeight + 4);
        boolean locked = ((PotLockAccess) (Object) this.menu.blockEntity).potlock$isLocked();
        this.potlock$button.setMessage(Component.translatable(locked ? "potlock.button.unlock" : "potlock.button.lock"));
    }

    /** Dibuja los slots deshabilitados oscurecidos y el item fijado como "fantasma" en los slots vacios. */
    @Inject(method = "renderBg", at = @At("TAIL"))
    private void potlock$drawLocks(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        PotLockAccess access = (PotLockAccess) (Object) this.menu.blockEntity;
        if (!access.potlock$isLocked()) return;
        for (int i = 0; i < PotLockAccess.INPUT_SLOTS; i++) {
            Slot slot = this.menu.slots.get(i);
            int x = this.leftPos + slot.x;
            int y = this.topPos + slot.y;
            Item filter = access.potlock$getFilter(i);
            if (filter == Items.AIR) {
                graphics.fill(x, y, x + 16, y + 16, 0xB0202020);
            } else if (!slot.hasItem()) {
                graphics.renderFakeItem(new ItemStack(filter), x, y);
                graphics.fill(x, y, x + 16, y + 16, 0x808B8B8B);
            }
        }
    }
}
