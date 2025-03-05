package com.momosoftworks.coldsweat.mixin_interface;

public interface IPassthrough
{
    double getPassthroughValue();
    void setPassthroughValue(double value);

    double getRealBaseValue();
    double getRealValue();
}
