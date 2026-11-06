package com.potlock.mixin;

import com.potlock.PotLockAccess;
import com.potlock.PotLockHandlerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.inventory.CookingPotItemHandler;

import java.util.Arrays;

@Mixin(CookingPotBlockEntity.class)
public abstract class CookingPotBlockEntityMixin extends BlockEntity implements PotLockAccess {
    private static final String KEY_LOCKED = "PotLocked";
    private static final String KEY_FILTERS = "PotLockFilters";

    @Shadow(remap = false)
    private CookingPotItemHandler inputHandler;
    @Shadow(remap = false)
    private CookingPotItemHandler outputHandler;

    @Unique
    private boolean potlock$locked;
    @Unique
    private Item[] potlock$filters;

    // Constructor ficticio, requerido por el "extends"; nunca se ejecuta.
    protected CookingPotBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void potlock$init(BlockPos pos, BlockState state, CallbackInfo ci) {
        this.potlock$filters = new Item[INPUT_SLOTS];
        Arrays.fill(this.potlock$filters, Items.AIR);
        ((PotLockHandlerAccess) (Object) this.inputHandler).potlock$setOwner(this);
        ((PotLockHandlerAccess) (Object) this.outputHandler).potlock$setOwner(this);
    }

    @Override
    public boolean potlock$isLocked() {
        return this.potlock$locked;
    }

    @Override
    public Item potlock$getFilter(int slot) {
        return slot >= 0 && slot < INPUT_SLOTS ? this.potlock$filters[slot] : Items.AIR;
    }

    @Override
    public void potlock$setLock(boolean locked, Item[] filters) {
        this.potlock$locked = locked;
        Arrays.fill(this.potlock$filters, Items.AIR);
        if (locked && filters != null) {
            System.arraycopy(filters, 0, this.potlock$filters, 0, Math.min(filters.length, INPUT_SLOTS));
        }
    }

    @Override
    public boolean potlock$accepts(int slot, Item item) {
        if (!this.potlock$locked) return true;
        Item filter = potlock$getFilter(slot);
        return filter != Items.AIR && filter == item;
    }

    @Unique
    private void potlock$write(CompoundTag tag) {
        tag.putBoolean(KEY_LOCKED, this.potlock$locked);
        ListTag list = new ListTag();
        for (Item item : this.potlock$filters) {
            list.add(StringTag.valueOf(BuiltInRegistries.ITEM.getKey(item).toString()));
        }
        tag.put(KEY_FILTERS, list);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void potlock$save(CompoundTag tag, CallbackInfo ci) {
        potlock$write(tag);
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void potlock$updateTag(CallbackInfoReturnable<CompoundTag> cir) {
        potlock$write(cir.getReturnValue());
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void potlock$load(CompoundTag tag, CallbackInfo ci) {
        this.potlock$locked = tag.getBoolean(KEY_LOCKED);
        Arrays.fill(this.potlock$filters, Items.AIR);
        ListTag list = tag.getList(KEY_FILTERS, Tag.TAG_STRING);
        for (int i = 0; i < Math.min(list.size(), INPUT_SLOTS); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            this.potlock$filters[i] = id == null ? Items.AIR : BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        }
    }
}
