package dev.momostudios.coldsweat.client.renderer.animation;

import dev.momostudios.coldsweat.util.math.CSMath;
import net.minecraft.client.renderer.model.ModelRenderer;

import java.util.ArrayList;
import java.util.List;

public class Animation
{
    ArrayList<Keyframe> posKeyframes;
    ArrayList<Keyframe> rotKeyframes;
    float duration;
    float offsetAmount;
    String partName;

    public Animation(String partName, float durationSecs, List<Keyframe> posFrames, List<Keyframe> rotFrames)
    {
        this.partName = partName;
        this.duration = durationSecs;
        this.posKeyframes = new ArrayList<>(posFrames);
        this.rotKeyframes = new ArrayList<>(rotFrames);
    }

    public Animation setRotations(float time, ModelRenderer part, boolean offset)
    {
        if (rotKeyframes.isEmpty()) return this;

        Keyframe next = rotKeyframes.get(0);
        Keyframe prev = rotKeyframes.get(rotKeyframes.size() - 1);
        float animProgress = time % duration;

        for (int i = 0; i < rotKeyframes.size(); i++)
        {
            Keyframe frame = rotKeyframes.get(i);
            if (frame.time >= animProgress)
            {
                next = frame;
                prev = rotKeyframes.get(i == 0 ? rotKeyframes.size() - 1 : i - 1);
                break;
            }
        }

        part.xRot = CSMath.toRadians(CSMath.blend(prev.x, next.x, animProgress, prev.time, next.time)) + (offset ? part.xRot : 0);
        part.yRot = CSMath.toRadians(CSMath.blend(prev.y, next.y, animProgress, prev.time, next.time)) + (offset ? part.yRot : 0);
        part.zRot = CSMath.toRadians(CSMath.blend(prev.z, next.z, animProgress, prev.time, next.time)) + (offset ? part.zRot : 0);
        return this;
    }

    public Animation setPositions(float time, ModelRenderer part, boolean offset)
    {
        if (posKeyframes.isEmpty()) return this;

        Keyframe next = posKeyframes.get(0);
        Keyframe prev = posKeyframes.get(posKeyframes.size() - 1);
        float animProgress = (time + offsetAmount) % duration;

        for (int i = 0; i < posKeyframes.size(); i++)
        {
            Keyframe frame = posKeyframes.get(i);
            if (frame.time >= animProgress)
            {
                next = frame;
                prev = posKeyframes.get(i == 0 ? posKeyframes.size() - 1 : i - 1);
                break;
            }
        }

        part.x = CSMath.blend(prev.x, next.x, animProgress, prev.time, next.time) + (offset ? part.x : 0);
        part.y = CSMath.blend(prev.y, next.y, animProgress, prev.time, next.time) + (offset ? part.y : 0);
        part.z = CSMath.blend(prev.z, next.z, animProgress, prev.time, next.time) + (offset ? part.z : 0);

        return this;
    }

    public Animation offset(float amount)
    {
        this.offsetAmount = amount;
        return this;
    }

    public void addKeyframe(Keyframe keyframe, KeyframeType type)
    {
        if (type == KeyframeType.POSITION)
            posKeyframes.add(keyframe);
        else
            rotKeyframes.add(keyframe);
    }

    public enum KeyframeType
    {
        POSITION,
        ROTATION
    }

    @Override
    public String toString()
    {
        return String.format("Animation{ partName: %s, duration: %s, offset: %s, positions: { %s } rotations { %s } }", partName, duration, offsetAmount, posKeyframes, rotKeyframes);
    }
}
