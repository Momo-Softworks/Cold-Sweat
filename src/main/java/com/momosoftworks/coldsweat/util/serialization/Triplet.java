package com.momosoftworks.coldsweat.util.serialization;

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

    public Triplet<A, B, C> of(A first, B second, C third)
    {   return new Triplet<>(first, second, third);
    }

    public A getA()
    {   return first;
    }

    public B getB()
    {   return second;
    }

    public C getC()
    {   return third;
    }
}
