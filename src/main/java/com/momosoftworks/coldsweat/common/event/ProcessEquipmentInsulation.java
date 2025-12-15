package com.momosoftworks.coldsweat.common.event;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.api.event.common.insulation.InsulationTickEvent;
import com.momosoftworks.coldsweat.api.event.vanilla.ItemBreakEvent;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.temperature.modifier.ArmorInsulationTempModifier;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.insulation.IInsulatableCap;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.compat.CompatManager;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.util.math.FastMap;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber
public class ProcessEquipmentInsulation
{
    @SubscribeEvent
    public static void applyArmorInsulation(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (event.phase == TickEvent.Phase.END && player instanceof ServerPlayer serverPlayer
        && player.tickCount % 20 == 0 && !player.level.isClientSide)
        {
            AtomicInteger fullyInsulatedSlots = new AtomicInteger(0);
            Map<String, Double> armorInsulation = new FastMap<>();

            double worldTemp = Temperature.get(player, Temperature.Trait.WORLD);
            double minTemp = Temperature.get(player, Temperature.Trait.FREEZING_POINT);
            double maxTemp = Temperature.get(player, Temperature.Trait.BURNING_POINT);

            for (ItemStack armorStack : player.getArmorSlots())
            {
                if (armorStack.getItem() instanceof ArmorItem)
                {
                    // Add the armor's built-in insulation value
                    applyBuiltinArmorInsulation(armorInsulation, armorStack, player, worldTemp, minTemp, maxTemp);
                    // Add the armor's insulation value from the Sewing Table
                    applySewnArmorInsulation(armorInsulation, armorStack, player, worldTemp, minTemp, maxTemp, fullyInsulatedSlots);

                    // Add the armor's defense value to the insulation value.
                    double armorAmount = armorStack.getAttributeModifiers(LivingEntity.getEquipmentSlotForItem(armorStack)).entries()
                                         .stream().filter(entry -> entry.getKey().equals(Attributes.ARMOR))
                                         .map(entry -> entry.getValue().getAmount())
                                         .mapToDouble(Double::doubleValue).sum();
                    mapAdd(armorInsulation, "cold_protection", Math.min(armorAmount, 20));
                    mapAdd(armorInsulation, "heat_protection", Math.min(armorAmount, 20));
                }
            }

            // Get insulation from curios
            for (ItemStack curio : CompatManager.Curios.getCurios(player))
            {
                for (InsulatorData insulator : ConfigSettings.INSULATING_CURIOS.get().get(curio.getItem()))
                {
                    if (insulator.test(player, curio))
                    {
                        mapAdd(armorInsulation, "cold_curios", insulator.getCold());
                        mapAdd(armorInsulation, "heat_curios", insulator.getHeat());
                    }
                }
            }

            // Post insulation event
            InsulationTickEvent insulationEvent = new InsulationTickEvent(player, armorInsulation);
            MinecraftForge.EVENT_BUS.post(insulationEvent);
            // Apply final insulation TempModifier
            if (!insulationEvent.isCanceled())
            {
                double cold = insulationEvent.getProperty("cold");
                double heat = insulationEvent.getProperty("heat");

                if (cold > 0 || heat > 0)
                {   Temperature.replaceOrAddModifier(player, new ArmorInsulationTempModifier(cold, heat).tickRate(20).expires(20), Temperature.Trait.RATE, Matcher.SAME_CLASS);
                }
            }

            // Award advancement for full insulation
            if (fullyInsulatedSlots.get() >= 4)
            {
                if (serverPlayer.getServer() != null)
                {
                    Advancement advancement = serverPlayer.getServer().getAdvancements().getAdvancement(new ResourceLocation("cold_sweat:full_insulation"));
                    if (advancement != null)
                    {   serverPlayer.getAdvancements().award(advancement, "requirement");
                    }
                }
            }
        }
    }

    private static void applyBuiltinArmorInsulation(Map<String, Double> armorInsulation, ItemStack armorStack, Player player, double worldTemp, double minTemp, double maxTemp)
    {
        List<InsulatorData> armorInsulators = new ArrayList<>(ConfigSettings.INSULATING_ARMORS.get().get(armorStack.getItem()));
        if (!armorInsulators.isEmpty()) // Add the armor's builtin insulation value (mutually exclusive with sewn insulation)
        {
            // Adapt builtin armor insulation
            Double newFactor = null;
            for (InsulatorData armorInsulator : armorInsulators)
            {
                // Check if the player meets the predicate for the insulation
                if (!armorInsulator.test(player, armorStack))
                {   continue;
                }
                List<Insulation> insulations = Insulation.deepCopy(armorInsulator.insulation());
                for (Insulation insul : insulations)
                {
                    // Set adaptation to calculated value
                    if (insul instanceof AdaptiveInsulation adaptive)
                    {
                        if (newFactor == null)
                        {   AdaptiveInsulation.readFactorFromArmor(adaptive, armorStack);
                            newFactor = AdaptiveInsulation.calculateChange(adaptive, worldTemp, minTemp, maxTemp);
                            AdaptiveInsulation.setFactorToArmor(armorStack, newFactor);
                        }
                        adaptive.setFactor(newFactor);
                    }
                    // Store cold/hot insulation values
                    mapAdd(armorInsulation, "cold_armor", insul.getCold());
                    mapAdd(armorInsulation, "heat_armor", insul.getHeat());
                }
            }
        }
    }

    private static void applySewnArmorInsulation(Map<String, Double> armorInsulation, ItemStack armorStack, Player player, double worldTemp, double minTemp, double maxTemp, AtomicInteger fullyInsulatedSlots)
    {
        LazyOptional<IInsulatableCap> iCap = ItemInsulationManager.getInsulationCap(armorStack);
        List<InsulatorData> insulators = RequirementHolder.filterValid(ItemInsulationManager.getAppliedArmorInsulators(armorStack), player);

        // Get the armor's insulation values
        for (InsulatorData insulator : insulators)
        {
            for (Insulation insulation : insulator.insulation())
            {
                mapAdd(armorInsulation, "cold_insulators", insulation.getCold());
                mapAdd(armorInsulation, "heat_insulators", insulation.getHeat());
            }
        }

        // Used for tracking "fully_insulated" advancement
        if ((armorInsulation.getOrDefault("cold_insulators", 0d) + armorInsulation.getOrDefault("heat_insulators", 0d)) / 2 >= ItemInsulationManager.getInsulationSlots(armorStack))
        {   fullyInsulatedSlots.incrementAndGet();
        }

        if (iCap.resolve().isPresent() && iCap.resolve().get() instanceof ItemInsulationCap cap)
        {
            // Calculate adaptive insulation adaptation state
            cap.calcAdaptiveInsulation(worldTemp, minTemp, maxTemp);
            // Remove insulation items if the player has too many
            popExtraInsulation(cap, armorStack, player);
        }
    }

    private static void mapAdd(Map<String, Double> map, String key, double value)
    {
        map.put(key, map.getOrDefault(key, 0d) + value);
    }

    private static void popExtraInsulation(ItemInsulationCap cap, ItemStack armorStack, Player player)
    {
        List<Pair<ItemStack, List<InsulatorData>>> totalInsulation = cap.getInsulation();
        int filledInsulationSlots = totalInsulation.size();
        if (filledInsulationSlots > ItemInsulationManager.getInsulationSlots(armorStack))
        {   WorldHelper.playEntitySound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, player, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        while (filledInsulationSlots > ItemInsulationManager.getInsulationSlots(armorStack))
        {
            ItemStack removedItem = cap.removeInsulationItem(totalInsulation.get(totalInsulation.size() - 1).getFirst());
            WorldHelper.entityDropItem(player, removedItem);

            filledInsulationSlots--;
        }
    }

    @SubscribeEvent
    public static void onArmorBroken(ItemBreakEvent event)
    {
        ItemStack stack = event.getItemStack();
        LivingEntity entity = event.getEntity();
        if (ItemInsulationManager.isInsulatable(stack))
        {
            ItemInsulationManager.getInsulationCap(stack).ifPresent(cap ->
            {
                for (Pair<ItemStack, List<InsulatorData>> insulation : cap.getInsulation())
                {
                    ItemEntity itemEntity = WorldHelper.entityDropItem(entity, insulation.getFirst());
                    itemEntity.setPickUpDelay(8);
                }
            });
        }
    }

    /**
     * Prevent damage by magma blocks if the player has hoglin.json hooves
     */
    @SubscribeEvent
    public static void onDamageTaken(LivingAttackEvent event)
    {
        DamageSource source = event.getSource();
        if (source == DamageSource.HOT_FLOOR && event.getEntity().getItemBySlot(EquipmentSlot.FEET).is(ModItems.HOGLIN_BOOTS))
        {   event.setCanceled(true);
        }
    }
}
