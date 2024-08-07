package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.temperature.modifier.ArmorInsulationTempModifier;
import com.momosoftworks.coldsweat.api.util.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.common.item.component.ArmorInsulation;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.core.init.ModItems;
import com.momosoftworks.coldsweat.util.compat.CompatManager;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public class ProcessEquipmentInsulation
{
    @SubscribeEvent
    public static void applyArmorInsulation(PlayerTickEvent.Post event)
    {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer
        && player.tickCount % 20 == 0 && !player.level().isClientSide)
        {
            int fullyInsulatedSlots = 0;
            double cold = 0;
            double heat = 0;

            double worldTemp = Temperature.get(player, Temperature.Trait.WORLD);
            double minTemp = Temperature.get(player, Temperature.Trait.FREEZING_POINT);
            double maxTemp = Temperature.get(player, Temperature.Trait.BURNING_POINT);

            for (ItemStack armorStack : player.getArmorSlots())
            {
                if (armorStack.getItem() instanceof Equipable)
                {
                    // Add the armor's intrinsic insulation value (defined in configs)
                    // Mutually exclusive with Sewing Table insulation
                    Insulator armorInsulator = ConfigSettings.INSULATING_ARMORS.get().get(armorStack.getItem());
                    if (armorInsulator != null)
                    {
                        // Check if the player meets the predicate for the insulation
                        if (!armorInsulator.predicate().test(serverPlayer) || !armorInsulator.data().test(armorStack, true))
                        {   continue;
                        }
                        cold += armorInsulator.insulation().getCold();
                        heat += armorInsulator.insulation().getHeat();
                    }
                    else
                    {   // Add the armor's insulation value from the Sewing Table
                        Optional<ArmorInsulation> icap = ItemInsulationManager.getInsulationCap(armorStack);
                        if (icap.isEmpty())
                        {   continue;
                        }
                        ArmorInsulation cap = icap.get();
                        List<Insulation> insulation = cap.getInsulation()
                                                      .stream()
                                                      .filter(pair -> CSMath.getIfNotNull(ConfigSettings.INSULATION_ITEMS.get().get(pair.getFirst().getItem()),
                                                                                          insulator -> insulator.test(serverPlayer, pair.getFirst()),
                                                                                          false))
                                                      .map(pair -> pair.getSecond())
                                                      .flatMap(List::stream).toList();

                        // Get the armor's insulation values
                        for (Insulation value : insulation)
                        {
                            if (value instanceof StaticInsulation insul)
                            {   cold += insul.getCold();
                                heat += insul.getHeat();
                            }
                            else if (value instanceof AdaptiveInsulation insul)
                            {   cold += CSMath.blend(insul.getInsulation() * 0.75, 0, insul.getFactor(), -1, 1);
                                heat += CSMath.blend(0, insul.getInsulation() * 0.75, insul.getFactor(), -1, 1);
                            }
                        }

                        // Used for tracking "fully_insulated" advancement
                        if ((cold + heat) / 2 >= ItemInsulationManager.getInsulationSlots(armorStack))
                        {   fullyInsulatedSlots++;
                        }

                        cap = cap.calcAdaptiveInsulation(worldTemp, minTemp, maxTemp);
                        armorStack.set(ModItemComponents.ARMOR_INSULATION, cap);
                    }

                    // Add the armor's defense value to the insulation value.
                    double armorAmount = armorStack.getAttributeModifiers().modifiers()
                                         .stream().filter(entry -> entry.attribute() == Attributes.ARMOR && entry.slot() == EquipmentSlotGroup.ARMOR)
                                         .map(entry -> entry.modifier().amount())
                                         .mapToDouble(Double::doubleValue).sum();
                    cold += Math.min(armorAmount, 20);
                    heat += Math.min(armorAmount, 20);

                }
            }

            /* Get insulation from curios */

            for (ItemStack stack : CompatManager.getCurios(player))
            {
                Insulator insulator = ConfigSettings.INSULATING_CURIOS.get().get(stack.getItem());
                if (insulator != null && insulator.test(player, stack))
                {
                    cold += insulator.insulation().getCold();
                    heat += insulator.insulation().getHeat();
                }
            }

            Temperature.addOrReplaceModifier(player, new ArmorInsulationTempModifier(cold, heat).tickRate(20).expires(20), Temperature.Trait.RATE, Placement.Duplicates.BY_CLASS);

            // Award advancement for full insulation
            if (fullyInsulatedSlots >= 4)
            {
                if (serverPlayer.getServer() != null)
                {
                    AdvancementHolder advancement = serverPlayer.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "full_insulation"));
                    if (advancement != null)
                    {   serverPlayer.getAdvancements().award(advancement, "requirement");
                    }
                }
            }
        }
    }

    /**
     * Prevent damage by magma blocks if the player has hoglin hooves
     */
    @SubscribeEvent
    public static void onDamageTaken(LivingIncomingDamageEvent event)
    {
        DamageSource source = event.getSource();
        if (source == event.getEntity().level().damageSources().hotFloor() && event.getEntity().getItemBySlot(EquipmentSlot.FEET).is(ModItems.HOGLIN_HOOVES))
        {   event.setCanceled(true);
        }
    }
}
