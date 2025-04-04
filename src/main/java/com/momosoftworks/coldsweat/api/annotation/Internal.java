package com.momosoftworks.coldsweat.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks methods, classes, and fields that are internal to the mod and should not be used by developers.
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Internal
{}
