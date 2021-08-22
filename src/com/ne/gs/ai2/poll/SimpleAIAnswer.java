/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.poll;

/**
 * @author ATracer
 */
public class SimpleAIAnswer implements AIAnswer {

    private final boolean answer;

    /**
     * @param answer
     */
    SimpleAIAnswer(boolean answer) {
        this.answer = answer;
    }

    @Override
    public boolean isPositive() {
        return answer;
    }

    @Override
    public Object getResult() {
        return answer;
    }

}
