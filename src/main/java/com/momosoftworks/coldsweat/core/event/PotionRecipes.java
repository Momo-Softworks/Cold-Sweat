package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.core.init.ModPotions;
import com.momosoftworks.coldsweat.util.item.PotionUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import java.util.Arrays;

@EventBusSubscriber
public class PotionRecipes
{
    @SubscribeEvent
    public static void register(RegisterBrewingRecipesEvent event)
    {
        ItemStack awkward = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
        ItemStack icePotion = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), ModPotions.ICE_RESISTANCE);
        ItemStack longIcePotion = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), ModPotions.LONG_ICE_RESISTANCE);

        event.getBuilder().addRecipe(new WorkingBrewingRecipe(Ingredient.of(awkward), Ingredient.of(ModItems.SOUL_SPROUT), icePotion));
        event.getBuilder().addRecipe(new WorkingBrewingRecipe(Ingredient.of(icePotion), Ingredient.of(Items.REDSTONE), longIcePotion));
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
        {   return Arrays.stream(potionIn.getItems()).anyMatch(ingredient -> ItemStack.isSameItemSameComponents(ingredient, input));
        }

        @Override
        public boolean isIngredient(ItemStack ingredient)
        {   return Arrays.stream(reagent.getItems()).anyMatch(ing -> ItemStack.isSameItemSameComponents(ing, ingredient));
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
