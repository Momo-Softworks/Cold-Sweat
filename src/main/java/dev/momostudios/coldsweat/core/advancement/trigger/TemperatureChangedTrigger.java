package dev.momostudios.coldsweat.core.advancement.trigger;

import com.google.gson.JsonObject;

import dev.momostudios.coldsweat.ColdSweat;
import dev.momostudios.coldsweat.api.util.Temperature;
import dev.momostudios.coldsweat.util.config.ConfigSettings;
import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TemperatureChangedTrigger extends SimpleCriterionTrigger<TemperatureChangedTrigger.Instance>
{
    static final ResourceLocation ID = new ResourceLocation(ColdSweat.MOD_ID, "temperature");

    @Override
    protected Instance createInstance(JsonObject json, EntityPredicate.Composite player, DeserializationContext context)
    {
        Temperature.Type type = Temperature.Type.fromID(json.get("type").getAsString());
        JsonObject values = json.getAsJsonObject("temperature");
        double below = Double.MAX_VALUE;
        if (values.has("below"))
        {
            try
            {   below = values.get("below").getAsDouble();
            }
            catch (Exception e)
            {
                String builtinValue = values.get("below").getAsString();
                if (builtinValue.equals("max_habitable"))
                    below = ConfigSettings.MAX_TEMP.get();
                else if (builtinValue.equals("min_habitable"))
                    below = ConfigSettings.MIN_TEMP.get();
            }
        }

        double above = -Double.MAX_VALUE;
        if (values.has("above"))
        {
            try
            {   above = values.get("above").getAsDouble();
            }
            catch (Exception e)
            {
                String builtinValue = values.get("above").getAsString();
                if (builtinValue.equals("max_habitable"))
                    above = ConfigSettings.MAX_TEMP.get();
                else if (builtinValue.equals("min_habitable"))
                    above = ConfigSettings.MIN_TEMP.get();
            }
        }

        return new Instance(player, type, below, above);
    }

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    public void trigger(ServerPlayer player, Temperature.Type type, double value)
    {
        this.trigger(player, triggerInstance -> triggerInstance.matches(type, value));
    }

    public static class Instance extends AbstractCriterionTriggerInstance
    {
        Temperature.Type type;
        double below;
        double above;

        public Instance(EntityPredicate.Composite player, Temperature.Type type, double below, double above)
        {
            super(ID, player);
            this.type = type;
            this.below = below;
            this.above = above;
        }

        public boolean matches(Temperature.Type type, double value)
        {
            if (type != this.type) return false;

            if (below <= above)
            {   return !CSMath.isInRange(value, below, above);
            }
            else
            {   return CSMath.isInRange(value, above, below);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context)
        {
            JsonObject obj = super.serializeToJson(context);

            obj.addProperty("type", type.getID());
            JsonObject values = new JsonObject();
            if (below != Double.MAX_VALUE)
                values.addProperty("below", below);
            if (above != -Double.MAX_VALUE)
                values.addProperty("above", above);
            obj.add("temperature", values);

            return obj;
        }
    }
}

