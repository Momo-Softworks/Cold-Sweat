package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.core.init.PotionInit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class PotionRecipes
{
    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event)
    {
        event.enqueueWork(() ->
        {
            ItemStack awkward = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
            ItemStack packedIce = new ItemStack(Items.PRISMARINE_CRYSTALS);
            ItemStack iceResPotion = PotionUtils.setPotion(Items.POTION.getDefaultInstance(), PotionInit.ICE_RESISTANCE_POTION.get());
            BrewingRecipeRegistry.addRecipe(Ingredient.of(awkward), Ingredient.of(packedIce), iceResPotion);
        });
    }
}
