/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils;

/**
 * @author MrPoke
 */
public final class SafeMath {

    public static int addSafe(int source, int value) throws OverfowException {
        long s = (long) source + (long) value;
        if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
            throw new OverfowException(source + " + " + value + " = " + ((long) source + (long) value));
        }
        return (int) s;
    }

    public static long addSafe(long source, long value) throws OverfowException {
        if ((source > 0 && value > Long.MAX_VALUE - source) || (source < 0 && value < Long.MIN_VALUE - source)) {
            throw new OverfowException(source + " + " + value + " = " + (source + value));
        }
        return source + value;
    }

    public static int multSafe(int source, int value) throws OverfowException {
        long m = ((long) source) * ((long) value);
        if (m < Integer.MIN_VALUE || m > Integer.MAX_VALUE) {
            throw new OverfowException(source + " * " + value + " = " + ((long) source * (long) value));
        }
        return (int) m;
    }

    public static long multSafe(long a, long b) throws OverfowException {

        long ret;
        String msg = "overflow: multiply";
        if (a > b) {
            // use symmetry to reduce boundry cases
            ret = multSafe(b, a);
        } else if (a < 0) {
            if (b < 0) {
                // check for positive overflow with negative a, negative b
                if (a >= Long.MAX_VALUE / b) {
                    ret = a * b;
                } else {
                    throw new OverfowException(msg);
                }
            } else if (b > 0) {
                // check for negative overflow with negative a, positive b
                if (Long.MIN_VALUE / b <= a) {
                    ret = a * b;
                } else {
                    throw new OverfowException(msg);
                }
            } else {
                ret = 0;
            }
        } else if (a > 0) {
            // check for positive overflow with positive a, positive b
            if (a <= Long.MAX_VALUE / b) {
                ret = a * b;
            } else {
                throw new OverfowException(msg);
            }
        } else {
            ret = 0;
        }
        return ret;
    }
}
