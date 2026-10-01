package com.momosoftworks.coldsweat.mixin;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.ArrayList;
import net.minecraft.world.item.component.TooltipDisplay;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.client.gui.tooltip.util.RequirementCheck;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class MixinItemTooltip
{
    ItemStack stack = (ItemStack) (Object) this;

    @Inject(method = "addDetailsToTooltipComponents",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;addAttributeTooltips(Ljava/util/function/Consumer;Lnet/minecraft/world/item/component/TooltipDisplay;Lnet/minecraft/world/entity/player/Player;)V"))
    private void injectBeforeAttributes(Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag tooltipFlag, Consumer<Component> builder, CallbackInfo ci)
    {
        ItemStack stack = (ItemStack) (Object) this;
        List<Component> tooltip = new ArrayList<>();

        // Add insulation attributes to tooltip
        AttributeModifierMap insulatorAttributes = new AttributeModifierMap();
        AttributeModifierMap unmetInsulatorAttributes = new AttributeModifierMap();
        for (InsulatorData insulator : ConfigSettings.INSULATION_ITEMS.get().get(stack.getItem()))
        {
            RequirementCheck check = TooltipHandler.checkRequirement(insulator);
            if (check.passed() || check.unknown())
            {   insulatorAttributes.putAll(insulator.attributes());
            }
            else unmetInsulatorAttributes.putAll(insulator.attributes());
        }
        if (!insulatorAttributes.isEmpty() || !unmetInsulatorAttributes.isEmpty())
        {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("item.modifiers.insulation").withStyle(ChatFormatting.GRAY));
            TooltipHandler.addModifierTooltipLines(tooltip, insulatorAttributes, true, false);
            TooltipHandler.addModifierTooltipLines(tooltip, unmetInsulatorAttributes, true, true);
        }

        // Add curio attributes to tooltip
        AttributeModifierMap curioAttributes = new AttributeModifierMap();
        AttributeModifierMap unmetCurioAttributes = new AttributeModifierMap();
        for (InsulatorData insulator : ConfigSettings.INSULATING_CURIOS.get().get(stack.getItem()))
        {
            RequirementCheck check = TooltipHandler.checkRequirement(insulator);
            if (check.passed() || check.unknown())
            {   curioAttributes.putAll(insulator.attributes());
            }
            else unmetCurioAttributes.putAll(insulator.attributes());
        }
        if (!curioAttributes.isEmpty() || !unmetCurioAttributes.isEmpty())
        {
            tooltip.add(CommonComponents.EMPTY);
            tooltip.add(Component.translatable("item.modifiers.curio").withStyle(ChatFormatting.GRAY));
            TooltipHandler.addModifierTooltipLines(tooltip, curioAttributes, true, false);
            TooltipHandler.addModifierTooltipLines(tooltip, unmetCurioAttributes, true, true);
        }
        tooltip.forEach(builder);
    }
}
