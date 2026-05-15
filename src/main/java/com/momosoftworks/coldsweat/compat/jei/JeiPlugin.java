package com.momosoftworks.coldsweat.compat.jei;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.jei.category.BoilerFuelCategory;
import com.momosoftworks.coldsweat.compat.jei.category.HearthFuelCategory;
import com.momosoftworks.coldsweat.compat.jei.category.IceboxFuelCategory;
import com.momosoftworks.coldsweat.compat.jei.category.SewingCategory;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    public static final RecipeType<FuelRecipe> HEARTH_RECIPE_TYPE = RecipeType.create(ColdSweat.MOD_ID, "hearth", FuelRecipe.class);
    public static final RecipeType<FuelRecipe> BOILER_RECIPE_TYPE = RecipeType.create(ColdSweat.MOD_ID, "boiler", FuelRecipe.class);
    public static final RecipeType<FuelRecipe> ICEBOX_RECIPE_TYPE = RecipeType.create(ColdSweat.MOD_ID, "icebox", FuelRecipe.class);
    public static final RecipeType<SewingRecipe> SEWING_RECIPE_TYPE = RecipeType.create(ColdSweat.MOD_ID, "sewing", SewingRecipe.class);

    @Override
    public ResourceLocation getPluginUid()
    {   return ColdSweat.createKey("jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        ConfigSettings.HEARTH_FUEL.get().forEach((fuelItem, fuel) ->
        {
            registration.addRecipes(HEARTH_RECIPE_TYPE, List.of(new FuelRecipe(fuelItem.getDefaultInstance(), fuel)));
        });
        ConfigSettings.BOILER_FUEL.get().forEach((fuelItem, fuel) ->
        {
            registration.addRecipes(BOILER_RECIPE_TYPE, List.of(new FuelRecipe(fuelItem.getDefaultInstance(), fuel)));
        });
        ConfigSettings.ICEBOX_FUEL.get().forEach((fuelItem, fuel) ->
        {
            registration.addRecipes(ICEBOX_RECIPE_TYPE, List.of(new FuelRecipe(fuelItem.getDefaultInstance(), fuel)));
        });
        ConfigSettings.INSULATION_ITEMS.get().forEach((item, insulator) -> {
            registration.addRecipes(SEWING_RECIPE_TYPE, List.of(new SewingRecipe(item.getDefaultInstance(), insulator)));
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        registration.addRecipeCategories(new HearthFuelCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BoilerFuelCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new IceboxFuelCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SewingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    public record FuelRecipe(ItemStack input, FuelData fuelData)
    {
        public double getFuel()
        {   return fuelData.fuel(input);
        }
    }

    public record SewingRecipe(ItemStack input, InsulatorData insulatorData) {}
}
