package com.momosoftworks.coldsweat.core.network.message;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.network.BufferHelper;
import com.momosoftworks.coldsweat.core.network.ColdSweatPacketHandler;
import com.momosoftworks.coldsweat.data.codec.configuration.InsulatorData;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SyncItemPredicatesMessage
{
    private final Map<UUID, Boolean> predicateMap = new HashMap<>();
    private final int inventorySlot;
    @Nullable private final EquipmentSlotType equipmentSlot;
    @Nullable private final ItemStack responseStack;

    public static SyncItemPredicatesMessage fromClient(int inventorySlot, @Nullable EquipmentSlotType equipmentSlot)
    {   return new SyncItemPredicatesMessage(inventorySlot, equipmentSlot, null, new HashMap<>());
    }

    public static SyncItemPredicatesMessage fromServer(ItemStack stack, int inventorySlot, @Nullable EquipmentSlotType equipmentSlot, Entity entity)
    {
        SyncItemPredicatesMessage message = new SyncItemPredicatesMessage(inventorySlot, equipmentSlot, stack, new HashMap<>());

        message.checkInsulator(stack, entity);
        message.checkInsulatingArmor(stack, entity);
        message.checkInsulatingCurio(stack, entity);
        message.checkArmorInsulation(stack, entity);

        message.checkBoilerFuel(stack);
        message.checkIceboxFuel(stack);
        message.checkHearthFuel(stack);
        message.checkSoulLampFuel(stack);

        message.checkFood(stack, entity);
        message.checkItemTemps(stack, inventorySlot, equipmentSlot, entity);
        message.checkDryingItems(stack, entity);

        return message;
    }

    private SyncItemPredicatesMessage(int inventorySlot, @Nullable EquipmentSlotType equipmentSlot,
                                       @Nullable ItemStack responseStack, Map<UUID, Boolean> predicateMap)
    {
        this.inventorySlot = inventorySlot;
        this.equipmentSlot = equipmentSlot;
        this.responseStack = responseStack;
        this.predicateMap.putAll(predicateMap);
    }

    public static void encode(SyncItemPredicatesMessage message, PacketBuffer buffer)
    {
        buffer.writeInt(message.inventorySlot);
        BufferHelper.writeOptional(buffer, Optional.ofNullable(message.equipmentSlot), PacketBuffer::writeEnum);

        boolean hasResponse = message.responseStack != null && !message.predicateMap.isEmpty();
        buffer.writeBoolean(hasResponse);
        if (hasResponse)
        {   buffer.writeItem(message.responseStack);
            BufferHelper.writeMap(buffer, message.predicateMap, PacketBuffer::writeUUID, PacketBuffer::writeBoolean);
        }
    }

    public static SyncItemPredicatesMessage decode(PacketBuffer buffer)
    {
        int inventorySlot = buffer.readInt();
        EquipmentSlotType equipmentSlot = BufferHelper.readOptional(buffer, buf -> buf.readEnum(EquipmentSlotType.class)).orElse(null);

        boolean hasResponse = buffer.readBoolean();
        if (hasResponse)
        {   ItemStack stack = buffer.readItem();
            Map<UUID, Boolean> predicateMap = BufferHelper.readMap(buffer, PacketBuffer::readUUID, PacketBuffer::readBoolean);
            return new SyncItemPredicatesMessage(inventorySlot, equipmentSlot, stack, predicateMap);
        }
        else return new SyncItemPredicatesMessage(inventorySlot, equipmentSlot, null, new HashMap<>());
    }

    public static void handle(SyncItemPredicatesMessage message, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        LogicalSide receivingSide = context.getDirection().getReceptionSide();

        if (receivingSide.isClient())
        {
            context.enqueueWork(() ->
            {   TooltipHandler.HOVERED_STACK_PREDICATES.putAll(message.predicateMap);
            });
        }
        else if (receivingSide.isServer() && context.getSender() != null)
        {
            context.enqueueWork(() ->
            {
                ServerPlayerEntity player = context.getSender();
                ItemStack stack = getStackFromPlayer(player, message.inventorySlot, message.equipmentSlot);
                if (!stack.isEmpty())
                {
                    ColdSweatPacketHandler.INSTANCE.sendTo(SyncItemPredicatesMessage.fromServer(stack, message.inventorySlot, message.equipmentSlot, player),
                                                           player.connection.connection,
                                                           NetworkDirection.PLAY_TO_CLIENT);
                }
            });
        }
    }

    private static ItemStack getStackFromPlayer(ServerPlayerEntity player, int inventorySlot, @Nullable EquipmentSlotType equipmentSlot)
    {
        if (equipmentSlot != null)
        {   return player.getItemBySlot(equipmentSlot);
        }
        if (inventorySlot >= 0 && inventorySlot < player.inventory.getContainerSize())
        {   return player.inventory.getItem(inventorySlot);
        }
        if (player.containerMenu != null && inventorySlot >= 0 && inventorySlot < player.containerMenu.slots.size())
        {   return player.containerMenu.getSlot(inventorySlot).getItem();
        }
        return ItemStack.EMPTY;
    }

    private void checkInsulator(ItemStack stack, Entity entity)
    {   this.checkItemRequirement(stack, entity, ConfigSettings.INSULATION_ITEMS);
    }

    private void checkInsulatingArmor(ItemStack stack, Entity entity)
    {   this.checkItemRequirement(stack, entity, ConfigSettings.INSULATING_ARMORS);
    }

    private void checkInsulatingCurio(ItemStack stack, Entity entity)
    {   this.checkItemRequirement(stack, entity, ConfigSettings.INSULATING_CURIOS);
    }

    private void checkArmorInsulation(ItemStack stack, Entity entity)
    {
        if (ItemInsulationManager.isInsulatable(stack))
        {
            ItemInsulationManager.getInsulationCap(stack).ifPresent(cap ->
            {
                for (Pair<ItemStack, List<InsulatorData>> pair : cap.getInsulation())
                {
                    for (InsulatorData insulatorData : pair.getSecond())
                    {   this.predicateMap.put(insulatorData.uuid(), insulatorData.test(entity, pair.getFirst()));
                    }
                }
            });
        }
    }

    private void checkFood(ItemStack stack, Entity entity)
    {   this.checkItemRequirement(stack, entity, ConfigSettings.FOOD_TEMPERATURES);
    }

    private void checkBoilerFuel(ItemStack stack)
    {   this.checkItemRequirement(stack, null, ConfigSettings.BOILER_FUEL);
    }

    private void checkIceboxFuel(ItemStack stack)
    {   this.checkItemRequirement(stack, null, ConfigSettings.ICEBOX_FUEL);
    }

    private void checkHearthFuel(ItemStack stack)
    {   this.checkItemRequirement(stack, null, ConfigSettings.HEARTH_FUEL);
    }

    private void checkSoulLampFuel(ItemStack stack)
    {   this.checkItemRequirement(stack, null, ConfigSettings.SOULSPRING_LAMP_FUEL);
    }

    private void checkItemTemps(ItemStack stack, int invSlot, EquipmentSlotType equipmentSlot, Entity entity)
    {
        if (ConfigSettings.ITEM_TEMPERATURES.get().containsKey(stack.getItem()))
        {
            Map<UUID, Boolean> insulatorMap = ConfigSettings.ITEM_TEMPERATURES.get().get(stack.getItem())
                                              .stream()
                                              .map(data ->
                                              {   boolean test = data.test(entity, stack, invSlot, equipmentSlot);
                                                  return new AbstractMap.SimpleEntry<>(data.uuid(), test);
                                              })
                                              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            this.predicateMap.putAll(insulatorMap);
        }
    }

    private void checkDryingItems(ItemStack stack, Entity entity)
    {   this.checkItemRequirement(stack, entity, ConfigSettings.DRYING_ITEMS);
    }

    private void checkItemRequirement(ItemStack stack, Entity entity, DynamicHolder<? extends Multimap<Item, ? extends RequirementHolder>> configSetting)
    {
        Map<UUID, Boolean> configMap = new HashMap<>();
        configSetting.get().get(stack.getItem())
        .forEach(data ->
        {
            UUID id = ((ConfigData) data).uuid();
            configMap.put(id, data.test(entity, stack));
        });
        this.predicateMap.putAll(configMap);
    }
}
