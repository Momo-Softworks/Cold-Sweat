package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemComponentsRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.data.codec.util.CommonStreamCodecs;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsulatorData extends ConfigData implements RequirementHolder
{
    final ItemRequirement item;
    final Insulation.Slot slot;
    final Insulation insulation;
    final EntityRequirement entity;
    final AttributeModifierMap attributes;
    final Map<ResourceLocation, Double> immuneTempModifiers;
    final boolean fillSlots;

    public InsulatorData(ItemRequirement item, Insulation.Slot slot,
                         Insulation insulation, EntityRequirement entity,
                         AttributeModifierMap attributes, Map<ResourceLocation, Double> immuneTempModifiers,
                         boolean fillSlots, List<String> requiredMods)
    {
        super(requiredMods);
        this.item = item;
        this.slot = slot;
        this.insulation = insulation;
        this.entity = entity;
        this.attributes = attributes;
        this.immuneTempModifiers = immuneTempModifiers;
        this.fillSlots = fillSlots;
    }

    public InsulatorData(ItemRequirement item, Insulation.Slot slot, Insulation insulation,
                         EntityRequirement entity, AttributeModifierMap attributes,
                         Map<ResourceLocation, Double> immuneTempModifiers, boolean fillSlots)
    {
        this(item, slot, insulation, entity, attributes, immuneTempModifiers, fillSlots, ConfigHelper.getModIDs(CSMath.listOrEmpty(item.items()), BuiltInRegistries.ITEM));
    }

    public static final Codec<InsulatorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemRequirement.CODEC.fieldOf("item").forGetter(InsulatorData::item),
            Insulation.Slot.CODEC.fieldOf("type").forGetter(InsulatorData::slot),
            Insulation.getCodec().fieldOf("insulation").forGetter(InsulatorData::insulation),
            EntityRequirement.getCodec().optionalFieldOf("entity", EntityRequirement.NONE).forGetter(InsulatorData::entity),
            AttributeModifierMap.CODEC.optionalFieldOf("attributes", new AttributeModifierMap()).forGetter(InsulatorData::attributes),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).optionalFieldOf("immune_temp_modifiers", new HashMap<>()).forGetter(InsulatorData::immuneTempModifiers),
            Codec.BOOL.optionalFieldOf("fill_slots", false).forGetter(InsulatorData::fillSlots),
            Codec.STRING.listOf().optionalFieldOf("required_mods", List.of()).forGetter(InsulatorData::requiredMods)
    ).apply(instance, InsulatorData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InsulatorData> STREAM_CODEC = StreamCodec.of(
    (buf, insulator) ->
    {
        ItemRequirement.STREAM_CODEC.encode(buf, insulator.item());
        Insulation.getNetworkCodec().encode(buf, insulator.insulation());
        buf.writeEnum(insulator.slot());
        buf.writeNbt(EntityRequirement.getCodec().encode(insulator.entity(), NbtOps.INSTANCE, new CompoundTag()).result().orElse(new CompoundTag()));
        AttributeModifierMap.STREAM_CODEC.encode(buf, insulator.attributes());
        buf.writeMap(insulator.immuneTempModifiers(), ResourceLocation.STREAM_CODEC, CommonStreamCodecs.DOUBLE);
        buf.writeBoolean(insulator.fillSlots());
    },
    (buf) ->
    {
        ItemRequirement item = ItemRequirement.STREAM_CODEC.decode(buf);
        Insulation insulation = Insulation.getNetworkCodec().decode(buf);
        Insulation.Slot slot = buf.readEnum(Insulation.Slot.class);
        EntityRequirement predicate = EntityRequirement.getCodec().decode(NbtOps.INSTANCE, buf.readNbt()).result().orElse(Pair.of(EntityRequirement.NONE, new CompoundTag())).getFirst();
        AttributeModifierMap attributes = AttributeModifierMap.STREAM_CODEC.decode(buf);
        Map<ResourceLocation, Double> immuneTempModifiers = buf.readMap(ResourceLocation.STREAM_CODEC, CommonStreamCodecs.DOUBLE);
        boolean multiSlot = buf.readBoolean();
        return new InsulatorData(item, slot, insulation, predicate, attributes, immuneTempModifiers, multiSlot);
    });

    public ItemRequirement item()
    {   return item;
    }
    public Insulation.Slot slot()
    {   return slot;
    }
    public Insulation insulation()
    {   return insulation;
    }
    public EntityRequirement entity()
    {   return entity;
    }
    public AttributeModifierMap attributes()
    {   return attributes;
    }
    public Map<ResourceLocation, Double> immuneTempModifiers()
    {   return immuneTempModifiers;
    }
    public boolean fillSlots()
    {   return fillSlots;
    }

    @Override
    public boolean test(ItemStack stack)
    {   return item.test(stack, true);
    }

    @Override
    public boolean test(Entity entity)
    {   return entity == null || this.entity.test(entity);
    }

    @Nullable
    public static InsulatorData fromToml(List<?> entry, Insulation.Slot slot)
    {
        if (entry.size() < 3)
        {   ColdSweat.LOGGER.error("Error parsing {} insulator config: not enough arguments", slot.getSerializedName());
            return null;
        }
        List<Either<TagKey<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
        if (items.isEmpty())
        {   return null;
        }
        double insulVal1 = ((Number) entry.get(1)).doubleValue();
        double insulVal2 = ((Number) entry.get(2)).doubleValue();
        boolean adaptive = entry.size() > 3 && entry.get(3).equals("adaptive");
        ItemComponentsRequirement components = entry.size() > 4 ? ItemComponentsRequirement.parse((String) entry.get(4)) : new ItemComponentsRequirement();
        boolean multiSlot = entry.size() > 5 && (Boolean) entry.get(5);

        Insulation insulation = adaptive ? new AdaptiveInsulation(insulVal1, insulVal2)
                                         : new StaticInsulation(insulVal1, insulVal2);

        ItemRequirement itemRequirement = new ItemRequirement(items, components);

        return new InsulatorData(itemRequirement, slot, insulation, EntityRequirement.NONE, new AttributeModifierMap(), new HashMap<>(), multiSlot);
    }

    public InsulatorData copy()
    {   return new InsulatorData(this.item, this.slot, this.insulation.copy(), this.entity, this.attributes, new HashMap<>(this.immuneTempModifiers), this.fillSlots);
    }

    @Override
    public Codec<InsulatorData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        InsulatorData that = (InsulatorData) obj;
        return super.equals(obj)
            && slot == that.slot
            && insulation.equals(that.insulation)
            && item.equals(that.item)
            && entity.equals(that.entity)
            && attributes.equals(that.attributes)
            && immuneTempModifiers.equals(that.immuneTempModifiers);
    }
}
