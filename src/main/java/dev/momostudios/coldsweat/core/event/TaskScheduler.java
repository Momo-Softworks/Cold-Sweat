package dev.momostudios.coldsweat.core.event;

import dev.momostudios.coldsweat.ColdSweat;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class TaskScheduler
{
    static final Map<Runnable, Integer> SERVER_SCHEDULE = Collections.synchronizedMap(new HashMap<>());
    static final Map<Runnable, Integer> CLIENT_SCHEDULE = Collections.synchronizedMap(new HashMap<>());

    @SubscribeEvent
    public static void runScheduledTasks(TickEvent event)
    {
        if ((event instanceof TickEvent.ServerTickEvent || event instanceof TickEvent.ClientTickEvent) && event.phase == TickEvent.Phase.START)
        {
            Map<Runnable, Integer> schedule = event.side.isClient() ? CLIENT_SCHEDULE : SERVER_SCHEDULE;

            synchronized (schedule)
            {
                // Iterate through all active tasks
                schedule.entrySet().removeIf(entry ->
                {
                    int ticks = entry.getValue();

                    // If the task is ready to run, run it and remove it from the schedule
                    if (ticks <= 0)
                    {
                        try
                        {
                            entry.getKey().run();
                        }
                        catch (Exception e)
                        {
                            ColdSweat.LOGGER.error("Error while running scheduled task", e);
                            e.printStackTrace();
                        }
                        return true;
                    }
                    // Otherwise, decrement the task's tick count
                    else
                    {
                        entry.setValue(ticks - 1);
                        return false;
                    }
                });
            }
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
