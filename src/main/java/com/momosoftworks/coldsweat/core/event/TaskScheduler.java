package com.momosoftworks.coldsweat.core.event;

import com.momosoftworks.coldsweat.ColdSweat;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber
public class TaskScheduler
{
    static final ConcurrentLinkedQueue<QueueEntry> SERVER_SCHEDULE = new ConcurrentLinkedQueue<>();
    static final ConcurrentLinkedQueue<QueueEntry> CLIENT_SCHEDULE = new ConcurrentLinkedQueue<>();

    @SubscribeEvent
    public static void runScheduledTasks(TickEvent event)
    {
        if ((event instanceof TickEvent.ServerTickEvent || event instanceof TickEvent.ClientTickEvent) && event.phase == TickEvent.Phase.START)
        {
            ConcurrentLinkedQueue<QueueEntry> schedule = event.side.isClient() ? CLIENT_SCHEDULE : SERVER_SCHEDULE;

            // Iterate through all active tasks
            if (!schedule.isEmpty())
            schedule.removeIf(entry ->
            {
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
                    return true;
                }
                // Otherwise, decrement the task's tick count
                else
                {   entry.time = ticks - 1;
                }
                return false;
            });
        }
    }

    /**
     * Executes the given Runnable on the serverside after a specified delay
     * @param task The code to execute
     * @param delay The delay in ticks
     */
    public static void scheduleServer(Runnable task, int delay)
    {   SERVER_SCHEDULE.add(new QueueEntry(task, delay));
    }

    /**
     * Executes the given Runnable on the serverside after a specified delay
     * @param task The code to execute
     * @param delay The delay in ticks
     */
    public static void scheduleClient(Runnable task, int delay)
    {   CLIENT_SCHEDULE.add(new QueueEntry(task, delay));
    }

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

        public Runnable getTask()
        {   return task;
        }

        public int getTime()
        {   return time;
        }

        public void setTime(int time)
        {   this.time = time;
        }
    }
}
