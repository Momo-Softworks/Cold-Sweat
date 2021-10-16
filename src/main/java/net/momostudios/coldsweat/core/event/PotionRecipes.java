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
import net.momostudios.coldsweat.core.init.PotionInit;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PotionRecipes
{
    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            BrewingRecipeRegistry.addRecipe(new IcePotionRecipe());
        });
    }

    public static class IcePotionRecipe implements IBrewingRecipe
    {

        @Override
        public boolean isInput(ItemStack input) {
            return input.getItem() == Items.POTION && PotionUtils.getPotionFromItem(input) == Potions.AWKWARD;
        }

        @Override
        public boolean isIngredient(ItemStack ingredient) {
            return ingredient.getItem() == Items.PACKED_ICE;
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
            return this.isInput(input) && this.isIngredient(ingredient) ?
                    PotionUtils.addPotionToItemStack(Items.POTION.getDefaultInstance(), PotionInit.ICE_RESISTANCE_POTION.get()) : ItemStack.EMPTY;
        }
    }
}
