package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that allows for scheduling {@link Runnable} tasks to be executed after a delay.<br>
 * Tasks will not be preserved if the server or client restarts, so this should be used for short-term tasks only.<br>
 */
@Mod.EventBusSubscriber
public class TaskScheduler
{
    private static final List<QueueEntry> SERVER_SCHEDULE = new ArrayList<>(32);
    private static final List<QueueEntry> CLIENT_SCHEDULE = new ArrayList<>(32);

    @SubscribeEvent
    public static synchronized void runScheduledTasks(TickEvent event)
    {
        if ((event instanceof TickEvent.ServerTickEvent || event instanceof TickEvent.ClientTickEvent) && event.phase == TickEvent.Phase.START)
        {
            List<QueueEntry> schedule = event.side.isClient() ? CLIENT_SCHEDULE : SERVER_SCHEDULE;

            // Iterate through all active tasks
            if (!schedule.isEmpty())
            {
                for (int i = 0; i < schedule.size(); i++)
                {
                    QueueEntry entry = schedule.get(i);
                    int ticks = entry.time;

                    // If the task is ready to run, run it and remove it from the schedule
                    if (ticks <= 0)
                    {
                        try
                        {   entry.task.run();
                        }
                        catch (Exception e)
                        {   ColdSweat.LOGGER.error("Error while running scheduled task", e);
                            throw e;
                        }
                        schedule.remove(i);
                        i--;
                    }
                    // Otherwise, decrement the task's tick count
                    else
                    {   entry.time = ticks - 1;
                    }
                }
            }
        }
    }

    /**
     * Executes the given Runnable on the logical server after a specified delay.<br>
     * If this is called on the client, the task will not execute.
     * @param task The code to execute
     * @param delay The delay in ticks
     */
    public static void scheduleServer(Runnable task, int delay)
    {   SERVER_SCHEDULE.add(new QueueEntry(task, delay));
    }

    /**
     * Executes the given Runnable on the logical client after a specified delay.<br>
     * If this is called on the server, the task will not execute.
     * @param task The code to execute
     * @param delay The delay in ticks
     */
    public static void scheduleClient(Runnable task, int delay)
    {   CLIENT_SCHEDULE.add(new QueueEntry(task, delay));
    }

    /**
     * Executes the given Runnable on both logical sides after a specified delay.<br>
     */
    public static void schedule(Runnable task, int delay)
    {   scheduleServer(task, delay);
        scheduleClient(task, delay);
    }

    static class QueueEntry
    {
        private final Runnable task;
        private int time;

        public QueueEntry(Runnable task, int time)
        {   this.task = task;
            this.time = time;
        }
    }
}
