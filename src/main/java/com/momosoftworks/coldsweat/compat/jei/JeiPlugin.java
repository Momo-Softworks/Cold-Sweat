package com.momosoftworks.coldsweat.compat.jei;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.compat.jei.category.BoilerFuelCategory;
import com.momosoftworks.coldsweat.compat.jei.category.HearthFuelCategory;
import com.momosoftworks.coldsweat.compat.jei.category.IceboxFuelCategory;
import com.momosoftworks.coldsweat.compat.jei.category.SewingCategory;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.FuelData;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.serialization.RegistryHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(ModItems.HEARTH.getDefaultInstance(), HEARTH_RECIPE_CATEGORY);
        registration.addRecipeCatalyst(ModItems.BOILER.getDefaultInstance(), BOILER_RECIPE_CATEGORY);
        registration.addRecipeCatalyst(ModItems.ICEBOX.getDefaultInstance(), ICEBOX_RECIPE_CATEGORY);
        registration.addRecipeCatalyst(ModItems.SEWING_TABLE.getDefaultInstance(), SEWING_RECIPE_CATEGORY);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        registration.addRecipes(ConfigSettings.HEARTH_FUEL.get().values().stream().distinct().map(FuelRecipe::new).collect(Collectors.toList()), HEARTH_RECIPE_CATEGORY);
        registration.addRecipes(ConfigSettings.BOILER_FUEL.get().values().stream().distinct().map(FuelRecipe::new).collect(Collectors.toList()), BOILER_RECIPE_CATEGORY);
        registration.addRecipes(ConfigSettings.ICEBOX_FUEL.get().values().stream().distinct().map(FuelRecipe::new).collect(Collectors.toList()), ICEBOX_RECIPE_CATEGORY);
        registration.addRecipes(groupItemsByData(ConfigSettings.INSULATION_ITEMS.get())
                                .stream().map(group -> new SewingRecipe(group.getSecond(), group.getFirst())).collect(Collectors.toList()), SEWING_RECIPE_CATEGORY);
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
            groups.add(Pair.of(data, new ArrayList<>(Arrays.asList(stack))));
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

    public static final class FuelRecipe
    {
        private final List<ItemStack> inputs;
        private final FuelData fuelData;

        public FuelRecipe(List<ItemStack> inputs, FuelData fuelData)
        {   this.inputs = inputs;
            this.fuelData = fuelData;
        }

        public FuelRecipe(FuelData fuelData)
        {   this(RegistryHelper.mapTaggableList(fuelData.item().flatten(ItemRequirement::items)).stream().map(Item::getDefaultInstance).collect(Collectors.toList()), fuelData);
        }

        public List<ItemStack> inputs() { return inputs; }
        public FuelData fuelData() { return fuelData; }
        public double getFuel()
        {   return fuelData.fuel(inputs.get(0));
        }
    }

    public static final class SewingRecipe
    {
        private final List<ItemStack> inputs;
        private final InsulatorData insulatorData;

        public SewingRecipe(List<ItemStack> inputs, InsulatorData insulatorData)
        {   this.inputs = inputs;
            this.insulatorData = insulatorData;
        }

        public List<ItemStack> inputs() { return inputs; }
        public InsulatorData insulatorData() { return insulatorData; }
    }
}
