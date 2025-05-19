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
import com.momosoftworks.coldsweat.data.codec.util.StreamCodecs;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InsulatorData extends ConfigData implements RequirementHolder
{
    final NegatableList<ItemRequirement> item;
    final Insulation.Slot slot;
    final List<Insulation> insulation;
    final NegatableList<EntityRequirement> entity;
    final AttributeModifierMap attributes;
    final Map<ResourceLocation, Double> immuneTempModifiers;
    final boolean fillSlots;
    final boolean hideIfUnmet;

    public InsulatorData(NegatableList<ItemRequirement> item, Insulation.Slot slot,
                         List<Insulation> insulation, NegatableList<EntityRequirement> entity,
                         AttributeModifierMap attributes, Map<ResourceLocation, Double> immuneTempModifiers,
                         boolean fillSlots, boolean hideIfUnmet, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.item = item;
        this.slot = slot;
        this.insulation = insulation;
        this.entity = entity;
        this.attributes = attributes;
        this.immuneTempModifiers = immuneTempModifiers;
        this.fillSlots = fillSlots;
        this.hideIfUnmet = hideIfUnmet;
    }

    public InsulatorData(NegatableList<ItemRequirement> item, Insulation.Slot slot, List<Insulation> insulation,
                         NegatableList<EntityRequirement> entity, AttributeModifierMap attributes,
                         Map<ResourceLocation, Double> immuneTempModifiers, boolean fillSlots, boolean hideIfUnmet)
    {
        this(item, slot, insulation, entity, attributes, immuneTempModifiers, fillSlots, hideIfUnmet, new NegatableList<>());
    }

    private static final Codec<List<Insulation>> INSULATION_CODEC = Codec.either(Insulation.getCodec().listOf(), Insulation.getCodec())
            .xmap(either -> either.map(left -> left.stream().filter(insul -> !insul.isEmpty()).toList(),
                                       right -> right.isEmpty() ? List.of() : List.of(right)),
                  list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list));

    public static final Codec<InsulatorData> CODEC = createCodec(RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(InsulatorData::item),
            Insulation.Slot.CODEC.fieldOf("type").forGetter(InsulatorData::slot),
            INSULATION_CODEC.fieldOf("insulation").forGetter(InsulatorData::insulation),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(InsulatorData::entity),
            AttributeModifierMap.CODEC.optionalFieldOf("attributes", new AttributeModifierMap()).forGetter(InsulatorData::attributes),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).optionalFieldOf("immune_temp_modifiers", new HashMap<>()).forGetter(InsulatorData::immuneTempModifiers),
            Codec.BOOL.optionalFieldOf("fill_slots", false).forGetter(InsulatorData::fillSlots),
            Codec.BOOL.optionalFieldOf("hide_if_unmet", false).forGetter(InsulatorData::hideIfUnmet)
    ).apply(instance, InsulatorData::new)));

    public static final StreamCodec<RegistryFriendlyByteBuf, InsulatorData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, InsulatorData> SIMPLE_STREAM_CODEC = StreamCodec.composite(
            Insulation.Slot.STREAM_CODEC,
            InsulatorData::slot,
            StreamCodecs.list(Insulation.getNetworkCodec()),
            InsulatorData::insulation,
            ByteBufCodecs.BOOL,
            InsulatorData::fillSlots,
            ByteBufCodecs.BOOL,
            InsulatorData::hideIfUnmet,
            (slot, insulation, fillSlots, hideIfUnmet) ->
                    new InsulatorData(new NegatableList<>(), slot, insulation, new NegatableList<>(), new AttributeModifierMap(), new HashMap<>(), fillSlots, hideIfUnmet)
    );

    public NegatableList<ItemRequirement> item()
    {   return item;
    }
    public Insulation.Slot slot()
    {   return slot;
    }
    public List<Insulation> insulation()
    {   return insulation;
    }
    public NegatableList<EntityRequirement> entity()
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
    public boolean hideIfUnmet()
    {   return hideIfUnmet;
    }

    public double getCold()
    {   return insulation.stream().mapToDouble(Insulation::getCold).sum();
    }
    public double getHeat()
    {   return insulation.stream().mapToDouble(Insulation::getHeat).sum();
    }

    @Override
    public boolean test(ItemStack stack)
    {   return item.test(rq -> rq.test(stack, true));
    }

    @Override
    public boolean test(Entity entity)
    {   return entity == null || this.entity.test(rq -> rq.test(entity));
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

        boolean adaptive = entry.size() > 3 && entry.get(3).equals("adaptive");

        List<Insulation> insulation = new ArrayList<>();
        if (!adaptive)
        {
            // Error checking
            if (!(entry.get(1) instanceof Number || entry.get(1) instanceof List<?> list && list.stream().allMatch(val -> val instanceof Number)))
            {   ColdSweat.LOGGER.error("Error parsing {} insulator config: invalid cold insulation value: {}", slot.getSerializedName(), entry.get(1));
                return null;
            }
            if (!(entry.get(2) instanceof Number || entry.get(2) instanceof List<?> list && list.stream().allMatch(val -> val instanceof Number)))
            {   ColdSweat.LOGGER.error("Error parsing {} insulator config: invalid heat insulation valueL {}", slot.getSerializedName(), entry.get(2));
                return null;
            }
            // Create/combine list of cold & hot insulation
            List<Number> insulVal1 = entry.get(1) instanceof List ? (List<Number>) entry.get(1) : List.of((Number) entry.get(1));
            List<Number> insulVal2 = entry.get(2) instanceof List ? (List<Number>) entry.get(2) : List.of((Number) entry.get(2));
            List<Insulation> coldList = insulVal1.stream().map(val -> new StaticInsulation(val.doubleValue(), 0)).collect(Collectors.toList());
            List<Insulation> hotList = insulVal2.stream().map(val -> new StaticInsulation(0, val.doubleValue())).collect(Collectors.toList());

            insulation.addAll(Insulation.combine(coldList, hotList));
        }
        else
        {   insulation.add(new AdaptiveInsulation(((Number) entry.get(1)).doubleValue(), ((Number) entry.get(2)).doubleValue()));
        }

        ItemComponentsRequirement components = entry.size() > 4 ? ItemComponentsRequirement.parse((String) entry.get(4)) : new ItemComponentsRequirement();
        boolean multiSlot = entry.size() > 5 && (Boolean) entry.get(5);

        ItemRequirement itemRequirement = new ItemRequirement(items, components);

        return new InsulatorData(new NegatableList<>(itemRequirement), slot, insulation, new NegatableList<>(), new AttributeModifierMap(), new HashMap<>(), multiSlot, false);
    }

    public InsulatorData copy()
    {   return new InsulatorData(this.item, this.slot, Insulation.deepCopy(this.insulation), this.entity,
                                 this.attributes, new HashMap<>(this.immuneTempModifiers), this.fillSlots, this.hideIfUnmet);
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
