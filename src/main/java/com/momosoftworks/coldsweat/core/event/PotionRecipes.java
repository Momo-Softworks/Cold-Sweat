package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.core.init.ModPotions;
import com.momosoftworks.coldsweat.util.item.PotionUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import javax.annotation.Nonnull;
import java.util.Arrays;

@EventBusSubscriber
public class PotionRecipes
{
    @SubscribeEvent
    public static void register(RegisterBrewingRecipesEvent event)
    {
        ItemStack awkward = createPotion(Potions.AWKWARD);
        ItemStack icePotion = createPotion(ModPotions.ICE_RESISTANCE);
        ItemStack longIcePotion = createPotion(ModPotions.LONG_ICE_RESISTANCE);

        event.getBuilder().addRecipe(new WorkingBrewingRecipe(Ingredient.of(awkward), Ingredient.of(ModItems.SOUL_SPROUT), icePotion));
        event.getBuilder().addRecipe(new WorkingBrewingRecipe(Ingredient.of(icePotion), Ingredient.of(Items.REDSTONE), longIcePotion));
    }

    private static ItemStack createPotion(Holder<Potion> potion)
    {   return PotionUtils.setPotion(Items.POTION.getDefaultInstance(), potion);
    }

    /**
     * A brewing recipe that actually checks item stack data for ingredients instead of just the item type.
     */
    public static class WorkingBrewingRecipe extends BrewingRecipe
    {
        Ingredient potionIn;
        Ingredient reagent;
        ItemStack output;

        public WorkingBrewingRecipe(Ingredient potionIn, Ingredient reagent, ItemStack output)
        {
            super(potionIn, reagent, output);
            this.potionIn = potionIn;
            this.reagent = reagent;
            this.output = output.copy();
        }

        @Override
        public boolean isInput(@Nonnull ItemStack potionIn)
        {
            if (potionIn == null)
            {   return false;
            }

            ItemStack[] matchingStacks = this.potionIn.getItems();

            if (matchingStacks.length == 0)
            {   return potionIn.isEmpty();
            }

            return Arrays.stream(matchingStacks).anyMatch(itemstack -> ItemStack.isSameItemSameComponents(itemstack, potionIn));
        }

        @Override
        public boolean isIngredient(ItemStack ingredient)
        {
            if (ingredient == null)
            {   return false;
            }

            ItemStack[] matchingStacks = this.reagent.getItems();

            if (matchingStacks.length == 0)
            {   return ingredient.isEmpty();
            }

            return Arrays.stream(matchingStacks).anyMatch(itemstack -> ItemStack.isSameItemSameComponents(itemstack, ingredient));
        }
    }
}
