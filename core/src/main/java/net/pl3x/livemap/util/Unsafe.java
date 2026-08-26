/*
 * This file is part of LiveMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020-2026 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pl3x.livemap.util;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NonNls;

/**
 * Unsafe utils.
 */
public final class Unsafe {
    private Unsafe() {
    }

    /**
     * Cast object to T.
     *
     * @param obj Object to cast
     * @param <T> Type to cast to
     * @return Object as T
     */
    @UnknownNullability("Unsafe cast. Could be null.")
    @SuppressWarnings("unchecked")
    public static <T> T cast(@UnknownNullability Object obj) {
        return (T) obj;
    }

    /**
     * An element annotated with {@code UnknownNullability} claims that no specific nullability
     * should be assumed by static analyzer. The unconditional dereference of the annotated value
     * should not trigger a static analysis warning by default (though static analysis tool may have
     * an option to perform stricter analysis and issue warnings for {@code @UnknownNullability} as well).
     * It's mainly useful at method return types to mark methods that may occasionally
     * return {@code null} but in many cases, user knows that in this particular code path
     * {@code null} is not possible, so producing a warning would be annoying.
     */
    @Documented
    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
    public @interface UnknownNullability {
        /**
         * Human-readable description of the circumstances, in which the type is nullable.
         *
         * @return textual reason when the annotated value could be null, for documentation purposes
         */
        @SuppressWarnings("UnusedReturnValue")
        @NonNls String value() default "";
    }
}
