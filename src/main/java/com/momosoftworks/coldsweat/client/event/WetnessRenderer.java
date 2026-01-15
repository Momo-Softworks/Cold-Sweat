package com.momosoftworks.coldsweat.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.momosoftworks.coldsweat.ColdSweat;
import com.momosoftworks.coldsweat.api.temperature.modifier.WaterTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.data.codec.util.IntegerBounds;
import com.momosoftworks.coldsweat.util.math.CSMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class WetnessRenderer
{
    private static final ResourceLocation WATER_DROP = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/overlay/droplet.png");
    private static final ResourceLocation WATER_DROP_TRAIL = new ResourceLocation(ColdSweat.MOD_ID, "textures/gui/overlay/droplet_trail.png");
    private static final List<Droplet> WATER_DROPS = new ArrayList<>();
    private static final List<Triplet<Vector2i, Float, Integer>> TRAILS = new ArrayList<>();
    private static boolean WAS_SUBMERGED = false;
    private static int LEFT_DROPLETS = 0;
    private static int RIGHT_DROPLETS = 0;
    private static final int MAX_DROPLETS = 5;

    @SubscribeEvent
    public static void updateSkyBrightness(TickEvent.ClientTickEvent event)
    {
        Level level = Minecraft.getInstance().level;
        if (level != null)
        {   level.updateSkyBrightness();
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Pre event)
    {
        if (!ConfigSettings.WATER_EFFECT_SETTING.get().showGui()) return;

        Minecraft mc = Minecraft.getInstance();
        float frametime = mc.getDeltaFrameTime();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        boolean paused = mc.isPaused();
        int uiScale = mc.options.guiScale().get();
        if (uiScale == 0)
        {   uiScale = mc.getWindow().calculateScale(0, mc.isEnforceUnicode());
        }

        Player player = mc.player;
        if (player == null || player.isSpectator()) return;

        BlockPos playerPos = BlockPos.containing(player.getEyePosition());
        float playerYVelocity = (float) (player.position().y - player.yOld);
        boolean isSubmerged = player.getEyeInFluidType() == Fluids.WATER.getFluidType();

        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

        double midTemp = (ConfigSettings.MIN_TEMP.get() + ConfigSettings.MAX_TEMP.get()) / 2.0;
        float tempMult = (float) CSMath.blend(1, 3, Temperature.get(player, Temperature.Trait.WORLD), midTemp, ConfigSettings.MAX_TEMP.get() * 2);

        // Clear water drops when the player submerges
        if (isSubmerged && !paused)
        {
            TRAILS.clear();
            for (Droplet drop : WATER_DROPS)
            {
                drop.alpha -= 0.6f * frametime;
                float xMoveDir = drop.position.x < screenWidth / 2f ? -1 : 1;
                float yMoveDir = drop.position.y < screenHeight / 2f ? -1 : 1;
                drop.position.add(new Vector2f(xMoveDir, yMoveDir).mul(200 * -playerYVelocity * frametime));
            }
        }

        // Get the player's wetness level
        double wetness = Temperature.getModifier(mc.player, Temperature.Trait.WORLD, WaterTempModifier.class).map(mod ->
                         {   if (Temperature.getImmunityToModifier(player, mod) == 1) return 0d;
                             else return CSMath.blend(0, 1, Math.abs(mod.getTemperature()), 0, 0.2);
                         }).orElse(0d);

        // Spawn a bunch of droplets when the player exits the water
        boolean justExitedWater = WAS_SUBMERGED && !isSubmerged;
        if (justExitedWater)
        {
            for (int i = 0; i < 10; i++)
            {
                Droplet newDrop = createDrop(screenWidth);
                newDrop.yMotion = getRandomVelocity(frametime) / 2 + 0.3f;
                newDrop.position.y = (float) (Math.sin(i*4+player.tickCount) / 2 + 0.5) * screenHeight; // arbitrary wave pattern for particle placement
                newDrop.position.x = (float) (i/10.0) * screenWidth; // even distribution on x axis
                WATER_DROPS.add(newDrop);
            }
        }
        WAS_SUBMERGED = player.isAlive() && isSubmerged;

        // Spawn droplets randomly when the player is wet
        if (!paused && !isSubmerged && wetness > 0.01f && ((float) Math.random() * 0.05) < 0.0015f * wetness * (frametime * 2)
        && WATER_DROPS.size() < MAX_DROPLETS)
        {
            WATER_DROPS.add(createDrop(screenWidth));
        }

        int waterColor = BiomeColors.getAverageWaterColor(player.level(), playerPos);

        // Setup rendering state
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getParticleShader);

        GuiGraphics graphics = event.getGuiGraphics();
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // Get light level at player position for lighting calculation
        int blockLight = player.level().getLightEngine().getLayerListener(LightLayer.BLOCK).getLightValue(playerPos);
        int skyLight = player.level().getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(playerPos);
        int combinedLight = LightTexture.pack(blockLight, skyLight);

        /*
         Render Water Droplets
         */
        RenderSystem.setShaderTexture(0, WATER_DROP);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        // Handle rendering & movement of water drops
        for (int i = 0; i < WATER_DROPS.size(); i++)
        {
            Droplet drop = WATER_DROPS.get(i);
            Vector2f pos = drop.position;
            float alpha = drop.alpha;
            drop.size = ConfigSettings.WATER_DROPLET_SCALE.get().clamp(drop.size);
            int scaledSize = drop.size / uiScale * 3;

            if (alpha > 0)
            {
                // Render the water drop with lighting
                renderQuadDirect(poseStack, buffer, (int) CSMath.roundNearest(pos.x, 3f/uiScale), (int)pos.y,
                                 scaledSize, scaledSize, 0, 0, 1, 1, alpha, combinedLight, waterColor);

                // Update the drop's position and alpha
                if (!paused)
                {
                    // Fade out
                    if (wetness <= 0)
                    {   drop.alpha -= 0.08f * frametime;
                    }
                    else
                    {   drop.alpha -= 0.003f * frametime * tempMult;
                    }
                    // Recalculate the y velocity every so often
                    if (drop.yMotionUpdateCooldown <= 0)
                    {
                        drop.yMotionUpdateCooldown = (float) Math.random() * 16f + 8f;
                        drop.yMotion = getRandomVelocity(frametime);
                    }
                    else drop.yMotionUpdateCooldown -= frametime;

                    // Randomly change the x motion
                    if (drop.XMotionUpdateCooldown <= 0)
                    {
                        drop.XMotionUpdateCooldown = (float) Math.random() * 8f + 4f;
                        drop.xMotion = (float) Math.random() * 0.02f - 0.01f;
                    }
                    drop.XMotionUpdateCooldown -= frametime;

                    int oldY = (int)pos.y;
                    // Move the drop
                    if (!isSubmerged)
                    {   drop.position.add(new Vector2f(drop.xMotion * drop.yMotion * 20 + drop.xVelocity, drop.yMotion).div(uiScale).mul(3));
                    }

                    // Add a trail behind the drop
                    for (int j = 0; j < Math.max(0, (int) (pos.y - oldY)); j++)
                    {
                        TRAILS.add(new Triplet<>(new Vector2i((int)pos.x, oldY + j), alpha, drop.size));
                    }
                }

                // Wrap drops around the screen
                if (pos.x < -20)
                {   pos.x = screenWidth + 20;
                }
                else if (pos.x > screenWidth + 20)
                {   pos.x = -20;
                }
                // Remove drops that fall off the bottom of the screen
                if (pos.y > screenHeight)
                {   removeDrop(drop);
                    i--;
                }
            }
            // Remove drops that have faded out
            else
            {   removeDrop(drop);
                i--;
            }
        }

        // End droplets batch
        tesselator.end();

        /*
         Render Droplet Trails
         */
        RenderSystem.setShaderTexture(0, WATER_DROP_TRAIL);
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        for (int i = 0; i < TRAILS.size(); i++)
        {
            Triplet<Vector2i, Float, Integer> trail = TRAILS.get(i);
            Vector2i pos = trail.getA();
            float alpha = trail.getB();
            int size = trail.getC();

            if (alpha > 0)
            {
                renderQuadDirect(poseStack, buffer, (int) CSMath.roundNearest(pos.x, 3f/uiScale * 4), pos.y,
                                 size / uiScale * 3, 1, 0, 0, 1, 1, alpha, combinedLight, waterColor);
                if (!paused)
                {
                    if (wetness <= 0)
                    {   alpha -= 0.08f * frametime;
                    }
                    else
                    {   alpha -= 0.045f * frametime * tempMult / 2;
                    }
                    TRAILS.set(i, new Triplet<>(new Vector2i(pos.x, pos.y), alpha, size));
                }
            }
            else
            {   TRAILS.remove(trail);
                i--;
            }
        }

        // End trails batch
        tesselator.end();

        // Cleanup rendering state
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        poseStack.popPose();
    }

    private static float getRandomVelocity(float frametime)
    {
        return (float) Math.min(0.7f * frametime * 20, (Math.pow(Math.random() * 5 + 0.1f, 3) * frametime) / 4f);
    }

    private static Droplet createDrop(int screenWidth)
    {
        IntegerBounds dropSize = ConfigSettings.WATER_DROPLET_SCALE.get();
        int size = dropSize.getRandom();
        // Ensure balance of droplets on each side
        Droplet.Side side = Math.random() < 0.5 ? Droplet.Side.LEFT : Droplet.Side.RIGHT;
        if (getDropletsOnSide(side) > getDropletsOnSide(side.opposite()))
        {   side = side.opposite();
        }
        // Set x position
        int xOffset = (int) (Math.random() * screenWidth / 4);
        int x = side == Droplet.Side.LEFT ? xOffset : screenWidth - xOffset;
        // Increment count of droplets on the side
        if (side == Droplet.Side.LEFT)
            LEFT_DROPLETS++;
        else
            RIGHT_DROPLETS++;
        // Create droplet
        return new Droplet(new Vector2f(x, -size), 1f, size, side);
    }

    private static void removeDrop(Droplet droplet)
    {
        if (droplet.side == Droplet.Side.LEFT)
            LEFT_DROPLETS--;
        else
            RIGHT_DROPLETS--;
        WATER_DROPS.remove(droplet);
    }

    private static int getDropletsOnSide(Droplet.Side side)
    {   return side == Droplet.Side.LEFT ? LEFT_DROPLETS : RIGHT_DROPLETS;
    }

    private static void renderQuadDirect(PoseStack poseStack, BufferBuilder buffer, int x, int y,
                                         int width, int height, float u, float v, float uWidth, float vHeight,
                                         float alpha, int lightLevel, int waterColor)
    {
        alpha *= ConfigSettings.WATER_DROPLET_OPACITY.get();
        float red = (waterColor >> 16 & 255)/255f;
        float green = (waterColor >> 8 & 255)/255f;
        float blue = (waterColor & 255)/255f;
        Matrix4f lastPose = poseStack.last().pose();
        buffer.vertex(lastPose, x, y, 0).uv(u, v).color(red, green, blue, alpha).uv2(lightLevel).endVertex();
        buffer.vertex(lastPose, x, y + height, 0).uv(u, v + vHeight).color(red, green, blue, alpha).uv2(lightLevel).endVertex();
        buffer.vertex(lastPose, x + width, y + height, 0).uv(u + uWidth, v + vHeight).color(red, green, blue, alpha).uv2(lightLevel).endVertex();
        buffer.vertex(lastPose, x + width, y, 0).uv(u + uWidth, v).color(red, green, blue, alpha).uv2(lightLevel).endVertex();
    }

    protected static class Droplet
    {
        public Vector2f position;
        public float alpha;
        public int size;
        public float yMotion = getRandomVelocity(Minecraft.getInstance().getFrameTime() / 5);
        public float xMotion = (float) Math.random() * 0.02f - 0.01f;
        public float xVelocity = 0;
        public float yMotionUpdateCooldown = (float) Math.random() * 16f + 8f;
        public float XMotionUpdateCooldown = 16;
        public Side side;

        public Droplet(Vector2f position, float alpha, int size, Side side)
        {
            this.position = position;
            this.alpha = alpha;
            this.size = size;
            this.side = side;
        }

        public enum Side
        {
            LEFT, RIGHT;

            public Side opposite()
            {   return this == LEFT ? RIGHT : LEFT;
            }
        }
    }
}