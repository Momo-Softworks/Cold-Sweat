package dev.momostudios.coldsweat.client.event;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.entity.PlayerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HandleSoulLampAnim
{
    public static Map<LivingEntity, Pair<Float, Float>> RIGHT_ARM_ROTATIONS = new HashMap<>();
    public static Map<LivingEntity, Pair<Float, Float>> LEFT_ARM_ROTATIONS = new HashMap<>();

    static Method SET_LIGHT = ObfuscationReflectionHelper.findMethod(LayerLightEngine.class, "m_7351_", long.class, int.class);
    static Method GET_LIGHT = ObfuscationReflectionHelper.findMethod(LayerLightEngine.class, "m_6172_", long.class);
    static Field LIGHT_ENGINE = ObfuscationReflectionHelper.findField(LevelLightEngine.class, "f_75802_");
    static
    {
        SET_LIGHT.setAccessible(true);
        GET_LIGHT.setAccessible(true);
        LIGHT_ENGINE.setAccessible(true);
    }

    static final Map<BlockPos, Integer> OLD_LIGHT = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            Player player = event.player;
            //boolean isOn = false;

            Pair<Float, Float> rightArmRot = RIGHT_ARM_ROTATIONS.getOrDefault(player, Pair.of(0f, 0f));

            if (PlayerHelper.holdingLamp(player, HumanoidArm.RIGHT))
            {
                //isOn = PlayerHelper.getItemInHand(player, HumanoidArm.RIGHT).getOrCreateTag().getBoolean("isOn");
                float post = rightArmRot.getFirst();
                if (post < 69.99)
                {
                    RIGHT_ARM_ROTATIONS.put(player, Pair.of(post + (70 - post) / 3f, post));
                }
            }
            else
            {
                float post = rightArmRot.getFirst();
                if (post > 0.01)
                {
                    RIGHT_ARM_ROTATIONS.put(player, Pair.of(post + (0 - post) / 3f, post));
                }
            }

            Pair<Float, Float> leftArmRot = LEFT_ARM_ROTATIONS.getOrDefault(player, Pair.of(0f, 0f));

            if (PlayerHelper.holdingLamp(player, HumanoidArm.LEFT))
            {
                //isOn = isOn || PlayerHelper.getItemInHand(player, HumanoidArm.LEFT).getOrCreateTag().getBoolean("isOn");
                float post = leftArmRot.getFirst();
                if (post < 69.99)
                {
                    LEFT_ARM_ROTATIONS.put(player, Pair.of(post + (71 - post) / 3f, post));
                }
            }
            else
            {
                float post = leftArmRot.getFirst();
                if (post > 0.01)
                {
                    LEFT_ARM_ROTATIONS.put(player, Pair.of(post + (0 - post) / 3f, post));
                }
            }

            // Cast light
            // Too buggy to use for now :(
            /*ClientLevel level = Minecraft.getInstance().level;
            if (level != null)
            {
                synchronized (OLD_LIGHT)
                {
                    LevelLightEngine lightEngine = level.getLightEngine();
                    HashMap<BlockPos, Integer> lightLevels = new HashMap<>();

                    if (!OLD_LIGHT.isEmpty())
                    {
                        // Put the old light back
                        lightLevels.putAll(OLD_LIGHT);
                        OLD_LIGHT.clear();
                    }
                    if (isOn && !player.isSpectator())
                    {
                        BlockPos playerPos = event.player.blockPosition();
                        Vec3 playerPosVec = event.player.position().add(0, event.player.getBbHeight() / 2, 0);

                        // Place the new light
                        for (int x = -4; x < 4; x++)
                        {
                            for (int y = -4; y < 4; y++)
                            {
                                for (int z = -4; z < 4; z++)
                                {
                                    BlockPos pos = playerPos.offset(x, y, z);
                                    int oldLight = lightLevels.getOrDefault(pos, getLight(pos, lightEngine));
                                    int newLight = (int) CSMath.blend(20, 0, CSMath.getDistance(playerPosVec, Vec3.atCenterOf(pos)), 0, 4);
                                    if (oldLight != newLight)
                                        OLD_LIGHT.put(pos, oldLight);
                                    lightLevels.put(pos, Math.max(oldLight, newLight));
                                }
                            }
                        }
                    }
                    for (Map.Entry<BlockPos, Integer> entry : lightLevels.entrySet())
                    {
                        setLight(entry.getKey(), lightEngine, entry.getValue());
                    }
                }
            }*/
        }
    }

    /*static void setLight(BlockPos pos, LevelLightEngine lightEngine, int light)
    {
        try
        {
            SET_LIGHT.invoke(LIGHT_ENGINE.get(lightEngine), pos.asLong(), 15 - light);
        } catch (Exception ignored) {}
    }

    static int getLight(BlockPos pos, LevelLightEngine lightEngine)
    {
        try
        {
            return 15 - (int) GET_LIGHT.invoke(LIGHT_ENGINE.get(lightEngine), pos.asLong());
        } catch (Exception ignored) {}
        return 0;
    }*/
}