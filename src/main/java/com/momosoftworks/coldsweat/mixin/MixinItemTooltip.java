package com.momosoftworks.coldsweat.mixin;

import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.common.capability.handler.EntityTempManager;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.config.type.Insulator;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class MixinItemTooltip
{
    @Shadow protected abstract void addModifierTooltip(Consumer<Component> pTooltipAdder, @Nullable Player pPlayer, Holder<Attribute> pAttribute, AttributeModifier pModfier);

    ItemStack stack = (ItemStack) (Object) this;

    @Inject(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addToTooltip(Lnet/minecraft/core/component/DataComponentType;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/function/Consumer;Lnet/minecraft/world/item/TooltipFlag;)V",
                                                 ordinal = 6, shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectBeforeAttributes(Item.TooltipContext pTooltipContext, Player player, TooltipFlag pTooltipFlag, CallbackInfoReturnable<List<Component>> cir,
                                        //locals
                                        List<Component> tooltip, MutableComponent mutablecomponent, Consumer consumer)
    {
        ItemStack stack = (ItemStack) (Object) this;

        // Add insulation attributes to tooltip
        AttributeModifierMap insulatorAttributes = new AttributeModifierMap();
        for (Insulator insulator : ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()))
        {
            if (insulator.test(player, stack))
            {   insulatorAttributes.putAll(insulator.attributes());
            }
        }
        if (!insulatorAttributes.isEmpty())
        {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("item.modifiers.insulation").withStyle(ChatFormatting.GRAY));
            TooltipHandler.addModifierTooltipLines(tooltip, insulatorAttributes);
        }

        // Add curio attributes to tooltip
        AttributeModifierMap curioAttributes = new AttributeModifierMap();
        for (Insulator insulator : ConfigSettings.INSULATING_CURIOS.get().get(stack.getItem()))
        {
            if (insulator.test(player, stack))
            {   curioAttributes.putAll(insulator.attributes());
            }
        }
        if (!curioAttributes.isEmpty())
        {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("item.modifiers.curio").withStyle(ChatFormatting.GRAY));
            TooltipHandler.addModifierTooltipLines(tooltip, curioAttributes);
        }
    }

    @Inject(method = "addAttributeTooltips", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void getItemAttributes(Consumer<Component> pTooltipAdder, Player player, CallbackInfo ci,
                                   // locals
                                   ItemAttributeModifiers itemattributemodifiers, EquipmentSlotGroup[] allSlots, int var5, int var6, EquipmentSlotGroup slot, MutableBoolean isFirstLine)
    {
        // We don't care if the item is not equipped in the correct slot
        if (player == null || EquipmentSlotGroup.bySlot(Minecraft.getInstance().player.getEquipmentSlotForItem(stack)) != slot
        || Arrays.stream(EquipmentSlot.values()).noneMatch(eq -> slot.test(eq) && stack.equals(player.getItemBySlot(eq))))
        {   return;
        }

        for (Insulator insulator : ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem()))
        {
            if (insulator.test(player, stack))
            {
                for (Map.Entry<Attribute, AttributeModifier> entry : insulator.attributes().getMap().entries())
                {   pTooltipAdder.accept(TooltipHandler.getFormattedAttributeModifier(Holder.direct(entry.getKey()), entry.getValue().amount(), entry.getValue().operation()));
                }
            }
        }
        ItemInsulationManager.getInsulationCap(stack).ifPresent(cap ->
        {
            cap.getInsulation().stream().map(Pair::getFirst).forEach(item ->
            {
                for (Insulator insulator : ConfigSettings.INSULATION_ITEMS.get().get(item.getItem()))
                {
                    if (insulator.test(player, item))
                    {
                        for (Map.Entry<Attribute, AttributeModifier> entry : insulator.attributes().getMap().entries())
                        {   pTooltipAdder.accept(TooltipHandler.getFormattedAttributeModifier(Holder.direct(entry.getKey()), entry.getValue().amount(), entry.getValue().operation()));
                        }
                    }
                }
            });
        });
    }

    @Inject(method = "addModifierTooltip",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"),
            cancellable = true)
    private void setupCustomAttributeDisplay(Consumer<Component> tooltip, Player player, Holder<Attribute> attribute, AttributeModifier modifier, CallbackInfo ci)
    {
        if (EntityTempManager.isTemperatureAttribute(attribute.value()))
        {
            MutableComponent newline = TooltipHandler.getFormattedAttributeModifier(attribute, modifier.amount(), modifier.operation());
            tooltip.accept(newline);
            ci.cancel();
        }
    }
}
