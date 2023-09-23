package dev.momosoftworks.coldsweat.client.renderer.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class AnimationManager
{
    static Field CHILDREN_FIELD = null;

    public static Map<Entity, Float> ANIMATION_TIMERS = new ConcurrentHashMap<>();
    static HashMap<EntityType<?>, Map<String, PartPose>> DEFAULT_POSES = new HashMap<>();
    static Map<Entity, Map<String, PartPose>> ANIMATION_STATES = new ConcurrentHashMap<>();

    static
    {
        try
        {
            CHILDREN_FIELD = ObfuscationReflectionHelper.findField(ModelPart.class, "f_104213_");
            CHILDREN_FIELD.setAccessible(true);
        } catch (Exception ignored) {}
    }

    public static Map<String, ModelPart> getChildrenMap(ModelPart part)
    {
        try
        {
            Map<String, ModelPart> map = new ConcurrentHashMap<>((Map<String, ModelPart>) CHILDREN_FIELD.get(part));
            for (ModelPart child : map.values())
            {
                map.putAll(getChildrenMap(child));
            }
            return map;
        } catch (Exception ignored) {}
        return Map.of();
    }

    public static void storeDefaultPoses(EntityType type, Map<String, ModelPart> parts)
    {
        DEFAULT_POSES.put(type, parts.entrySet().stream().map(partEntry -> Map.entry(partEntry.getKey(), partEntry.getValue().storePose())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    static Map<String, PartPose> loadDefaultPoses(EntityType type, Map<String, ModelPart> parts)
    {
        Map<String, PartPose> defaultPoses = DEFAULT_POSES.get(type);
        if (defaultPoses != null)
        {
            defaultPoses.forEach((name, pose) -> parts.get(name).loadPose(pose));
        }
        return defaultPoses;
    }

    public static void saveAnimationStates(Entity entity, Map<String, ModelPart> parts)
    {
        ANIMATION_STATES.put(entity, parts.entrySet().stream().map(partEntry -> Map.entry(partEntry.getKey(), partEntry.getValue().storePose())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public static void loadAnimationStates(Entity entity, Map<String, ModelPart> parts)
    {
        Map<String, PartPose> animationStates = ANIMATION_STATES.computeIfAbsent(entity, ent -> loadDefaultPoses(entity.getType(), parts));
        animationStates.forEach((name, state) ->
        {
            ModelPart part = parts.get(name);
            if (part != null)
            {
                part.loadPose(state);
            }
        });
    }

    public static void animateEntity(Entity entity, BiFunction<Float, Float, Float> animator)
    {
        float timer = ANIMATION_TIMERS.computeIfAbsent(entity, ent -> 0f);
        ANIMATION_TIMERS.put(entity, animator.apply(timer, Minecraft.getInstance().getDeltaFrameTime() / 20));
    }
}
