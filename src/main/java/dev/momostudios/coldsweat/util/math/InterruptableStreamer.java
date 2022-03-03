package dev.momostudios.coldsweat.util.math;

import java.util.Collection;
import java.util.function.BiConsumer;

public class InterruptableStreamer<T>
{
    private boolean stopped = false;
    Collection<T> stream;

    public InterruptableStreamer(Collection<T> stream)
    {
        this.stream = stream;
    }

    public void stop()
    {
        stopped = true;
    }

    public void run(BiConsumer<T, InterruptableStreamer<T>> consumer)
    {
        for (T t : stream)
        {
            if (stopped)
            {
                break;
            }
            consumer.accept(t, this);
        }
    }
}
