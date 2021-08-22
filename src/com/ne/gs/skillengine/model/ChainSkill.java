/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.model;

public class ChainSkill {

    private String category;
    private int chainCount = 0;
    private long useTime;

    public ChainSkill(String category, int chainCount, long useTime) {
        this.category = category;
        this.chainCount = chainCount;
        this.useTime = useTime;
    }

    public void updateChainSkill(String category) {
        this.category = category;
        chainCount = 0;
        useTime = System.currentTimeMillis();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String name) {
        category = name;
    }

    public int getChainCount() {
        return chainCount;
    }

    public void setChainCount(int chainCount) {
        this.chainCount = chainCount;
    }

    public void increaseChainCount() {
        chainCount++;
    }

    public long getUseTime() {
        return useTime;
    }

    public void setUseTime(long useTime) {
        this.useTime = useTime;
    }
}
