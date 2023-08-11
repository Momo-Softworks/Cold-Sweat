package dev.momostudios.coldsweat.client.renderer.animation;

import dev.momostudios.coldsweat.client.renderer.model.PartPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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
            CHILDREN_FIELD = ObfuscationReflectionHelper.findField(ModelRenderer.class, "field_78805_m");
            CHILDREN_FIELD.setAccessible(true);
        } catch (Exception ignored) {}
    }

    public static Map<String, ModelRenderer> getChildrenMap(ModelRenderer part)
    {
        try
        {
            Map<String, ModelRenderer> map = new ConcurrentHashMap<>((Map<String, ModelRenderer>) CHILDREN_FIELD.get(part));
            for (ModelRenderer child : map.values())
            {
                map.putAll(getChildrenMap(child));
            }
            return map;
        } catch (Exception ignored) {}
        return new HashMap<>();
    }

    public static void storeDefaultPoses(EntityType type, Map<String, ModelRenderer> parts)
    {
        DEFAULT_POSES.put(type, parts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, partEntry ->
        {   ModelRenderer part = partEntry.getValue();
            return PartPose.offsetAndRotation(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot);
        })));
    }

    static Map<String, PartPose> loadDefaultPoses(EntityType<?> type, Map<String, ModelRenderer> parts)
    {
        Map<String, PartPose> defaultPoses = DEFAULT_POSES.get(type);
        if (defaultPoses != null)
        {   defaultPoses.forEach((name, pose) ->
            {   ModelRenderer part = parts.get(name);
                part.x = pose.offsetX;
                part.y = pose.offsetY;
                part.z = pose.offsetZ;
                part.xRot = pose.rotationX;
                part.yRot = pose.rotationY;
                part.zRot = pose.rotationZ;
            });
        }
        return defaultPoses;
    }

    public static void saveAnimationStates(Entity entity, Map<String, ModelRenderer> parts)
    {
        ANIMATION_STATES.put(entity, parts.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, partEntry ->
        {   ModelRenderer part = partEntry.getValue();
            return PartPose.offsetAndRotation(part.x, part.y, part.z, part.xRot, part.yRot, part.zRot);
        })));
    }

    public static void loadAnimationStates(Entity entity, Map<String, ModelRenderer> parts)
    {
        Map<String, PartPose> animationStates = ANIMATION_STATES.computeIfAbsent(entity, ent -> loadDefaultPoses(entity.getType(), parts));
        animationStates.forEach((name, pose) ->
        {
            ModelRenderer part = parts.get(name);
            part.x = pose.offsetX;
            part.y = pose.offsetY;
            part.z = pose.offsetZ;
            part.xRot = pose.rotationX;
            part.yRot = pose.rotationY;
            part.zRot = pose.rotationZ;
        });
    }

    public static void animateEntity(Entity entity, BiFunction<Float, Float, Float> animator)
    {
        float timer = ANIMATION_TIMERS.computeIfAbsent(entity, ent -> 0f);
        ANIMATION_TIMERS.put(entity, animator.apply(timer, Minecraft.getInstance().getDeltaFrameTime() / 20));
    }
}
