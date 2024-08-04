package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.core.init.PotionInit;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import javax.annotation.Nonnull;
import java.util.Arrays;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PotionRecipes
{
    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event)
    {
        ItemStack awkward = createPotion(Potions.AWKWARD);
        ItemStack icePotion = createPotion(PotionInit.ICE_RESISTANCE.get());
        ItemStack longIcePotion = createPotion(PotionInit.ICE_RESISTANCE_LONG.get());

        BrewingRecipeRegistry.addRecipe(new WorkingBrewingRecipe(Ingredient.of(awkward), Ingredient.of(ModItems.SOUL_SPROUT), icePotion));
        BrewingRecipeRegistry.addRecipe(new WorkingBrewingRecipe(Ingredient.of(icePotion), Ingredient.of(Items.REDSTONE), longIcePotion));
    }

    private static ItemStack createPotion(Potion potion)
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

            return Arrays.stream(matchingStacks).anyMatch(itemstack -> ItemStack.matches(itemstack, potionIn));
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

            return Arrays.stream(matchingStacks).anyMatch(itemstack -> ItemStack.matches(itemstack, ingredient));
        }
    }
}
