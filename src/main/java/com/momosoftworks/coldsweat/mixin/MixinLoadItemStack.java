package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.event.core.MissingObjectEvent;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public class MixinLoadItemStack<T>
{
    @Inject(method = "parse(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Ljava/util/Optional;", at = @At("RETURN"),
            cancellable = true)
    private static void fixParsing(HolderLookup.Provider lookups, Tag tag, CallbackInfoReturnable<Optional<ItemStack>> cir)
    {
        if (cir.getReturnValue().isEmpty() && tag instanceof CompoundTag compound)
        {
            String key = compound.getString("id");
            MissingObjectEvent<Item> event = new MissingObjectEvent<>(ResourceLocation.parse(key));
            NeoForge.EVENT_BUS.post(event);
            if (event.getRemappedKey() != null)
            {
                compound.putString("id", event.getRemappedKey().toString());
                Optional<ItemStack> parsed = ItemStack.CODEC.parse(lookups.createSerializationContext(NbtOps.INSTANCE), compound)
                        .resultOrPartial(message -> ColdSweat.LOGGER.error("Tried to remap item {}, but failed: '{}'", key, message));
                cir.setReturnValue(parsed);
            }
        }
    }
}
