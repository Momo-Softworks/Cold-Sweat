package com.momosoftworks.coldsweat.util.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.stream.Stream;

public class ItemStackHelper
{
    public static void playBucketEmptySound(ItemStack stack, @Nullable Player pPlayer, LevelAccessor pLevel, BlockPos pPos)
    {
        Method playEmptySound = ObfuscationReflectionHelper.findMethod(BucketItem.class, "playEmptySound", Player.class, LevelAccessor.class, BlockPos.class);
        playEmptySound.setAccessible(true);
        try
        {   playEmptySound.invoke(stack.getItem(), pPlayer, pLevel, pPos);
        }
        catch (Exception e)
        {   e.printStackTrace();
        }
    }

    public static Stream<ItemAttributeModifiers.Entry> getAttributeModifiers(ItemStack stack, AttributeModifier.Operation operation)
    {   return stack.getAttributeModifiers().modifiers().stream().filter(entry -> entry.modifier().operation() == operation);
    }

    public static Stream<ItemAttributeModifiers.Entry> getAttributeModifiers(ItemStack stack, EquipmentSlot slot)
    {   return stack.getAttributeModifiers().modifiers().stream().filter(entry -> entry.slot().test(slot));
    }
}
