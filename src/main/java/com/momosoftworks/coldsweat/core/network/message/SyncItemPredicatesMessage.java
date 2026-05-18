package com.momosoftworks.coldsweat.core.network.message;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.client.event.TooltipHandler;
import com.momosoftworks.coldsweat.client.gui.tooltip.util.RequirementCheck;
import com.momosoftworks.coldsweat.common.capability.handler.ItemInsulationManager;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.configuration.*;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.util.serialization.DynamicHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class SyncItemPredicatesMessage implements CustomPacketPayload
{
    public static final Type<SyncItemPredicatesMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ColdSweat.MOD_ID, "sync_item_predicates"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncItemPredicatesMessage> CODEC = CustomPacketPayload.codec(SyncItemPredicatesMessage::encode, SyncItemPredicatesMessage::decode);

    private final Map<UUID, Boolean> predicateMap = new HashMap<>();
    private final int inventorySlot;
    @Nullable private final EquipmentSlot equipmentSlot;
    private final ItemStack stack;

    public static SyncItemPredicatesMessage fromClient(int inventorySlot, @Nullable EquipmentSlot equipmentSlot, ItemStack stack)
    {   return new SyncItemPredicatesMessage(inventorySlot, equipmentSlot, stack, Map.of());
    }

    public static SyncItemPredicatesMessage fromServer(int inventorySlot, @Nullable EquipmentSlot equipmentSlot, ItemStack stack, Entity entity)
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
        message.checkItemTemps(stack, entity);
        message.checkDryingItems(stack, entity);

        return message;
    }

    private SyncItemPredicatesMessage(int inventorySlot, @Nullable EquipmentSlot equipmentSlot,
                                      ItemStack stack, Map<UUID, Boolean> predicateMap)
    {
        this.inventorySlot = inventorySlot;
        this.equipmentSlot = equipmentSlot;
        this.stack = stack;
        this.predicateMap.putAll(predicateMap);
    }

    public static void encode(SyncItemPredicatesMessage message, RegistryFriendlyByteBuf buffer)
    {
        buffer.writeInt(message.inventorySlot);
        buffer.writeOptional(Optional.ofNullable(message.equipmentSlot), FriendlyByteBuf::writeEnum);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, message.stack);
        buffer.writeMap(message.predicateMap, (buf, uuid) -> buf.writeUUID(uuid), FriendlyByteBuf::writeBoolean);
    }

    public static SyncItemPredicatesMessage decode(RegistryFriendlyByteBuf buffer)
    {
        int inventorySlot = buffer.readInt();
        EquipmentSlot equipmentSlot = buffer.readOptional(buf -> buf.readEnum(EquipmentSlot.class)).orElse(null);
        ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        Map<UUID, Boolean> predicateMap = buffer.readMap(buf -> buf.readUUID(), FriendlyByteBuf::readBoolean);
        return new SyncItemPredicatesMessage(inventorySlot, equipmentSlot, stack, predicateMap);
    }

    public static void handle(SyncItemPredicatesMessage message, IPayloadContext context)
    {
        LogicalSide receivingSide = context.flow().getReceptionSide();

        if (receivingSide.isClient())
        {
            context.enqueueWork(() ->
            {
                message.predicateMap.forEach((uuid, value) ->
                {   TooltipHandler.HOVERED_STACK_PREDICATES.put(uuid, value ? RequirementCheck.PASSED : RequirementCheck.FAILED);
                });
                TooltipHandler.FETCHING_TOOLTIP = false;
            });
        }
        else if (receivingSide.isServer() && context.player() instanceof ServerPlayer player)
        {
            context.enqueueWork(() ->
            {   PacketDistributor.sendToPlayer(player, SyncItemPredicatesMessage.fromServer(message.inventorySlot, message.equipmentSlot, message.stack, player));
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type()
    {   return TYPE;
    }

    public static boolean hasDataToSend(ItemStack stack)
    {
        Item item = stack.getItem();
        return ConfigSettings.INSULATION_ITEMS.get().containsKey(item)
            || ConfigSettings.INSULATING_ARMORS.get().containsKey(item)
            || ConfigSettings.INSULATING_CURIOS.get().containsKey(item)
            || ItemInsulationManager.isInsulatable(stack)
            || ConfigSettings.FOOD_TEMPERATURES.get().containsKey(item)
            || ConfigSettings.BOILER_FUEL.get().containsKey(item)
            || ConfigSettings.ICEBOX_FUEL.get().containsKey(item)
            || ConfigSettings.HEARTH_FUEL.get().containsKey(item)
            || ConfigSettings.SOULSPRING_LAMP_FUEL.get().containsKey(item)
            || ConfigSettings.ITEM_TEMPERATURES.get().containsKey(item)
            || ConfigSettings.DRYING_ITEMS.get().containsKey(item);
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

    private void checkItemTemps(ItemStack stack, Entity entity)
    {
        if (ConfigSettings.ITEM_TEMPERATURES.get().containsKey(stack.getItem()))
        {
            Map<UUID, Boolean> insulatorMap = ConfigSettings.ITEM_TEMPERATURES.get().get(stack.getItem())
                                              .stream()
                                              .map(data ->
                                              {   boolean test = data.test(entity, stack);
                                                  return Map.entry(data.uuid(), test);
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
