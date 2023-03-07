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
        BlockPredicate block = BlockPredicate.fromJson(json.get("blocks"));
        double distance = json.get("distance").getAsDouble();
        return new Instance(player, block, distance);
    }

    @Override
    public ResourceLocation getId()
    {   return ID;
    }

    public void trigger(ServerPlayer player, BlockPos pos, double distance)
    {   this.trigger(player, triggerInstance -> triggerInstance.matches(player.getLevel(), pos, distance));
    }

    public static class Instance extends AbstractCriterionTriggerInstance
    {
        BlockPredicate block;
        double distance;

        public Instance(EntityPredicate.Composite player, BlockPredicate block,  double distance)
        {
            super(ID, player);
            this.block = block;
            this.distance = distance;
        }

        public boolean matches(ServerLevel level, BlockPos pos, double distance)
        {   return distance <= this.distance && block.matches(level, pos);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject obj = super.serializeToJson(context);

            obj.add("blocks", block.serializeToJson());

            return obj;
        }
    }
}
