package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.data.codec.util.ValueGetter;
import java.util.Map;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.EnumHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.*;

public class ItemTempData extends ConfigData implements RequirementHolder
{
    final NegatableList<ItemRequirement> item;
    final List<Either<IntegerBounds, SlotType>> slots;
    final ValueGetter<Double> temperature;
    final Temperature.Trait trait;
    final ValueGetter<Double> maxEffect;
    final ValueGetter<Double> maxTemp;
    final ValueGetter<Double> minTemp;
    final NegatableList<EntityRequirement> entityRequirement;
    final AttributeModifierMap attributeModifiers;
    final Map<ResourceLocation, ValueGetter<Double>> immuneTempModifiers;
    final ValueGetter<Boolean> hideIfUnmet;

    public ItemTempData(NegatableList<ItemRequirement> item, List<Either<IntegerBounds, SlotType>> slots, ValueGetter<Double> temperature,
                        Temperature.Trait trait, ValueGetter<Double> maxEffect, ValueGetter<Double> maxTemp, ValueGetter<Double> minTemp,
                        NegatableList<EntityRequirement> entityRequirement, AttributeModifierMap attributeModifiers,
                        Map<ResourceLocation, ValueGetter<Double>> immuneTempModifiers, ValueGetter<Boolean> hideIfUnmet, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.item = item;
        this.slots = slots;
        this.temperature = temperature;
        this.trait = trait;
        this.maxEffect = maxEffect;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.entityRequirement = entityRequirement;
        this.attributeModifiers = attributeModifiers;
        this.immuneTempModifiers = immuneTempModifiers;
        this.hideIfUnmet = hideIfUnmet;
    }

    public ItemTempData(NegatableList<ItemRequirement> item, List<Either<IntegerBounds, SlotType>> slots, ValueGetter<Double> temperature,
                        Temperature.Trait trait, ValueGetter<Double> maxEffect, ValueGetter<Double> maxTemp, ValueGetter<Double> minTemp,
                        NegatableList<EntityRequirement> entityRequirement, AttributeModifierMap attributeModifiers,
                        Map<ResourceLocation, ValueGetter<Double>> immuneTempModifiers, ValueGetter<Boolean> hideIfUnmet)
    {
        this(item, slots, temperature, trait, maxEffect, maxTemp, minTemp, entityRequirement, attributeModifiers, immuneTempModifiers, hideIfUnmet, new NegatableList<>());
    }

    public static final Codec<ItemTempData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(ItemTempData::item),
            Codec.either(IntegerBounds.CODEC, SlotType.CODEC).listOf().fieldOf("slots").forGetter(ItemTempData::slots),
            ValueGetter.fieldCodec("temperature", ExtraCodecs.DOUBLE, 0.0).forGetter(ItemTempData::temperature),
            Temperature.Trait.CODEC.optionalFieldOf("trait", Temperature.Trait.WORLD).forGetter(ItemTempData::trait),
            ValueGetter.optionalFieldCodec("max_effect", ExtraCodecs.DOUBLE, Double.POSITIVE_INFINITY).forGetter(ItemTempData::maxEffect),
            ValueGetter.optionalFieldCodec("max_temp", ExtraCodecs.DOUBLE, Double.POSITIVE_INFINITY).forGetter(ItemTempData::maxTemp),
            ValueGetter.optionalFieldCodec("min_temp", ExtraCodecs.DOUBLE, Double.NEGATIVE_INFINITY).forGetter(ItemTempData::minTemp),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(ItemTempData::entityRequirement),
            AttributeModifierMap.CODEC.optionalFieldOf("attributes", new AttributeModifierMap()).forGetter(ItemTempData::attributeModifiers),
            Codec.unboundedMap(ResourceLocation.CODEC, ValueGetter.codec(ExtraCodecs.DOUBLE, 0.0)).optionalFieldOf("immune_temp_modifiers", new HashMap<>()).forGetter(ItemTempData::immuneTempModifiers),
            ValueGetter.optionalFieldCodec("hide_if_unmet", Codec.BOOL, false).forGetter(ItemTempData::hideIfUnmet)
    ).apply(instance, ItemTempData::new)));

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public List<Either<IntegerBounds, SlotType>> slots()
    {   return slots;
    }
    public ValueGetter<Double> temperature()
    {   return temperature;
    }
    public double getTemperature(Entity entity, ItemStack item)
    {   return temperature.get(Map.of("entity", entity, "item", item));
    }
    public Temperature.Trait trait()
    {   return trait;
    }
    public ValueGetter<Double> maxEffect()
    {   return maxEffect;
    }
    public double maxEffect(ItemStack stack, Entity entity)
    {   return maxEffect.get(Map.of("item", stack, "entity", entity));
    }
    public ValueGetter<Double> maxTemp()
    {   return maxTemp;
    }
    public double maxTemp(ItemStack stack, Entity entity)
    {   return maxTemp.get(Map.of("item", stack, "entity", entity));
    }
    public ValueGetter<Double> minTemp()
    {   return minTemp;
    }
    public double minTemp(ItemStack stack, Entity entity)
    {   return minTemp.get(Map.of("item", stack, "entity", entity));
    }
    public NegatableList<EntityRequirement> entityRequirement()
    {   return entityRequirement;
    }
    public AttributeModifierMap attributeModifiers()
    {   return attributeModifiers;
    }
    public Map<ResourceLocation, ValueGetter<Double>> immuneTempModifiers()
    {   return immuneTempModifiers;
    }
    public Map<ResourceLocation, Double> immuneTempModifiers(ItemStack stack, Entity entity)
    {
        Map<ResourceLocation, Double> result = new HashMap<>();
        for (Map.Entry<ResourceLocation, ValueGetter<Double>> entry : immuneTempModifiers.entrySet())
        {   result.put(entry.getKey(), entry.getValue().get(Map.of("item", stack, "entity", entity)));
        }
        return result;
    }
    public ValueGetter<Boolean> hideIfUnmet()
    {   return hideIfUnmet;
    }
    public boolean hideIfUnmet(ItemStack stack, Entity entity)
    {   return hideIfUnmet.get(Map.of("item", stack, "entity", entity));
    }

    @Override
    public boolean test(Entity entity)
    {   return entityRequirement.test(rq -> rq.test(entity));
    }

    @Override
    public boolean test(ItemStack stack)
    {   return item.test(rq -> rq.test(stack, true));
    }

    public boolean test(Entity entity, ItemStack stack, int slot, @Nullable EquipmentSlot equipmentSlot)
    {   return test(entity) && test(stack) && test(slot, equipmentSlot);
    }

    public boolean test(Entity entity, ItemStack stack, SlotType slot)
    {
        if (!this.test(entity) || !this.item().test(rq -> rq.test(stack, true)))
        {   return false;
        }
        for (int i = 0; i < this.slots().size(); i++)
        {
            Optional<SlotType> slotType = this.slots().get(i).right();
            if (slotType.isPresent() && slotType.get().equals(slot))
            {   return true;
            }
        }
        return false;
    }

    public boolean test(int slot, @Nullable EquipmentSlot equipmentSlot)
    {
        if (slot == -1 && equipmentSlot == null)
        {   return true;
        }
        for (Either<IntegerBounds, SlotType> either : slots)
        {
            if (either.left().isPresent())
            {
                if (slot != -1 && either.left().get().test(slot))
                {   return true;
                }
            }
            else if (either.right().isPresent())
            {
                if (equipmentSlot != null && either.right().get().matches(equipmentSlot)
                || slot != -1 && either.right().get().matches(slot))
                {   return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public static ItemTempData fromToml(List<?> entry)
    {
        if (entry.size() < 4)
        {   ColdSweat.LOGGER.error("Error parsing item temp config: not enough arguments");
            return null;
        }
        NegatableList<Either<TagKey<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty())
        {   return null;
        }
        //temp
        ValueGetter<Double> temp = ValueGetter.parse(() -> entry.get(1), ExtraCodecs.DOUBLE, 0.0);
        // slots
        List<Either<IntegerBounds, SlotType>> slotTypes = Arrays.stream(((String) entry.get(2)).split(","))
                                                          .map(String::trim).map(SlotType::byName)
                                                          .map(Either::<IntegerBounds, SlotType>right).toList();
        if (slotTypes.isEmpty())
        {   ColdSweat.LOGGER.error("Error parsing item temp config: \"{}\" is not a valid slot type", entry.get(2));
            return null;
        }
        // trait
        Temperature.Trait trait = Temperature.Trait.fromID((String) entry.get(3));
        // nbt
        NbtRequirement nbtRequirement = entry.size() > 4
                                        ? new NbtRequirement(NBTHelper.parseCompoundNbt((String) entry.get(4)))
                                        : new NbtRequirement(new CompoundTag());
        // max effect
        ValueGetter<Double> maxEffect = ValueGetter.parse(() -> entry.get(5), ExtraCodecs.DOUBLE, Double.POSITIVE_INFINITY);
        // hide if unmet
        ValueGetter<Boolean> hideIfUnmet = ValueGetter.parse(() -> entry.get(6), Codec.BOOL, false);
        // compile item requirement
        ItemRequirement itemRequirement = new ItemRequirement(items, nbtRequirement);

        ItemTempData result = new ItemTempData(new NegatableList<>(itemRequirement), slotTypes, temp, trait, maxEffect,
                                               ValueGetter.constant(Double.POSITIVE_INFINITY), ValueGetter.constant(Double.NEGATIVE_INFINITY),
                                               new NegatableList<>(), new AttributeModifierMap(), new HashMap<>(), hideIfUnmet);
        result.setConfigType(Type.TOML);
        return result;
    }

    public String getSlotRangeName()
    {
        if (slots.size() != 1 || slots.get(0).left().isPresent())
        {   return "";
        }
        return slots.get(0).right().get().getSerializedName();
    }

    @Override
    public Codec<ItemTempData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ItemTempData that = (ItemTempData) obj;
        return super.equals(obj)
            && temperature == that.temperature
            && item.equals(that.item)
            && slots.equals(that.slots)
            && trait.equals(that.trait)
            && maxEffect.equals(that.maxEffect)
            && entityRequirement.equals(that.entityRequirement)
            && attributeModifiers.equals(that.attributeModifiers)
            && immuneTempModifiers.equals(that.immuneTempModifiers)
            && hideIfUnmet == that.hideIfUnmet;
    }

    public enum SlotType implements StringRepresentable
    {
        HEAD("head", List.of(Either.right(EquipmentSlot.HEAD))),
        CHEST("chest", List.of(Either.right(EquipmentSlot.CHEST))),
        LEGS("legs", List.of(Either.right(EquipmentSlot.LEGS))),
        FEET("feet", List.of(Either.right(EquipmentSlot.FEET))),
        INVENTORY("inventory", List.of(Either.left(IntegerBounds.NONE))),
        HOTBAR("hotbar", List.of(Either.left(new IntegerBounds(0, 8)))),
        CURIO("curio", List.of()),
        HAND("hand", List.of(Either.right(EquipmentSlot.MAINHAND), Either.right(EquipmentSlot.OFFHAND)));

        public static final Codec<SlotType> CODEC = ExtraCodecs.enumIgnoreCase(values());

        private final String name;
        private final List<Either<IntegerBounds, EquipmentSlot>> slots;

        SlotType(String name, List<Either<IntegerBounds, EquipmentSlot>> slots)
        {   this.name = name;
            this.slots = slots;
        }

        public List<Either<IntegerBounds, EquipmentSlot>> getSlots()
        {   return slots;
        }

        public boolean matches(int slotId)
        {
            for (int i = 0; i < slots.size(); i++)
            {
                Either<IntegerBounds, EquipmentSlot> either = slots.get(i);
                if (either.left().isPresent() && either.left().get().test(slotId))
                {   return true;
                }
            }
            return false;
        }

        public boolean matches(EquipmentSlot slot)
        {
            for (int i = 0; i < slots.size(); i++)
            {
                Either<IntegerBounds, EquipmentSlot> either = slots.get(i);
                if (either.right().isPresent() && either.right().get().equals(slot))
                {   return true;
                }
            }
            return false;
        }

        @Override
        public String getSerializedName()
        {   return name;
        }

        public String getFormattedName()
        {   return Component.translatable(String.format("tooltip.cold_sweat.slot_type.%s", this.getSerializedName())).getString();
        }

        public static SlotType byName(String name)
        {   return EnumHelper.byName(values(), name);
        }


        public static SlotType fromEquipment(EquipmentSlot slot)
        {
            return switch (slot)
            {
                case HEAD -> HEAD;
                case CHEST -> CHEST;
                case LEGS -> LEGS;
                case FEET -> FEET;
                default -> HAND;
            };
        }
    }
}
