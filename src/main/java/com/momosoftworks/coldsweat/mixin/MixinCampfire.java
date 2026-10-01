package com.momosoftworks.coldsweat.mixin;

import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.server.level.ServerLevel;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.data.tag.ModBlockTags;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
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
    /**
     * Heat waterskins gradually
     */
    @Inject(method = "cookTick", at = @At(value = "HEAD"))
    private static void onItemCook(ServerLevel level, BlockPos pos, BlockState state, CampfireBlockEntity blockEntity,
                                   RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> recipeCache, CallbackInfo ci)
    {
        double maxStrength = ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() * 0.6;
        double tempRate = ConfigSettings.TEMP_RATE.get();

        for (int i = 0; i < blockEntity.getItems().size(); i++)
        {
            ItemStack stack = blockEntity.getItems().get(i);
            if (stack.is(ModItems.FILLED_WATERSKIN) && (level.getGameTime() & 4) == 0)
            {
                double temperature = stack.getOrDefault(ModItemComponents.WATER_TEMPERATURE, 0.0);

                // If the block ID contains "soul", it's a soul campfire
                if (state.is(BlockTags.CAMPFIRES) && CSMath.betweenExclusive(temperature, -maxStrength, maxStrength))
                {
                    double delta = tempRate * 0.1;
                    if (state.is(ModBlockTags.SOUL_FIRE) && ConfigSettings.COLD_SOUL_FIRE.get())
                    {   delta *= -1;
                    }
                    stack.set(ModItemComponents.WATER_TEMPERATURE, temperature + delta);
                }
            }
        }
    }

    /**
     * Ensure waterskin temperature is not reset when cooking finishes
     */
    @ModifyArg(method = "cookTick",
               at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"), index = 4)
    private static ItemStack onItemFinishedCooking(Level level, double x, double y, double z, ItemStack result)
    {
        if (result.is(ModItems.FILLED_WATERSKIN))
        {
            double maxStrength = ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() * 0.6;
            BlockState state = level.getBlockState(BlockPos.containing(x, y, z));

            if (state.is(BlockTags.CAMPFIRES))
            {
                double newTemp = maxStrength;
                if (state.is(ModBlockTags.SOUL_FIRE) && ConfigSettings.COLD_SOUL_FIRE.get())
                {   newTemp *= -1;
                }
                result.set(ModItemComponents.WATER_TEMPERATURE, newTemp);
            }
        }
        return result;
    }
}
