package com.momosoftworks.coldsweat.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.common.event.capability.EntityTempManager;
import com.momosoftworks.coldsweat.common.event.capability.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

@Mixin(ItemStack.class)
public class MixinItemTooltip
{
    @Inject(method = "getTooltipLines", at = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemStack$TooltipDisplayFlags;MODIFIERS:Lnet/minecraft/item/ItemStack$TooltipDisplayFlags;", shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void injectBeforeAttributes(PlayerEntity player, ITooltipFlag advanced, CallbackInfoReturnable<List<ITextComponent>> cir,
                                        // local variables
                                        List<ITextComponent> tooltip)
    {
        ItemStack stack = (ItemStack) (Object) this;
        Optional.ofNullable(ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem())).ifPresent(insulator ->
        {
            if (insulator.test(Minecraft.getInstance().player, stack))
            {
                if (!insulator.attributes.getMap().isEmpty())
                {
                    tooltip.add(new StringTextComponent(""));
                    tooltip.add(new TranslationTextComponent("item.modifiers.insulation").withStyle(TextFormatting.GRAY));
                    TooltipHandler.addModifierTooltipLines(tooltip, insulator.attributes);
                }
            }
        });
    }

    @Redirect(method = "getTooltipLines", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getAttributeModifiers(Lnet/minecraft/inventory/EquipmentSlotType;)Lcom/google/common/collect/Multimap;"))
    private Multimap<Attribute, AttributeModifier> getItemAttributes(ItemStack stack, EquipmentSlotType slot)
    {
        if (MobEntity.getEquipmentSlotForItem(stack) != slot)
        {   return stack.getAttributeModifiers(slot);
        }
        Multimap<Attribute, AttributeModifier> map = HashMultimap.create(stack.getAttributeModifiers(slot));

        Optional.ofNullable(ConfigSettings.INSULATING_ARMORS.get().get(stack.getItem())).ifPresent(insulator ->
        {
            if (insulator.test(Minecraft.getInstance().player, stack))
            {   map.putAll(insulator.attributes.getMap());
            }
        });
        ItemInsulationManager.getInsulationCap(stack).ifPresent(cap ->
        {
            cap.getInsulation().stream().map(Pair::getFirst).forEach(item ->
            {
                Optional.ofNullable(ConfigSettings.INSULATION_ITEMS.get().get(item.getItem())).ifPresent(insulator ->
                {
                    if (insulator.test(Minecraft.getInstance().player, item))
                    {   map.putAll(insulator.attributes.getMap());
                    }
                });
            });
        });
        return map;
    }

    private static List<ITextComponent> TOOLTIP = null;
    private static Map.Entry<Attribute, AttributeModifier> ENTRY = null;
    private static AttributeModifier MODIFIER = null;

    @Inject(method = "getTooltipLines",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"),
            slice = @Slice
                    (
                            from = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 7),
                            to = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 9)
                    ),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void setupCustomAttributeDisplay(PlayerEntity pPlayer, ITooltipFlag pIsAdvanced, CallbackInfoReturnable<List<ITextComponent>> cir,
                                             // Locals
                                             List<ITextComponent> tooltip, IFormattableTextComponent name, int hideFlags, EquipmentSlotType[] var6, int var7, int var8,
                                             EquipmentSlotType equipmentslot, Multimap<Attribute, AttributeModifier> attributeMap, Iterator<AttributeModifier> entryIterator,
                                             Map.Entry<Attribute, AttributeModifier> entry, AttributeModifier modifier, double d0, boolean flag, double d1)
    {
        TOOLTIP = tooltip;
        ENTRY = entry;
        MODIFIER = modifier;
    }

    @ModifyArg(method = "getTooltipLines",
               at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"),
               slice = @Slice
               (
                   from = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 7),
                   to = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 9)
               ))
    private <E> E stopCustomAttributeVanilla(E obj)
    {
        if (obj instanceof IFormattableTextComponent)
        {
            IFormattableTextComponent component = ((IFormattableTextComponent) obj);
            List<ITextComponent> siblings = component.getSiblings();
            if (TOOLTIP != null && ENTRY != null && MODIFIER != null
            && EntityTempManager.isTemperatureAttribute(ENTRY.getKey()))
            {
                IFormattableTextComponent newline = TooltipHandler.getFormattedAttributeModifier(ENTRY.getKey(), MODIFIER.getAmount(), MODIFIER.getOperation());
                for (ITextComponent sibling : siblings)
                {   newline = newline.append(sibling);
                }
                return (E) newline;
            }
        }
        return obj;
    }
}
