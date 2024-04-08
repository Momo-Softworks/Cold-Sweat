package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireTileEntity.class)
public class MixinCampfire
{
    CampfireTileEntity self = (CampfireTileEntity)(Object)this;

    // Heat waterskins gradually
    @Inject(method = "cook",
            at = @At(value = "HEAD"))
    private void onItemCook(CallbackInfo ci)
    {
        double waterskinStrength = ConfigSettings.WATERSKIN_STRENGTH.get();
        double tempRate = ConfigSettings.TEMP_RATE.get();
        BlockState state = self.getBlockState();
        for (int i = 0; i < self.getItems().size(); i++)
        {
            ItemStack stack = self.getItems().get(i);
            if (stack.getItem() == ModItems.FILLED_WATERSKIN && (self.getLevel().getGameTime() & 4) == 0)
            {
                CompoundNBT tag = stack.getOrCreateTag();
                double temperature = tag.getDouble("temperature");
                if (state.is(ModBlockTags.SOUL_CAMPFIRES) && tag.getDouble("temperature") > -waterskinStrength * 0.6)
                {   tag.putDouble("temperature", temperature + tempRate * 0.1 * (ConfigSettings.COLD_SOUL_FIRE.get() ? -1 : 1));
                }
                else if (state.is(ModBlockTags.CAMPFIRES) && tag.getDouble("temperature") < waterskinStrength * 0.6)
                {   tag.putDouble("temperature", temperature + tempRate * 0.1);
                }
            }
        }
    }

    // Ensure waterskin temperature is not reset when cooking finishes
    @ModifyArg(method = "cook",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/InventoryHelper;dropItemStack(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)V"),
               index = 4)
    private ItemStack onItemFinishedCooking(World level, double x, double y, double z, ItemStack result)
    {
        if (result.getItem() == ModItems.FILLED_WATERSKIN)
        {
            double waterskinStrength = ConfigSettings.WATERSKIN_STRENGTH.get();
            CompoundNBT tag = result.getOrCreateTag();
            BlockState state = level.getBlockState(new BlockPos(x, y, z));
            if (state.is(ModBlockTags.SOUL_CAMPFIRES))
            {   tag.putDouble("temperature", waterskinStrength * 0.6 * (ConfigSettings.COLD_SOUL_FIRE.get() ? -1 : 1));
            }
            else if (state.is(ModBlockTags.CAMPFIRES))
            {   tag.putDouble("temperature", waterskinStrength * 0.6);
            }
        }
        return result;
    }
}
