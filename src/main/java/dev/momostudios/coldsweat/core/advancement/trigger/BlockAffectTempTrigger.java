package dev.momostudios.coldsweat.core.advancement.trigger;

import com.google.gson.JsonObject;
import dev.momostudios.coldsweat.ColdSweat;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class BlockAffectTempTrigger extends SimpleCriterionTrigger<BlockAffectTempTrigger.Instance>
{
    static final ResourceLocation ID = new ResourceLocation(ColdSweat.MOD_ID, "block_affects_temperature");

    @Override
    protected Instance createInstance(JsonObject json, EntityPredicate.Composite player, DeserializationContext context)
    {
        double distance = json.has("distance") ? json.get("distance").getAsDouble() : 0;
        double totalEffect = json.has("total_effect") ? json.get("total_effect").getAsDouble() : 0;
        BlockPredicate block = BlockPredicate.fromJson(json.get("blocks"));
        return new Instance(player, block, distance, totalEffect);
    }

    @Override
    public ResourceLocation getId()
    {   return ID;
    }

    public void trigger(ServerPlayer player, BlockPos pos, double distance, double totalEffect)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(player.getLevel(), pos, distance, totalEffect));
    }

    public static class Instance extends AbstractCriterionTriggerInstance
    {
        BlockPredicate block;
        MinMaxBounds.Doubles distance;
        MinMaxBounds.Doubles totalEffect;

        public Instance(EntityPredicate.Composite player, BlockPredicate block, double distance, double totalEffect)
        {
            super(ID, player);
            this.block = block;
            this.distance = MinMaxBounds.Doubles.atMost(distance);
            this.totalEffect = totalEffect < 0 ? MinMaxBounds.Doubles.atMost(totalEffect) : MinMaxBounds.Doubles.atLeast(totalEffect);
        }

        public boolean matches(ServerLevel level, BlockPos pos, double distance, double totalEffect)
        {
            return this.distance.matches(distance)
                && this.totalEffect.matches(totalEffect)
                && this.block.matches(level, pos);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject obj = super.serializeToJson(context);

            obj.add("distance", this.distance.serializeToJson());
            obj.add("total_effect", this.totalEffect.serializeToJson());
            obj.add("blocks", this.block.serializeToJson());

            return obj;
        }
    }
}
