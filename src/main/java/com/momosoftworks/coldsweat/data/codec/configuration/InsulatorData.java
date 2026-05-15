package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.insulation.AdaptiveInsulation;
import com.momosoftworks.coldsweat.api.insulation.Insulation;
import com.momosoftworks.coldsweat.api.insulation.StaticInsulation;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.ItemRequirement;
import com.momosoftworks.coldsweat.data.codec.requirement.NbtRequirement;
import com.momosoftworks.coldsweat.data.codec.util.AttributeModifierMap;
import com.momosoftworks.coldsweat.data.codec.util.ExtraCodecs;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.serialization.ConfigHelper;
import com.momosoftworks.coldsweat.util.serialization.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class InsulatorData extends ConfigData implements RequirementHolder, IForgeRegistryEntry<InsulatorData>
{
    final NegatableList<ItemRequirement> item;
    final Insulation.Slot slot;
    final List<Insulation> insulation;
    final NegatableList<EntityRequirement> entity;
    final AttributeModifierMap attributes;
    final Map<ResourceLocation, Double> immuneTempModifiers;
    final boolean fillSlots;
    final boolean hideIfUnmet;
    final Optional<HintText> hint;

    public InsulatorData(NegatableList<ItemRequirement> item, Insulation.Slot slot,
                         List<Insulation> insulation, NegatableList<EntityRequirement> entity,
                         AttributeModifierMap attributes, Map<ResourceLocation, Double> immuneTempModifiers,
                         boolean fillSlots, boolean hideIfUnmet, Optional<HintText> hint, NegatableList<String> requiredMods)
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
        this.hint = hint;
    }

    public InsulatorData(NegatableList<ItemRequirement> item, Insulation.Slot slot, List<Insulation> insulation,
                         NegatableList<EntityRequirement> entity, AttributeModifierMap attributes,
                         Map<ResourceLocation, Double> immuneTempModifiers, boolean fillSlots, boolean hideIfUnmet, Optional<HintText> hint)
    {
        this(item, slot, insulation, entity, attributes, immuneTempModifiers, fillSlots, hideIfUnmet, hint, new NegatableList<>());
    }

    private static final Codec<List<Insulation>> INSULATION_CODEC = Codec.either(Insulation.getCodec().listOf(), Insulation.getCodec())
            .xmap(either -> either.map(left -> left.stream().filter(insul -> !insul.isEmpty()).toList(),
                                       right -> right.isEmpty() ? List.of() : List.of(right)),
                  list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list));

    public static final Codec<InsulatorData> CODEC = createCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
            NegatableList.codec(ItemRequirement.CODEC).optionalFieldOf("item", new NegatableList<>()).forGetter(InsulatorData::item),
            Insulation.Slot.CODEC.fieldOf("type").forGetter(InsulatorData::slot),
            INSULATION_CODEC.optionalFieldOf("insulation", List.of()).forGetter(InsulatorData::insulation),
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(InsulatorData::entity),
            AttributeModifierMap.CODEC.optionalFieldOf("attributes", new AttributeModifierMap()).forGetter(InsulatorData::attributes),
            Codec.unboundedMap(ResourceLocation.CODEC, ExtraCodecs.DOUBLE).optionalFieldOf("immune_temp_modifiers", new HashMap<>()).forGetter(InsulatorData::immuneTempModifiers),
            Codec.BOOL.optionalFieldOf("fill_slots", true).forGetter(InsulatorData::fillSlots),
            Codec.BOOL.optionalFieldOf("hide_if_unmet", false).forGetter(InsulatorData::hideIfUnmet),
            HintText.CODEC.optionalFieldOf("hint").forGetter(InsulatorData::hint)
    ).apply(instance, InsulatorData::new)));

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
    public Optional<HintText> hint()
    {   return hint;
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
        NegatableList<Either<TagKey<Item>, Item>> items = ConfigHelper.getItems((String) entry.get(0));
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

        CompoundTag tag = entry.size() > 4 ? NBTHelper.parseCompoundNbt((String) entry.get(4)) : new CompoundTag();
        boolean fillSlots = entry.size() <= 5 || (Boolean) entry.get(5);

        ItemRequirement itemRequirement = new ItemRequirement(items, new NbtRequirement(tag));

        InsulatorData result = new InsulatorData(new NegatableList<>(itemRequirement), slot, insulation, new NegatableList<>(),
                                                 new AttributeModifierMap(), new HashMap<>(), fillSlots, false, Optional.empty());
        result.setConfigType(Type.TOML);
        return result;
    }

    public InsulatorData copy()
    {   return new InsulatorData(this.item, this.slot, Insulation.deepCopy(this.insulation), this.entity,
                                 this.attributes, new HashMap<>(this.immuneTempModifiers), this.fillSlots,
                                 this.hideIfUnmet, this.hint);
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

    @Override
    public InsulatorData setRegistryName(ResourceLocation name)
    {
        return this;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return new ResourceLocation("coldsweat", "insulators");
    }

    @Override
    public Class<InsulatorData> getRegistryType()
    {
        return InsulatorData.class;
    }

    public static final class HintText
    {
        private final Optional<String> key;
        private final Optional<String> text;

        public static final Codec<HintText> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("key").forGetter(HintText::key),
                Codec.STRING.optionalFieldOf("text").forGetter(HintText::text)
        ).apply(instance, HintText::new));

        public HintText(Optional<String> key, Optional<String> text)
        {
            this.key = key;
            this.text = text;
        }

        public Optional<String> key()
        {   return key;
        }

        public Optional<String> text()
        {   return text;
        }

        public MutableComponent getText()
        {   return key.<MutableComponent>map(TranslatableComponent::new).orElse(text.map(TextComponent::new).orElse(new TextComponent("")));
        }
    }
}
