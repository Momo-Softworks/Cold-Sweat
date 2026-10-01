package com.momosoftworks.coldsweat.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Drives Cold Sweat's screen post-effects through the vanilla post-effect slot.<br>
 * Since 1.21.5, post-effect uniforms are baked into immutable GPU buffers when the chain loads.
 * {@link #setUniforms} swaps a pass's uniform buffer for a writable one so it can be updated at runtime.
 */
public class PostProcessShaderManager
{
    private static final PostProcessShaderManager INSTANCE = new PostProcessShaderManager();

    public static final Identifier HEAT_BLUR = ColdSweat.createKey("heat_blur");

    public static PostProcessShaderManager getInstance()
    {   return INSTANCE;
    }

    /**
     * Activates the given effect, unless it is already active
     */
    public void setEffect(Identifier effect)
    {
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        if (!effect.equals(renderer.currentPostEffect()))
        {   renderer.setPostEffect(effect);
        }
    }

    /**
     * Clears the active post-effect, if it belongs to Cold Sweat
     */
    public void clearEffect()
    {
        GameRenderer renderer = Minecraft.getInstance().gameRenderer;
        if (isColdSweatEffect(renderer.currentPostEffect()))
        {   renderer.clearPostEffect();
        }
    }

    public boolean hasEffect()
    {   return isColdSweatEffect(Minecraft.getInstance().gameRenderer.currentPostEffect());
    }

    /**
     * Overwrites a uniform block of one of the effect's passes.<br>
     * Must be called outside of a render pass, before the post chain is processed for the frame.
     * @param passIndex the index of the pass in the effect's "passes" list
     * @param block the name of the uniform block, as declared in the effect's json
     * @param writer writes the block's values, in the order they are declared in the shader
     */
    public void setUniforms(Identifier effect, int passIndex, String block, Consumer<Std140Builder> writer)
    {
        PostChain chain = Minecraft.getInstance().getShaderManager().getPostChain(effect, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null || passIndex >= chain.passes.size())
        {   return;
        }
        PostPass pass = chain.passes.get(passIndex);
        GpuBuffer buffer = pass.customUniforms.get(block);
        if (buffer == null)
        {   return;
        }
        // Replace the immutable buffer vanilla created with one we can write to
        if ((buffer.usage() & GpuBuffer.USAGE_COPY_DST) == 0)
        {
            GpuBuffer writable = RenderSystem.getDevice().createBuffer(() -> effect + " / " + block, GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, buffer.size());
            buffer.close();
            pass.customUniforms.put(block, writable);
            buffer = writable;
        }
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            Std140Builder builder = Std140Builder.onStack(stack, (int) buffer.size());
            writer.accept(builder);
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), builder.get());
        }
    }

    private static boolean isColdSweatEffect(@Nullable Identifier effect)
    {   return effect != null && effect.getNamespace().equals(ColdSweat.MOD_ID);
    }
}
