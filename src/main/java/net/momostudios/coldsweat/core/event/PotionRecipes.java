package net.momostudios.coldsweat.core.event;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.*;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent;
import net.momostudios.coldsweat.core.init.PotionInit;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PotionRecipes
{
    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            ItemStack awkward = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.AWKWARD);
            ItemStack packedIce = new ItemStack(Items.PACKED_ICE);
            ItemStack iceResPotion = PotionUtils.addPotionToItemStack(Items.POTION.getDefaultInstance(), PotionInit.ICE_RESISTANCE_POTION.get());
            BrewingRecipeRegistry.addRecipe(Ingredient.fromStacks(awkward), Ingredient.fromStacks(packedIce), iceResPotion);
        });
    }
}
