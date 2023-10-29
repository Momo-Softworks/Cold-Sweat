package com.momosoftworks.coldsweat.common.event;

import com.momosoftworks.coldsweat.api.temperature.modifier.WaterskinTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import com.momosoftworks.coldsweat.util.math.CSMath;
import com.momosoftworks.coldsweat.util.math.Direction;
import com.momosoftworks.coldsweat.util.registries.ModItems;
import com.momosoftworks.coldsweat.util.world.BlockPos;
import com.momosoftworks.coldsweat.util.world.ItemHelper;
import com.momosoftworks.coldsweat.util.world.TaskScheduler;
import com.momosoftworks.coldsweat.util.world.WorldHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RegisterDispenserBehaviors
{
    public static void register()
    {
        BlockDispenser.dispenseBehaviorRegistry.putObject(ModItems.FILLED_WATERSKIN, new IBehaviorDispenseItem()
        {
            @Override
            public ItemStack dispense(IBlockSource source, ItemStack stack)
            {
                EnumFacing facing = BlockDispenser.func_149937_b(source.getBlockMetadata());
                BlockPos pos = new BlockPos(source.getX() + facing.getFrontOffsetX(), source.getY() + facing.getFrontOffsetY(), source.getZ() + facing.getFrontOffsetZ());
                World world = source.getWorld();
                Chunk chunk = WorldHelper.getChunk(world, pos);
                double itemTemp = ItemHelper.getOrCrateTag(stack).getDouble("temperature");

                if (chunk == null) return stack;

                // Play sound
                world.playSoundEffect(pos.getX(), pos.getY(), pos.getZ(), "random.drink", 1, (float) ((Math.random() / 5) + 0.9));

                // Spawn particles
                Random rand = new Random();
                for (int i = 0; i < 6; i++)
                {
                    TaskScheduler.scheduleClient(() ->
                    {
                        for (int p = 0; p < rand.nextInt(5) + 5; p++)
                        {
                            world.spawnParticle("splash", pos.getX() + rand.nextDouble(),
                                                          pos.getY() + rand.nextDouble(),
                                                          pos.getZ() + rand.nextDouble(), 0, 0, 0);
                        }
                    }, i);
                }

                // Spawn a hitbox that falls at the same rate as the particles and gives players below the waterskin effect
                new Object()
                {
                    double acceleration = 0;
                    int tick = 0;
                    AxisAlignedBB aabb = WorldHelper.getBox(pos).expand(0.5, 0.5, 0.5);
                    // Track affected players to prevent duplicate effects
                    final List<Entity> affectedPlayers = new ArrayList<>();

                    void start()
                    {   MinecraftForge.EVENT_BUS.register(this);
                    }

                    @SubscribeEvent
                    public void onTick(TickEvent.WorldTickEvent event)
                    {
                        if (event.world.isRemote == world.isRemote && event.phase == TickEvent.Phase.START)
                        {
                            // Temperature of waterskin weakens over time
                            double waterTemp = CSMath.blend(itemTemp, itemTemp / 5, tick, 20, 100);

                            // Move the box down at the speed of gravity
                            AxisAlignedBB movedBox;
                            aabb = movedBox = aabb.addCoord(0, -acceleration, 0);

                            // If there's ground, stop
                            BlockPos pos = new BlockPos(movedBox.minX, movedBox.minY, movedBox.minZ);
                            if (WorldHelper.isSpreadBlocked(WorldHelper.getBlockState(chunk, pos), Direction.DOWN, Direction.DOWN))
                            {   MinecraftForge.EVENT_BUS.unregister(this);
                                return;
                            }

                            // Apply the waterskin modifier to all entities in the box
                            world.getEntitiesWithinAABB(EntityPlayer.class, movedBox).forEach(player ->
                            {
                                if (!EntityTempManager.getEntitiesWithTemperature().contains(player.getClass())) return;
                                if (!affectedPlayers.contains(((Entity) player)))
                                {   // Apply the effect and store the player
                                    Temperature.addModifier(((Entity) player), new WaterskinTempModifier(waterTemp).expires(0), Temperature.Type.CORE, true);
                                    affectedPlayers.add(((Entity) player));
                                }
                            });

                            // Increase the speed of the box
                            acceleration += 0.0052;
                            tick++;

                            // Expire after 5 seconds
                            if (tick > 100)
                            {   MinecraftForge.EVENT_BUS.unregister(this);
                            }
                        }
                    }
                }.start();

                return FilledWaterskinItem.getEmpty(stack);
            }
        });
    }
}
