package dev.momostudios.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.registry.TempModifierRegistry;
import dev.momostudios.coldsweat.api.temperature.modifier.InsulationTempModifier;
import dev.momostudios.coldsweat.api.temperature.modifier.TempModifier;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.common.capability.ItemInsulationCap;
import dev.momostudios.coldsweat.common.capability.ModCapabilities;
import dev.momostudios.coldsweat.config.ItemSettingsConfig;
import dev.momostudios.coldsweat.util.compat.CompatManager;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ColdSweat.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorInsulation
{

    @SubscribeEvent
    public static void addLeatherModifiers(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level.isClientSide() && player.tickCount % 10 == 0)
        {
            Map<Item, Pair<Double, Double>> insulatingArmors = ConfigSettings.INSULATING_ARMORS.get();

            int fullyInsulated = 0;
            double cold = 0;
            double hot = 0;
            for (ItemStack armorStack : player.getArmorSlots())
            {
                if (armorStack.getItem() instanceof ArmorItem armorItem)
                {
                    // Add the armor's intrinsic insulation value (defined in configs)
                    // Mutually exclusive with Sewing Table insulation
                    Pair<Double, Double> insulationValue = insulatingArmors.get(armorStack.getItem());
                    if (insulationValue != null)
                    {
                        cold += insulationValue.getFirst();
                        hot += insulationValue.getSecond();
                    }
                    else
                    {   // Add the armor's insulation value from the Sewing Table
                        List<Pair<Double, Double>> insulation = armorStack.getCapability(ModCapabilities.ITEM_INSULATION).orElse(new ItemInsulationCap()).getInsulation();

                        // Get the armor's insulation values
                        for (Pair<Double, Double> value : insulation)
                        {
                            cold += value.getFirst();
                            hot += value.getSecond();
                        }

                        // Used for tracking "fully_insulated" advancement
                        if ((cold + hot) / 2 >= getInsulationSlots(armorStack))
                        {   fullyInsulated++;
                        }
                    }

                    // Add the armor's defense value to the insulation value.
                    cold += armorItem.getDefense();
                    hot += armorItem.getDefense();

                }
            }

            TempModifier currentMod = Temperature.getModifier(player, Temperature.Type.RATE, InsulationTempModifier.class);
            if (currentMod == null || currentMod.getNBT().getDouble("cold") != cold || currentMod.getNBT().getDouble("hot") != hot)
            {
                if (cold == 0 && hot == 0 && currentMod != null)
                    Temperature.removeModifiers(player, Temperature.Type.RATE, (mod) -> mod instanceof InsulationTempModifier);
                else
                    Temperature.replaceModifier(player, new InsulationTempModifier(cold, hot).tickRate(20), Temperature.Type.RATE);
            }

            // Armor underwear compat
            if (CompatManager.isArmorUnderwearLoaded())
            {
                Temperature.addModifier(player, TempModifierRegistry.getEntryFor("armorunder:lining").tickRate(20), Temperature.Type.MAX, false);
                Temperature.addModifier(player, TempModifierRegistry.getEntryFor("armorunder:lining").tickRate(20), Temperature.Type.MIN, false);
            }

            // Award advancement for full insulation
            if (fullyInsulated >= 4 && player instanceof ServerPlayer serverPlayer)
            {
                Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(new ResourceLocation("cold_sweat:full_insulation"));
                if (advancement != null)
                    serverPlayer.getAdvancements().award(advancement, "requirement");
            }
        }
    }

    public static Pair<Double, Double> getItemInsulation(ItemStack item)
    {
        return ConfigSettings.INSULATION_ITEMS.get().getOrDefault(item.getItem(), Pair.of(0d, 0d));
    }

    public static int getInsulationSlots(ItemStack item)
    {
        List<? extends Number> slots = ItemSettingsConfig.getInstance().insulationSlots();
        return switch (LivingEntity.getEquipmentSlotForItem(item))
        {
            case HEAD  -> slots.get(0).intValue();
            case CHEST -> slots.get(1).intValue();
            case LEGS  -> slots.get(2).intValue();
            case FEET  -> slots.get(3).intValue();
            default -> 0;
        };
    }
}
