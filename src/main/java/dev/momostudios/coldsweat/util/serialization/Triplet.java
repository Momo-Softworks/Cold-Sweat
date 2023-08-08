package dev.momostudios.coldsweat.util.serialization;

public class Triplet<A, B, C>
{
    public A first;
    public B second;
    public C third;

    public Triplet(A first, B second, C third)
    {   this.first = first;
        this.second = second;
        this.third = third;
    }

    public A getFirst()
    {   return first;
    }

    public B getSecond()
    {   return second;
    }

    public C getThird()
    {   return third;
    }
}
