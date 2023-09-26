package com.momosoftworks.coldsweat.client.renderer.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnimationBatch
{
    Map<String, Animation> animations = new HashMap<>();

    public AnimationBatch(Animation... animations)
    {
        for (Animation animation : animations)
        {   this.animations.put(animation.partName, animation);
        }
    }

    public void addAnimation(String name, Animation animation)
    {
        animations.put(name, animation);
    }

    public Animation getAnimation(String name)
    {   return animations.computeIfAbsent(name, k -> new Animation(name, 0, new ArrayList<>(), new ArrayList<>()));
    }

    public void animateAll(Map<String, ModelRenderer> parts, float animationTime, boolean offset)
    {
        parts.forEach((name, part) ->
        {   this.getAnimation(name).setPositions(animationTime, part, offset).setRotations(animationTime, part, offset);
        });
    }

    public void animate(String name, ModelRenderer part, float animationTime, boolean offset)
    {   this.getAnimation(name).setPositions(animationTime, part, offset).setRotations(animationTime, part, offset);
    }

    public static AnimationBatch loadFromFile(ResourceLocation path)
    {
        AnimationBatch batch = new AnimationBatch();

        try
        {
            // Load the file pointed by the ResourceLocation
            InputStream inputStream = Minecraft.getInstance().getResourceManager().getResource(path).getInputStream();

            // Parse the file as JSON
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(new InputStreamReader(inputStream)).getAsJsonObject();
            JsonObject animations = obj.getAsJsonObject("animations");

            // Iterate through each animation (there's usually only one)
            for (Map.Entry<String, JsonElement> animation : animations.entrySet())
            {
                JsonObject animationObj = animation.getValue().getAsJsonObject();

                JsonObject bones = animationObj.getAsJsonObject("bones").getAsJsonObject();

                for (Map.Entry<String, JsonElement> boneEntry : bones.entrySet())
                {
                    String boneName = boneEntry.getKey();
                    JsonObject bone = boneEntry.getValue().getAsJsonObject();

                    List<Keyframe> posFrames = new ArrayList<>();
                    if (bone.has("position"))
                    {
                        JsonObject pos = bone.getAsJsonObject("position");
                        if (pos.has("vector"))
                        {
                            JsonArray vector = pos.getAsJsonArray("vector");
                            float x = floatFromJSON(vector.get(0));
                            float y = floatFromJSON(vector.get(1));
                            float z = floatFromJSON(vector.get(2));

                            posFrames.add(new Keyframe(0, x, y, z));
                        }
                        else
                        {
                            for (Map.Entry<String, JsonElement> posEntry : bone.getAsJsonObject("position").entrySet())
                            {
                                float timestamp = Float.parseFloat(posEntry.getKey());
                                JsonObject position = posEntry.getValue().getAsJsonObject();
                                JsonArray vector = position.getAsJsonArray("vector");

                                float x = floatFromJSON(vector.get(0));
                                float y = floatFromJSON(vector.get(1));
                                float z = floatFromJSON(vector.get(2));

                                posFrames.add(new Keyframe(timestamp, x, y, z));
                            }
                        }
                    }

                    List<Keyframe> rotFrames = new ArrayList<>();
                    if (bone.has("rotation"))
                    {
                        JsonObject rot = bone.getAsJsonObject("rotation");
                        if (rot.has("vector"))
                        {
                            JsonArray vector = rot.getAsJsonArray("vector");
                            float x = floatFromJSON(vector.get(0));
                            float y = floatFromJSON(vector.get(1));
                            float z = floatFromJSON(vector.get(2));

                            rotFrames.add(new Keyframe(0, x, y, z));
                        }
                        else
                        {
                            for (Map.Entry<String, JsonElement> rotEntry : bone.getAsJsonObject("rotation").entrySet())
                            {
                                float timestamp = Float.parseFloat(rotEntry.getKey());
                                JsonObject rotation = rotEntry.getValue().getAsJsonObject();
                                JsonArray vector = rotation.getAsJsonArray("vector");

                                float x = floatFromJSON(vector.get(0));
                                float y = floatFromJSON(vector.get(1));
                                float z = floatFromJSON(vector.get(2));

                                rotFrames.add(new Keyframe(timestamp, x, y, z));
                            }
                        }
                    }

                    Animation anim = new Animation(boneName, animationObj.get("animation_length").getAsFloat(), posFrames, rotFrames);
                    batch.addAnimation(boneName, anim);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return batch;
    }

    static float floatFromJSON(JsonElement element)
    {
        try
        {   return element.getAsFloat();
        }
        catch (Exception e)
        {
            String str = element.getAsString();
            if (str.equals("-")) return 0;
            return Float.parseFloat(element.getAsString().replaceAll("[^\\d-.]", ""));
        }
    }

    @Override
    public String toString()
    {
        return animations.toString();
    }
}
