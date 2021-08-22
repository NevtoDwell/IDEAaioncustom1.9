/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model;

public enum TeleportAnimation {
    NO_ANIMATION(0, 0),
    BEAM_ANIMATION(1, 3),
    JUMP_AIMATION(3, 10),
    JUMP_AIMATION_2(4, 10),
    JUMP_AIMATION_3(8, 3);

    private final int startAnimation;
    private final int endAnimation;

    private TeleportAnimation(int startAnimation, int endAnimation) {
        this.startAnimation = startAnimation;
        this.endAnimation = endAnimation;
    }

    public int getStartAnimationId() {
        return startAnimation;
    }

    public int getEndAnimationId() {
        return endAnimation;
    }

    public boolean isNoAnimation() {
        return getStartAnimationId() == 0;
    }
}
