package dev.momostudios.coldsweat.mixin;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.config.ConfigSettings;
import dev.momostudios.coldsweat.data.tags.ModBlockTags;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
public class MixinCampfire
{
    // Heat waterskins gradually
    @Inject(method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V",
            at = @At(value = "HEAD"), remap = ColdSweat.REMAP_MIXINS)
    private static void onItemCook(Level level, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity, CallbackInfo ci)
    {
        double waterskinStrength = ConfigSettings.WATERSKIN_STRENGTH.get();
        double tempRate = ConfigSettings.TEMP_RATE.get();
        for (int i = 0; i < blockEntity.getItems().size(); i++)
        {
            ItemStack stack = blockEntity.getItems().get(i);
            if (stack.is(ModItems.FILLED_WATERSKIN) && (level.getGameTime() & 4) == 0)
            {
                CompoundTag tag = stack.getOrCreateTag();
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
    @ModifyArg(method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"), index = 4, remap = ColdSweat.REMAP_MIXINS)
    private static ItemStack onItemFinishedCooking(Level level, double x, double y, double z, ItemStack result)
    {
        if (result.is(ModItems.FILLED_WATERSKIN))
        {
            double waterskinStrength = ConfigSettings.WATERSKIN_STRENGTH.get();
            CompoundTag tag = result.getOrCreateTag();
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
