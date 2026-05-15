package com.momosoftworks.coldsweat.util.entity;

import com.momosoftworks.coldsweat.core.init.ModItems;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.common.item.SoulspringLampItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EntityHelper
{
    private static final Map<Entity, Pair<CompoundTag, Long>> ENTITY_DATA_CACHE = new HashMap<>();

    private EntityHelper() {}

    public static ItemStack getItemInHand(LivingEntity player, HumanoidArm hand)
    {   return player.getItemInHand(hand == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public static HumanoidArm getArmFromHand(InteractionHand hand, Player player)
    {   return hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public static boolean holdingLamp(LivingEntity player, HumanoidArm arm)
    {   return getItemInHand(player, arm).getItem() == ModItems.SOULSPRING_LAMP.value();
    }

    public static boolean holdingLitLamp(LivingEntity player)
    {   return Stream.of(HumanoidArm.LEFT, HumanoidArm.RIGHT).anyMatch(arm -> holdingLamp(player, arm) && SoulspringLampItem.isLit(getItemInHand(player, arm)));
    }

    public static Vec3 getCenterOf(Entity entity)
    {   return entity.position().add(0, entity.getBbHeight() / 2, 0);
    }

    public static ServerPlayer getServerPlayer(Player player)
    {   return ((MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER)).getPlayerList().getPlayer(player.getUUID());
    }

    @Nullable
    public static EquipmentSlot getEquipmentSlot(int index)
    {
        if (index == 100 + EquipmentSlot.HEAD.getIndex())
        {   return EquipmentSlot.HEAD;
        }
        else if (index == 100 + EquipmentSlot.CHEST.getIndex())
        {   return EquipmentSlot.CHEST;
        }
        else if (index == 100 + EquipmentSlot.LEGS.getIndex())
        {   return EquipmentSlot.LEGS;
        }
        else if (index == 100 + EquipmentSlot.FEET.getIndex())
        {   return EquipmentSlot.FEET;
        }
        else if (index == 98)
        {   return EquipmentSlot.MAINHAND;
        }
        else
        {   return index == 99 ? EquipmentSlot.OFFHAND : null;
        }
    }

    public static CompoundTag getFullData(Entity entity)
    {
        long time = System.currentTimeMillis();
        Pair<CompoundTag, Long> pair = ENTITY_DATA_CACHE.get(entity);
        if (pair != null && time - pair.getSecond() < 1000)
        {   return pair.getFirst();
        }
        else
        {   CompoundTag nbt = entity.saveWithoutId(new CompoundTag());
            ENTITY_DATA_CACHE.put(entity, Pair.of(nbt, time));
            return nbt;
        }
    }
}
