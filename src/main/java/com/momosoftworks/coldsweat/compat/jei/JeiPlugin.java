package com.momosoftworks.coldsweat.compat.jei;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.jei.category.BoilerFuelCategory;
import com.momosoftworks.coldsweat.compat.jei.category.HearthFuelCategory;
import com.momosoftworks.coldsweat.compat.jei.category.IceboxFuelCategory;
import com.momosoftworks.coldsweat.compat.jei.category.SewingCategory;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModBlocks;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    public static final IRecipeType<FuelRecipe> HEARTH_RECIPE_TYPE = IRecipeType.create(ColdSweat.MOD_ID, "hearth", FuelRecipe.class);
    public static final IRecipeType<FuelRecipe> BOILER_RECIPE_TYPE = IRecipeType.create(ColdSweat.MOD_ID, "boiler", FuelRecipe.class);
    public static final IRecipeType<FuelRecipe> ICEBOX_RECIPE_TYPE = IRecipeType.create(ColdSweat.MOD_ID, "icebox", FuelRecipe.class);
    public static final IRecipeType<SewingRecipe> SEWING_RECIPE_TYPE = IRecipeType.create(ColdSweat.MOD_ID, "sewing", SewingRecipe.class);

    @Override
    public Identifier getPluginUid()
    {   return ColdSweat.createKey("jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addCraftingStation(HEARTH_RECIPE_TYPE, ModItems.HEARTH);
        registration.addCraftingStation(BOILER_RECIPE_TYPE, ModItems.BOILER);
        registration.addCraftingStation(ICEBOX_RECIPE_TYPE, ModItems.ICEBOX);
        registration.addCraftingStation(SEWING_RECIPE_TYPE, ModItems.SEWING_TABLE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        registration.addRecipes(HEARTH_RECIPE_TYPE, ConfigSettings.HEARTH_FUEL.get().values().stream().distinct().map(FuelRecipe::new).toList());
        registration.addRecipes(BOILER_RECIPE_TYPE, ConfigSettings.BOILER_FUEL.get().values().stream().distinct().map(FuelRecipe::new).toList());
        registration.addRecipes(ICEBOX_RECIPE_TYPE, ConfigSettings.ICEBOX_FUEL.get().values().stream().distinct().map(FuelRecipe::new).toList());
        registration.addRecipes(SEWING_RECIPE_TYPE, groupItemsByData(ConfigSettings.INSULATION_ITEMS.get())
                                                    .stream().map(group -> new SewingRecipe(group.getSecond(), group.getFirst())).toList());
    }

    /**
     * Groups items that share the same config entry so they can be shown as one recipe
     */
    private static <T> List<Pair<T, List<ItemStack>>> groupItemsByData(Multimap<Item, T> dataMap)
    {
        List<Pair<T, List<ItemStack>>> groups = new ArrayList<>();
        dataMap.forEach((item, data) ->
        {
            ItemStack stack = item.getDefaultInstance();
            for (int i = 0; i < groups.size(); i++)
            {
                Pair<T, List<ItemStack>> group = groups.get(i);
                if (group.getFirst().equals(data))
                {   group.getSecond().add(stack);
                    return;
                }
            }
            groups.add(Pair.of(data, new ArrayList<>(List.of(stack))));
        });
        return groups;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        registration.addRecipeCategories(new HearthFuelCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BoilerFuelCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new IceboxFuelCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SewingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    public record FuelRecipe(List<ItemStack> inputs, FuelData fuelData)
    {
        public FuelRecipe(FuelData fuelData)
        {   this(RegistryHelper.mapBuiltinRegistryTagList(BuiltInRegistries.ITEM, fuelData.item().flatten(ItemRequirement::items)).stream().map(Item::getDefaultInstance).toList(), fuelData);
        }

        public double getFuel()
        {   return fuelData.fuel(inputs.get(0));
        }
    }

    public record SewingRecipe(List<ItemStack> inputs, InsulatorData insulatorData) {}
}
