package dev.momostudios.coldsweat.client.renderer.model;

import net.minecraft.client.renderer.model.ModelRenderer;

import java.util.HashMap;
import java.util.Map;

public class ModelPart
{
    public String name;
    ModelRenderer self;
    PartPose pose;
    public Map<String, ModelPart> children = new HashMap<>();

    public ModelPart(String name, ModelRenderer self, PartPose pose)
    {   this.name = name;
        this.self = self;
        this.pose = pose;
    }

    public ModelPart addOrReplaceChild(ModelPart part)
    {   children.put(part.name, part);
        self.addChild(part.self);
        return this;
    }

    public ModelPart addOrReplaceChild(String name, ModelRenderer self, PartPose pose)
    {   return addOrReplaceChild(new ModelPart(name, self, pose));
    }

    public ModelPart getChild(String name)
    {   return children.get(name);
    }

    public ModelRenderer getModel()
    {   return self;
    }

    public PartPose getPose()
    {   return pose;
    }

    public void setPose(PartPose pose)
    {   this.pose = pose;
    }

    public Map<String, ModelPart> getAllChildren()
    {   for (ModelPart child : children.values())
        {   children.putAll(child.getAllChildren());
        }
        return children;
    }
}
