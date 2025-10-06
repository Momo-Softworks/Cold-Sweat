package com.momosoftworks.coldsweat.mixin;

import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
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
        double maxStrength = ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() * 0.6;
        double tempRate = ConfigSettings.TEMP_RATE.get();
        BlockState state = self.getBlockState();
        for (int i = 0; i < self.getItems().size(); i++)
        {
            ItemStack stack = self.getItems().get(i);
            if (stack.getItem() == ModItems.FILLED_WATERSKIN && (self.getLevel().getGameTime() & 4) == 0)
            {
                CompoundNBT tag = stack.getOrCreateTag();
                double temperature = tag.getDouble(FilledWaterskinItem.NBT_TEMPERATURE);

                // If the block ID contains "soul", it's a soul campfire
                if (state.is(BlockTags.CAMPFIRES) && CSMath.betweenExclusive(tag.getDouble(FilledWaterskinItem.NBT_TEMPERATURE), -maxStrength, maxStrength))
                {
                    double delta = tempRate * 0.1;
                    if (state.is(ModBlockTags.SOUL_FIRE) && ConfigSettings.COLD_SOUL_FIRE.get())
                    {   delta *= -1;
                    }
                    tag.putDouble(FilledWaterskinItem.NBT_TEMPERATURE, temperature + delta);
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
            double maxStrength = ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() * 0.6;
            CompoundNBT tag = result.getOrCreateTag();
            BlockState state = level.getBlockState(new BlockPos(x, y, z));

            if (state.is(BlockTags.CAMPFIRES))
            {
                double newTemp = maxStrength;
                if (state.is(ModBlockTags.SOUL_FIRE) && ConfigSettings.COLD_SOUL_FIRE.get())
                {   newTemp *= -1;
                }
                tag.putDouble(FilledWaterskinItem.NBT_TEMPERATURE, newTemp);
            }
        }
        return result;
    }
}
