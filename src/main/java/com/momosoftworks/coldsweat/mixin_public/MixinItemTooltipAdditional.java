package com.momosoftworks.coldsweat.mixin_public;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.util.math.FastMultiMap;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;

import java.util.Map;

@EventBusSubscriber(Dist.CLIENT)
public class MixinItemTooltipAdditional
{
    public static Multimap<Holder<Attribute>, AttributeModifier> INSULATION_MODIFIERS = new FastMultiMap<>();
    public static Multimap<Holder<Attribute>, AttributeModifier> UNMET_MODIFIERS = new FastMultiMap<>();
    public static EquipmentSlotGroup CURRENT_SLOT_QUERY = null;

    @SubscribeEvent
    public static void addModifiers(AddAttributeTooltipsEvent event)
    {
        ItemStack stack = event.getStack();
        for (InsulatorData data : ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem()))
        {
            for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : data.attributes().getMap().entries())
            {
                Holder<Attribute> attribute = entry.getKey();
                AttributeModifier modifier = entry.getValue();
                event.addTooltipLines(TooltipHandler.getFormattedAttributeModifier(attribute, modifier.amount(), modifier.operation(),
                                                                                   true, !TooltipHandler.passesRequirement(data)));
            }
        }
        ItemInsulationManager.getInsulationCap(stack).ifPresent(cap ->
        {
            cap.getInsulation().stream().map(Pair::getFirst).forEach(item ->
            {
                for (InsulatorData insulator : ConfigSettings.INSULATION_ITEMS.get().get(item.getItem()))
                {
                    for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : insulator.attributes().getMap().entries())
                    {
                        Holder<Attribute> attribute = entry.getKey();
                        AttributeModifier modifier = entry.getValue();
                        event.addTooltipLines(TooltipHandler.getFormattedAttributeModifier(attribute, modifier.amount(), modifier.operation(),
                                                                                           true, !TooltipHandler.passesRequirement(insulator)));
                    }
                }
            });
        });
    }
}
