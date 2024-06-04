package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.core.init.PotionInit;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Arrays;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PotionRecipes
{
    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            ItemStack awkward = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
            ItemStack icePotion = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), PotionInit.ICE_RESISTANCE.get());
            ItemStack longIcePotion = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), PotionInit.LONG_ICE_RESISTANCE.get());

            BrewingRecipeRegistry.addRecipe(new WorkingBrewingRecipe(Ingredient.of(awkward), Ingredient.of(ModItems.SOUL_SPROUT), icePotion));
            BrewingRecipeRegistry.addRecipe(new WorkingBrewingRecipe(Ingredient.of(icePotion), Ingredient.of(Items.REDSTONE), longIcePotion));
        });
    }

    /**
     * A brewing recipe that actually checks item stack data for ingredients instead of just the item type.
     */
    public static class WorkingBrewingRecipe implements IBrewingRecipe
    {
        Ingredient potionIn;
        Ingredient reagent;
        ItemStack output;

        public WorkingBrewingRecipe(Ingredient potionIn, Ingredient reagent, ItemStack output)
        {
            this.potionIn = potionIn;
            this.reagent = reagent;
            this.output = output.copy();
        }

        @Override
        public boolean isInput(ItemStack input)
        {   return Arrays.stream(potionIn.getItems()).anyMatch(ingredient -> ItemStack.isSameItemSameTags(ingredient, input));
        }

        @Override
        public boolean isIngredient(ItemStack ingredient)
        {   return Arrays.stream(reagent.getItems()).anyMatch(ing -> ItemStack.isSameItemSameTags(ing, ingredient));
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient)
        {
            return isInput(input) && isIngredient(ingredient)
                   ? output
                   : ItemStack.EMPTY;
        }
    }
}
