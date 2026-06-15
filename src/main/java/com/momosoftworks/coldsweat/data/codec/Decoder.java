package com.momosoftworks.coldsweat.data.codec;

import com.momosoftworks.coldsweat.data.ops.CodecOps;

import java.util.Optional;

@FunctionalInterface
public interface Decoder<T>
{
    <E> Optional<T> decode(CodecOps<E> ops, E obj);
}
