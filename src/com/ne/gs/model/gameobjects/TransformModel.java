/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import com.ne.gs.model.TribeClass;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.skillengine.model.TransformType;

public class TransformModel {

    private int modelId;
    private final int originalModelId;
    private final TransformType originalType;
    private TransformType transformType;
    private int panelId;
    private boolean isActive = false;
    private TribeClass transformTribe;
    private TribeClass overrideTribe;

    public TransformModel(Creature creature) {
        if (creature instanceof Player) {
            originalType = TransformType.PC;
        } else {
            originalType = TransformType.NONE;
        }
        originalModelId = creature.getObjectTemplate().getTemplateId();
        transformType = TransformType.NONE;
    }

    public int getModelId() {
        if (isActive && modelId > 0) {
            return modelId;
        }
        return originalModelId;
    }

    public void setModelId(int modelId) {
        if (modelId == 0 || modelId == originalModelId) {
            modelId = originalModelId;
            isActive = false;
        } else {
            this.modelId = modelId;
            isActive = true;
        }
    }

    public TransformType getType() {
        if (isActive) {
            return transformType;
        }
        return originalType;
    }

    public void setTransformType(TransformType transformType) {
        this.transformType = transformType;
    }

    public int getPanelId() {
        if (isActive) {
            return panelId;
        }
        return 0;
    }

    public void setPanelId(int id) {
        panelId = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public TribeClass getTribe() {
        if (isActive && transformTribe != null) {
            return transformTribe;
        }
        return overrideTribe;
    }

    public void setTribe(TribeClass transformTribe, boolean override) {
        if (override) {
            overrideTribe = transformTribe;
        } else {
            this.transformTribe = transformTribe;
        }
    }
}
