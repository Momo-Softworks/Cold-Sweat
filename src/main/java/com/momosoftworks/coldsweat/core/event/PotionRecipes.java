package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.core.init.ModPotions;
import com.momosoftworks.coldsweat.util.item.PotionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

import javax.annotation.Nonnull;
import java.util.Arrays;

@EventBusSubscriber
public class PotionRecipes
{
    @SubscribeEvent
    public static void register(RegisterBrewingRecipesEvent event)
    {
        Ingredient awkward = createPotion(Potions.AWKWARD);
        Ingredient icePotionIngredient = createPotion(ModPotions.ICE_RESISTANCE);
        ItemStack icePotion = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), ModPotions.ICE_RESISTANCE);
        ItemStack longIcePotion = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), ModPotions.LONG_ICE_RESISTANCE);

        event.getBuilder().addRecipe(new BrewingRecipe(awkward, Ingredient.of(ModItems.SOUL_SPROUT), icePotion));
        event.getBuilder().addRecipe(new BrewingRecipe(icePotionIngredient, Ingredient.of(Items.REDSTONE), longIcePotion));
    }

    private static Ingredient createPotion(Holder<Potion> potion)
    {
        DataComponentIngredient ingredientComponents = new DataComponentIngredient(
                                                          HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.POTION)),
                                                          DataComponentPredicate.builder().expect(DataComponents.POTION_CONTENTS, new PotionContents(potion)).build(),
                                                          false);
        return new Ingredient(ingredientComponents);
    }
}
