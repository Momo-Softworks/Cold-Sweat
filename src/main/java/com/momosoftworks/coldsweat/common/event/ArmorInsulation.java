package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.api.temperature.modifier.InsulationTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.capability.insulation.IInsulatableCap;
import com.momosoftworks.coldsweat.common.capability.insulation.ItemInsulationCap;
import com.momosoftworks.coldsweat.common.event.capability.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.util.ItemData;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber
public class ArmorInsulation
{
    @SubscribeEvent
    public static void addLeatherModifiers(TickEvent.PlayerTickEvent event)
    {
        PlayerEntity player = event.player;
        if (event.phase == TickEvent.Phase.END && !player.level.isClientSide() && player.tickCount % 10 == 0)
        {
            int fullyInsulated = 0;
            double cold = 0;
            double hot = 0;

            double worldTemp = Temperature.get(player, Temperature.Type.WORLD);
            double minTemp = Temperature.get(player, Temperature.Ability.BURNING_POINT);
            double maxTemp = Temperature.get(player, Temperature.Ability.FREEZING_POINT);

            for (ItemStack armorStack : player.getArmorSlots())
            {
                if (armorStack.getItem() instanceof ArmorItem)
                {
                    ArmorItem armorItem = (ArmorItem) armorStack.getItem();
                    // Add the armor's intrinsic insulation value (defined in configs)
                    // Mutually exclusive with Sewing Table insulation
                    Insulation insulationValue = ConfigSettings.INSULATING_ARMORS.get().get(ItemData.of(armorStack));
                    if (insulationValue != null)
                    {
                        cold += insulationValue.getCold();
                        hot += insulationValue.getHot();
                    }
                    else
                    {   // Add the armor's insulation value from the Sewing Table
                        LazyOptional<IInsulatableCap> iCap = ItemInsulationManager.getInsulationCap(armorStack);
                        List<Insulation> insulation = ItemInsulationManager.getInsulationCap(armorStack)
                                                      .map(IInsulatableCap::getInsulation).orElse(new ArrayList<>())
                                                      .stream()
                                                      .map(pair -> pair.getSecond())
                                                      .flatMap(List::stream).collect(Collectors.toList());

                        // Get the armor's insulation values
                        for (Insulation value : insulation)
                        {
                            if (value instanceof StaticInsulation)
                            {
                                StaticInsulation insul = ((StaticInsulation) value);
                                cold += insul.getCold();
                                hot += insul.getHot();
                            }
                            else if (value instanceof AdaptiveInsulation)
                            {   AdaptiveInsulation insul = (AdaptiveInsulation) value;
                                cold += CSMath.blend(insul.getInsulation() * 0.75, 0, insul.getFactor(), -1, 1);
                                hot += CSMath.blend(0, insul.getInsulation() * 0.75, insul.getFactor(), -1, 1);
                            }
                        }

                        // Used for tracking "fully_insulated" advancement
                        if ((cold + hot) / 2 >= ItemInsulationManager.getInsulationSlots(armorStack))
                        {   fullyInsulated++;
                        }

                        if (iCap.resolve().isPresent() && iCap.resolve().get() instanceof ItemInsulationCap)
                        {   ((ItemInsulationCap) iCap.resolve().get()).calcAdaptiveInsulation(worldTemp, minTemp, maxTemp);
                        }
                    }

                    // Add the armor's defense value to the insulation value.
                    double armorAmount = armorStack.getAttributeModifiers(MobEntity.getEquipmentSlotForItem(armorStack)).entries()
                                         .stream().filter(entry -> entry.getKey().equals(Attributes.ARMOR))
                                         .map(entry -> entry.getValue().getAmount())
                                         .mapToDouble(Double::doubleValue).sum();
                    cold += Math.min(armorAmount, 20);
                    hot += Math.min(armorAmount, 20);

                }
            }

            if (cold == 0 && hot == 0)
                Temperature.removeModifiers(player, Temperature.Type.RATE, (mod) -> mod instanceof InsulationTempModifier);
            else
                Temperature.addOrReplaceModifier(player, new InsulationTempModifier(cold, hot).tickRate(20), Temperature.Type.RATE);

            // Award advancement for full insulation
            if (fullyInsulated >= 4 && player instanceof ServerPlayerEntity)
            {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
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

    /**
     * Prevent damage by magma blocks if the player has hoglin hooves
     */
    @SubscribeEvent
    public static void onDamageTaken(LivingAttackEvent event)
    {
        DamageSource source = event.getSource();
        if (source == DamageSource.HOT_FLOOR && event.getEntityLiving().getItemBySlot(EquipmentSlotType.FEET).getItem() == ModItems.HOGLIN_HOOVES)
        {   event.setCanceled(true);
        }
    }
}
