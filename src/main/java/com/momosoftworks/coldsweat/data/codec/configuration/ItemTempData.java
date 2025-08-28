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
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.FastMap;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemTempData extends ConfigData implements RequirementHolder, IForgeRegistryEntry<ItemTempData>
{
    final NegatableList<ItemRequirement> item;
    final List<Either<IntegerBounds, SlotType>> slots;
    final double temperature;
    final Temperature.Trait trait;
    final Double maxEffect;
    final NegatableList<EntityRequirement> entityRequirement;
    final AttributeModifierMap attributeModifiers;
    final Map<ResourceLocation, Double> immuneTempModifiers;

    public ItemTempData(NegatableList<ItemRequirement> item, List<Either<IntegerBounds, SlotType>> slots, double temperature,
                        Temperature.Trait trait, Double maxEffect, NegatableList<EntityRequirement> entityRequirement, AttributeModifierMap attributeModifiers,
                        Map<ResourceLocation, Double> immuneTempModifiers, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.item = item;
        this.slots = slots;
        this.temperature = temperature;
        this.trait = trait;
        this.maxEffect = maxEffect;
        this.entityRequirement = entityRequirement;
        this.attributeModifiers = attributeModifiers;
        this.immuneTempModifiers = immuneTempModifiers;
    }

    public ItemTempData(NegatableList<ItemRequirement> item, List<Either<IntegerBounds, SlotType>> slots, double temperature,
                        Temperature.Trait trait, Double maxEffect, NegatableList<EntityRequirement> entityRequirement, AttributeModifierMap attributeModifiers,
                        Map<ResourceLocation, Double> immuneTempModifiers)
    {
        this(item, slots, temperature, trait, maxEffect, entityRequirement, attributeModifiers, immuneTempModifiers, new NegatableList<>());
    }

    public static final Codec<ItemTempData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(ItemTempData::item),
            Codec.either(IntegerBounds.CODEC, SlotType.CODEC).listOf().fieldOf("slots").forGetter(ItemTempData::slots),
            Codec.DOUBLE.fieldOf("temperature").forGetter(ItemTempData::temperature),
            Temperature.Trait.CODEC.optionalFieldOf("trait", Temperature.Trait.WORLD).forGetter(ItemTempData::trait),
            Codec.DOUBLE.optionalFieldOf("max_effect", Double.POSITIVE_INFINITY).forGetter(ItemTempData::maxEffect),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(ItemTempData::entityRequirement),
            AttributeModifierMap.CODEC.optionalFieldOf("attributes", new AttributeModifierMap()).forGetter(ItemTempData::attributeModifiers),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).optionalFieldOf("immune_temp_modifiers", new HashMap<>()).forGetter(ItemTempData::immuneTempModifiers)
    ).apply(instance, ItemTempData::new)));

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public List<Either<IntegerBounds, SlotType>> slots()
    {   return slots;
    }
    public double temperature()
    {   return temperature;
    }
    public Temperature.Trait trait()
    {   return trait;
    }
    public Double maxEffect()
    {   return maxEffect;
    }
    public NegatableList<EntityRequirement> entityRequirement()
    {   return entityRequirement;
    }
    public AttributeModifierMap attributeModifiers()
    {   return attributeModifiers;
    }
    public Map<ResourceLocation, Double> immuneTempModifiers()
    {   return immuneTempModifiers;
    }

    @Override
    public boolean test(Entity entity)
    {   return entityRequirement.test(rq -> rq.test(entity));
    }

    public boolean test(Entity entity, ItemStack stack, int slot, @Nullable EquipmentSlot equipmentSlot)
    {   return test(stack, slot, equipmentSlot) && test(entity);
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

    public boolean test(ItemStack stack, int slot, @Nullable EquipmentSlot equipmentSlot)
    {
        if (!item.test(rq -> rq.test(stack, true)))
        {   return false;
        }
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
        double temp = ((Number) entry.get(1)).doubleValue();
        // slots
        SlotType slotType = SlotType.byName((String) entry.get(2));
        if (slotType == null)
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
        double maxEffect = entry.size() > 5 ? ((Number) entry.get(5)).doubleValue() : Double.POSITIVE_INFINITY;
        // compile item requirement
        ItemRequirement itemRequirement = new ItemRequirement(items, nbtRequirement);

        return new ItemTempData(new NegatableList<>(itemRequirement), List.of(Either.right(slotType)), temp, trait, maxEffect, new NegatableList<>(), new AttributeModifierMap(), new FastMap<>());
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
            && entityRequirement.equals(that.entityRequirement);
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

        public static final Codec<SlotType> CODEC = StringRepresentable.fromEnum(SlotType::values, SlotType::byName);

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

        public static SlotType byName(String name)
        {
            for (SlotType type : values())
            {
                if (type.name.equals(name))
                {   return type;
                }
            }
            return null;
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

    @Override
    public ItemTempData setRegistryName(ResourceLocation name)
    {   return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName()
    {   return null;
    }

    @Override
    public Class<ItemTempData> getRegistryType()
    {   return ItemTempData.class;
    }
}
