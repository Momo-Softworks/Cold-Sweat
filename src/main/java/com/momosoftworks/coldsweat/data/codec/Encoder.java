package com.momosoftworks.coldsweat.data.codec;

import com.momosoftworks.coldsweat.data.ops.CodecOps;

import java.util.Optional;

@FunctionalInterface
public interface Encoder<T>
{
    <E> Optional<E> encode(CodecOps<E> ops, T obj);
}
