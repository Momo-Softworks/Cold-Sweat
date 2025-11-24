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
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    public static final ResourceLocation HEARTH_RECIPE_CATEGORY = new ResourceLocation(ColdSweat.MOD_ID, "hearth");
    public static final ResourceLocation BOILER_RECIPE_CATEGORY = new ResourceLocation(ColdSweat.MOD_ID, "boiler");
    public static final ResourceLocation ICEBOX_RECIPE_CATEGORY = new ResourceLocation(ColdSweat.MOD_ID, "icebox");
    public static final ResourceLocation SEWING_RECIPE_CATEGORY = new ResourceLocation(ColdSweat.MOD_ID, "sewing");

    @Override
    public ResourceLocation getPluginUid()
    {   return ColdSweat.createKey("jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        ConfigSettings.HEARTH_FUEL.get().forEach((fuelItem, fuel) ->
        {   registration.addRecipes(Arrays.asList(new FuelRecipe(fuelItem.getDefaultInstance(), fuel)), HEARTH_RECIPE_CATEGORY);
        });
        ConfigSettings.BOILER_FUEL.get().forEach((fuelItem, fuel) ->
        {   registration.addRecipes(Arrays.asList(new FuelRecipe(fuelItem.getDefaultInstance(), fuel)), BOILER_RECIPE_CATEGORY);
        });
        ConfigSettings.ICEBOX_FUEL.get().forEach((fuelItem, fuel) ->
        {   registration.addRecipes(Arrays.asList(new FuelRecipe(fuelItem.getDefaultInstance(), fuel)), ICEBOX_RECIPE_CATEGORY);
        });
        ConfigSettings.INSULATION_ITEMS.get().forEach((item, insulator) ->
        {   registration.addRecipes(Arrays.asList(new SewingRecipe(item.getDefaultInstance(), insulator)), SEWING_RECIPE_CATEGORY);
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

    public static final class FuelRecipe
    {
        private final ItemStack input;
        private final FuelData fuelData;

        public FuelRecipe(ItemStack input, FuelData fuelData)
        {   this.input = input;
            this.fuelData = fuelData;
        }
        public ItemStack input() { return input; }
        public FuelData fuelData() { return fuelData; }
    }

    public static final class SewingRecipe
    {
        private final ItemStack input;
        private final InsulatorData insulatorData;

        public SewingRecipe(ItemStack input, InsulatorData insulatorData)
        {   this.input = input;
            this.insulatorData = insulatorData;
        }

        public ItemStack input() { return input; }
        public InsulatorData insulatorData() { return insulatorData; }
    }
}
