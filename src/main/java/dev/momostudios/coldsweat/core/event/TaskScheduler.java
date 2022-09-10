package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber
public class TaskScheduler
{
    static volatile LinkedHashMap<Runnable, Integer> SERVER_SCHEDULE = new LinkedHashMap<>();
    static volatile LinkedHashMap<Runnable, Integer> CLIENT_SCHEDULE = new LinkedHashMap<>();

    @SubscribeEvent
    public static synchronized void runScheduledTasks(TickEvent event)
    {
        if ((event instanceof TickEvent.ServerTickEvent || event instanceof TickEvent.ClientTickEvent) && event.phase == TickEvent.Phase.START)
        {
            Map<Runnable, Integer> schedule = event.side.isClient() ? CLIENT_SCHEDULE : SERVER_SCHEDULE;
            if (schedule.isEmpty()) return;

            // Track tasks that have expired
            Set<Map.Entry<Runnable, Integer>> toRemove = new HashSet<>();
            // Iterate through all active tasks
            for (Map.Entry<Runnable, Integer> task : schedule.entrySet())
            {
                if (task.getValue() <= 0)
                {
                    try
                    {
                        // Run the task and remove it from the scheduler
                        task.getKey().run();
                        toRemove.add(task);
                    }
                    catch (Exception e)
                    {
                        ColdSweat.LOGGER.error("Error while running scheduled task", e);
                        e.printStackTrace();
                    }
                }
                // Tick timer
                else
                {
                    task.setValue(task.getValue() - 1);
                }
            }
            schedule.entrySet().removeAll(toRemove);
        }
    }

    /**
     * Executes the given Runnable on the serverside after a specified delay
     * @param task The code to execute
     * @param delay The delay in ticks
     */
    public static void scheduleServer(Runnable task, int delay)
    {
        SERVER_SCHEDULE.put(task, delay);
    }

    /**
     * Executes the given Runnable on the serverside after a specified delay
     * @param task The code to execute
     * @param delay The delay in ticks
     */
    public static void scheduleClient(Runnable task, int delay)
    {
        CLIENT_SCHEDULE.put(task, delay);
    }
}
