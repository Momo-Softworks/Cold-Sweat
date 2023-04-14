package dev.momostudios.coldsweat.client.event;

import com.mojang.datafixers.util.Pair;
import dev.momostudios.coldsweat.util.registries.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class HandleSoulLampAnim
{
    public static Map<LivingEntity, Pair<Float, Float>> RIGHT_ARM_ROTATIONS = new HashMap<>();
    public static Map<LivingEntity, Pair<Float, Float>> LEFT_ARM_ROTATIONS = new HashMap<>();
    static boolean LEFT_HANDED = false;

    /*static Method SET_LIGHT = ObfuscationReflectionHelper.findMethod(LayerLightEngine.class, "m_7351_", long.class, int.class);
    static Method GET_LIGHT = ObfuscationReflectionHelper.findMethod(LayerLightEngine.class, "m_6172_", long.class);
    static Field LIGHT_ENGINE = ObfuscationReflectionHelper.findField(LevelLightEngine.class, "f_75802_");
    static
    {
        SET_LIGHT.setAccessible(true);
        GET_LIGHT.setAccessible(true);
        LIGHT_ENGINE.setAccessible(true);
    }

    static final Map<BlockPos, Integer> OLD_LIGHT = new HashMap<>();*/

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event)
    {
        Player player = event.player;
        if (player.level.isClientSide && event.phase == TickEvent.Phase.START)
        {
            if (player.tickCount % 20 == 0)
                LEFT_HANDED = player.getMainArm() == HumanoidArm.LEFT;
            //boolean isOn = false;

            Pair<Float, Float> rightArmRot = RIGHT_ARM_ROTATIONS.getOrDefault(player, Pair.of(0f, 0f));

            if (player.getItemInHand(LEFT_HANDED ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND).getItem() == ModItems.SOULSPRING_LAMP)
            {
                //isOn = PlayerHelper.getItemInHand(player, HumanoidArm.RIGHT).getOrCreateTag().getBoolean("isOn");
                float prevRot = rightArmRot.getFirst();
                if (prevRot < 69.99)
                {
                    RIGHT_ARM_ROTATIONS.put(player, Pair.of(prevRot + (70 - prevRot) / 3f, prevRot));
                }
            }
            else
            {
                float prevRot = rightArmRot.getFirst();
                if (prevRot > 0.01)
                {
                    RIGHT_ARM_ROTATIONS.put(player, Pair.of(prevRot + (0 - prevRot) / 3f, prevRot));
                }
            }

            Pair<Float, Float> leftArmRot = LEFT_ARM_ROTATIONS.getOrDefault(player, Pair.of(0f, 0f));

            if (player.getItemInHand(LEFT_HANDED ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND).getItem() == ModItems.SOULSPRING_LAMP)
            {
                //isOn = isOn || PlayerHelper.getItemInHand(player, HumanoidArm.LEFT).getOrCreateTag().getBoolean("isOn");
                float prevRot = leftArmRot.getFirst();
                if (prevRot < 69.99)
                {
                    LEFT_ARM_ROTATIONS.put(player, Pair.of(prevRot + (70 - prevRot) / 3f, prevRot));
                }
            }
            else
            {
                float prevRot = leftArmRot.getFirst();
                if (prevRot > 0.01)
                {
                    LEFT_ARM_ROTATIONS.put(player, Pair.of(prevRot + (0 - prevRot) / 3f, prevRot));
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