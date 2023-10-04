package com.momosoftworks.coldsweat.util.math;

public class Triplet<F, S, T>
{
    F first;
    S second;
    T third;

    public Triplet(F first, S second, T third)
    {   this.first = first;
        this.second = second;
        this.third = third;
    }

    public F getFirst()
    {   return first;
    }

    public S getSecond()
    {   return second;
    }

    public T getThird()
    {   return third;
    }
}
