package com.momosoftworks.coldsweat.util.world;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import com.momosoftworks.coldsweat.ColdSweat;

import java.util.LinkedHashMap;
import java.util.Map;

public class TaskScheduler
{
    static volatile LinkedHashMap<Runnable, Integer> SERVER_SCHEDULE = new LinkedHashMap<>();
    static volatile LinkedHashMap<Runnable, Integer> CLIENT_SCHEDULE = new LinkedHashMap<>();

    @SubscribeEvent
    public synchronized void runScheduledTasks(TickEvent event)
    {
        if ((event instanceof TickEvent.ServerTickEvent || event instanceof TickEvent.ClientTickEvent) && event.phase == TickEvent.Phase.START)
        {
            Map<Runnable, Integer> schedule = event.side.isClient() ? CLIENT_SCHEDULE : SERVER_SCHEDULE;
            if (schedule.isEmpty()) return;

            // Iterate through all active tasks
            schedule.entrySet().removeIf(task ->
            {
                int ticksLeft = task.getValue();
                if (ticksLeft <= 0)
                {
                    try
                    {
                        // Run the task and remove it from the scheduler
                        task.getKey().run();
                    }
                    catch (Exception e)
                    {
                        ColdSweat.LOGGER.error("Error while running scheduled task", e);
                        e.printStackTrace();
                    }
                    return true;
                }
                // Tick timer
                else
                {
                    task.setValue(ticksLeft - 1);
                    return false;
                }
            });
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
     * Executes the given Runnable on the clientside after a specified delay
     * @param task The code to execute
     * @param delay The delay in ticks
     */
    public static void scheduleClient(Runnable task, int delay)
    {
        CLIENT_SCHEDULE.put(task, delay);
    }
}
