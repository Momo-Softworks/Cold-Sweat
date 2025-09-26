package com.momosoftworks.coldsweat.client.renderer;

import com.google.gson.JsonSyntaxException;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostProcessShaderManager implements AutoCloseable
{
    private static final PostProcessShaderManager INSTANCE = new PostProcessShaderManager(
            Minecraft.getInstance().getMainRenderTarget(),
            Minecraft.getInstance().getResourceManager());

    private static final Map<String, ShaderGroup> ACTIVE_EFFECTS = new HashMap<>();
    private final Framebuffer mainTarget;
    private final IResourceManager resourceManager;
    private boolean screenSizeUpdated = false;

    public static final ResourceLocation BLOBS = new ResourceLocation("minecraft", "shaders/post/blobs2.json");

    public PostProcessShaderManager(Framebuffer mainTarget, IResourceManager resourceManager)
    {
        this.mainTarget = mainTarget;
        this.resourceManager = resourceManager;
    }

    public static PostProcessShaderManager getInstance()
    {   return INSTANCE;
    }

    public void loadEffect(String id, ResourceLocation shaderLocation)
    {
        try
        {
            // Create new PostChain for this effect
            ShaderGroup postChain = new ShaderGroup(
                    Minecraft.getInstance().getTextureManager(),
                    resourceManager,
                    mainTarget,
                    shaderLocation
            );

            // Store it in our active effects
            if (ACTIVE_EFFECTS.put(id, postChain) != null)
            {
                // If we're replacing an existing effect, close the old one
                closeEffect(id);
            }

        }
        catch (IOException | JsonSyntaxException e)
        {   ColdSweat.LOGGER.error("Failed to load shader effect: " + id, e);
        }
    }

    public void closeEffect(String id)
    {
        ShaderGroup chain = ACTIVE_EFFECTS.remove(id);
        if (chain != null)
        {
            chain.close();
        }
    }

    public void process(float partialTicks)
    {
        // Process all active effects
        for (ShaderGroup chain : ACTIVE_EFFECTS.values())
        {
            if (getPostPasses(chain).stream().anyMatch(pass -> getOrthoMatrix(pass) == null))
            {   chain.resize(mainTarget.width, mainTarget.height);
            }
            chain.process(partialTicks);
        }
    }

    public void resize(int width, int height)
    {
        // Resize all active effects
        for (ShaderGroup chain : ACTIVE_EFFECTS.values())
        {
            chain.resize(width, height);
        }
    }

    public boolean hasEffect(String id)
    {   return ACTIVE_EFFECTS.containsKey(id);
    }

    public ShaderGroup getEffect(String id)
    {   return ACTIVE_EFFECTS.get(id);
    }

    private static final Field POST_PASSES = ObfuscationReflectionHelper.findField(ShaderGroup.class, "field_148031_d");
    private static final Field ORTHO_MATRIX = ObfuscationReflectionHelper.findField(Shader.class, "field_148053_h");
    static
    {   POST_PASSES.setAccessible(true);
        ORTHO_MATRIX.setAccessible(true);
    }

    public List<Shader> getPostPasses(String effectId)
    {
        ShaderGroup chain = ACTIVE_EFFECTS.get(effectId);
        if (chain == null)
        {   return new ArrayList<>();
        }
        try
        {  return (List<Shader>) POST_PASSES.get(chain);
        }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Failed to get post passes for effect: " + effectId, e);
            return new ArrayList<>();
        }
    }

    public static List<Shader> getPostPasses(ShaderGroup chain)
    {
        try
        {  return (List<Shader>) POST_PASSES.get(chain);
        }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Failed to get post passes for effect: " + chain, e);
            return new ArrayList<>();
        }
    }

    public static Matrix4f getOrthoMatrix(Shader pass)
    {
        try
        {  return (Matrix4f) ORTHO_MATRIX.get(pass);
        }
        catch (Exception e)
        {
            ColdSweat.LOGGER.error("Failed to get ortho matrix for pass: " + pass, e);
            return new Matrix4f();
        }
    }

    @Override
    public void close()
    {
        // Clean up all effects
        for (ShaderGroup chain : ACTIVE_EFFECTS.values())
        {
            chain.close();
        }
        ACTIVE_EFFECTS.clear();
    }
}