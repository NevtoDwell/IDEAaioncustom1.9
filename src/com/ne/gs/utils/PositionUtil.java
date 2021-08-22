/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils;

import com.ne.gs.model.gameobjects.VisibleObject;

/**
 * @author ATracer
 */
public final class PositionUtil {

    private static final float MAX_ANGLE_DIFF = 90f;

    /**
     * @param object1
     * @param object2
     *
     * @return true or false
     */
    public static boolean isBehindTarget(VisibleObject object1, VisibleObject object2) {
        float angleObject1 = MathUtil.calculateAngleFrom(object1, object2);
        float angleObject2 = MathUtil.convertHeadingToDegree(object2.getHeading());
        float angleDiff = angleObject1 - angleObject2;

        if (angleDiff <= -360 + MAX_ANGLE_DIFF) {
            angleDiff += 360;
        }
        if (angleDiff >= 360 - MAX_ANGLE_DIFF) {
            angleDiff -= 360;
        }
        return Math.abs(angleDiff) <= MAX_ANGLE_DIFF;
    }

    /**
     * @param object1
     * @param object2
     *
     * @return true or false
     */
    public static boolean isInFrontOfTarget(VisibleObject object1, VisibleObject object2) {
        float angleObject2 = MathUtil.calculateAngleFrom(object2, object1);
        float angleObject1 = MathUtil.convertHeadingToDegree(object2.getHeading());
        float angleDiff = angleObject1 - angleObject2;

        if (angleDiff <= -360 + MAX_ANGLE_DIFF) {
            angleDiff += 360;
        }
        if (angleDiff >= 360 - MAX_ANGLE_DIFF) {
            angleDiff -= 360;
        }
        return Math.abs(angleDiff) <= MAX_ANGLE_DIFF;
    }

    /**
     * Analyse two object position by coordinates
     *
     * @param object1
     * @param object2
     *
     * @return true if the analysed object is behind base object
     */
    public static boolean isBehind(VisibleObject object1, VisibleObject object2) {
        float angle = MathUtil.convertHeadingToDegree(object1.getHeading()) + 90;
        if (angle >= 360) {
            angle -= 360;
        }
        double radian = Math.toRadians(angle);
        float x0 = object1.getX();
        float y0 = object1.getY();
        float x1 = (float) (Math.cos(radian) * 5) + x0;
        float y1 = (float) (Math.sin(radian) * 5) + y0;
        float xA = object2.getX();
        float yA = object2.getY();
        float temp = (x1 - x0) * (yA - y0) - (y1 - y0) * (xA - x0);
        return temp > 0;
    }

    public static float getAngleToTarget(VisibleObject object1, VisibleObject object2) {
        float angleObject1 = MathUtil.convertHeadingToDegree(object1.getHeading()) - 180.0F;
        if (angleObject1 < 0.0F) {
            angleObject1 += 360.0F;
        }
        float angleObject2 = MathUtil.calculateAngleFrom(object1, object2);
        float angleDiff = angleObject1 - angleObject2 - 180.0F;
        if (angleDiff < 0.0F) {
            angleDiff += 360.0F;
        }
        return angleDiff;
    }

    public static byte getMoveAwayHeading(VisibleObject fromObject, VisibleObject object) {
        float angle = MathUtil.calculateAngleFrom(fromObject, object);
        byte heading = MathUtil.convertDegreeToHeading(angle);
        return heading;
    }
}
