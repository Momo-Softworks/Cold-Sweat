package com.momosoftworks.coldsweat.data.codec.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.effect.TempEffectType;
import com.momosoftworks.coldsweat.core.init.TempEffectInit;
import com.momosoftworks.coldsweat.data.codec.impl.ConfigData;
import com.momosoftworks.coldsweat.data.codec.impl.RequirementHolder;
import com.momosoftworks.coldsweat.data.codec.requirement.EntityRequirement;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.data.codec.util.NegatableList;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class TempEffectsData extends ConfigData implements RequirementHolder
{
    final NegatableList<EntityRequirement> entity;
    final List<TempEffectHolder> effects;

    public TempEffectsData(NegatableList<EntityRequirement> entity, List<TempEffectHolder> effects, NegatableList<String> requiredMods)
    {
        super(requiredMods);
        this.entity = entity;
        this.effects = effects;
    }

    public TempEffectsData(NegatableList<EntityRequirement> entity, List<TempEffectHolder> effects)
    {   this(entity, effects, new NegatableList<>());
    }

    public static final Codec<TempEffectsData> CODEC = createCodec(RecordCodecBuilder.create(instance -> instance.group(
            NegatableList.codec(EntityRequirement.getCodec()).optionalFieldOf("entity", new NegatableList<>()).forGetter(TempEffectsData::entity),
            TempEffectHolder.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(TempEffectsData::effects)
    ).apply(instance, TempEffectsData::new)));

    public NegatableList<EntityRequirement> entity()
    {   return entity;
    }
    public List<TempEffectHolder> effects()
    {   return effects;
    }

    @Override
    public boolean test(Entity entity)
    {   return this.entity.test(req -> req.test(entity));
    }

    @Override
    public Codec<TempEffectsData> getCodec()
    {   return CODEC;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        TempEffectsData that = (TempEffectsData) obj;
        return super.equals(obj)
            && entity.equals(that.entity)
            && effects.equals(that.effects);
    }

    public static class TempEffectHolder
    {
        private final TempEffectType<?> effect;
        private final IntegerBounds range;

        private static final Codec<TempEffectType<?>> EFFECT_CODEC = ResourceLocation.CODEC.xmap(
                rl -> CSMath.getIfNotNull(TempEffectInit.TEMP_EFFECTS_REGISTRY.get(), reg ->
                {
                    TempEffectType<?> effectType = reg.getValue(rl);
                    if (effectType == null)
                    {   ColdSweat.LOGGER.error("Error parsing temp effect: \"{}\" not found", rl);
                    }
                    return effectType;
                }, null),
                effect -> CSMath.getIfNotNull(TempEffectInit.TEMP_EFFECTS_REGISTRY.get(), reg -> reg.getKey(effect), null));

        public static final Codec<TempEffectHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EFFECT_CODEC.fieldOf("effect").forGetter(TempEffectHolder::effect),
                IntegerBounds.CODEC.optionalFieldOf("range", IntegerBounds.NONE).forGetter(TempEffectHolder::range)
        ).apply(instance, TempEffectHolder::new));

        public TempEffectHolder(TempEffectType<?> effect, IntegerBounds range)
        {   this.effect = effect;
            this.range = range;
        }

        public TempEffectType<?> effect()
        {   return effect;
        }

        public IntegerBounds range()
        {   return range;
        }
    }
}
