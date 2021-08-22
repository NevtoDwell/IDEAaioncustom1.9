/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team.legion;

import com.ne.gs.model.gameobjects.PersistentState;

/**
 * @author Simple modified cura
 */
public class LegionEmblem {

    private int emblemId = 0x00;
    private int color_r = 0x00;
    private int color_g = 0x00;
    private int color_b = 0x00;
    private boolean defaultEmblem = true;
    private LegionEmblemType emblemType = LegionEmblemType.DEFAULT;
    private PersistentState persistentState;

    private boolean isUploading = false;
    private int uploadSize = 0;
    private int uploadedSize = 0;
    private byte[] uploadData;

    private byte[] customEmblemData;

    /**
     * @return the customEmblemData
     */
    public byte[] getCustomEmblemData() {
        return customEmblemData;
    }

    /**
     * @param customEmblemData
     *     the customEmblemData to set
     */
    public void setCustomEmblemData(byte[] customEmblemData) {
        setPersistentState(PersistentState.UPDATE_REQUIRED);
        this.customEmblemData = customEmblemData;
        emblemType = LegionEmblemType.CUSTOM;
    }

    public LegionEmblem() {
        setPersistentState(PersistentState.NEW);
    }

    /**
     * @param emblemId
     *     the emblemId to set
     * @param color_r
     *     the color_r to set
     * @param color_g
     *     the color_g to set
     * @param color_b
     *     the color_b to set
     * @param emblemType
     *     the emblemType to set
     * @param emblem_data
     */
    public void setEmblem(int emblemId, int color_r, int color_g, int color_b, LegionEmblemType emblemType,
                          byte[] emblem_data) {
        this.emblemId = emblemId;
        this.color_r = color_r;
        this.color_g = color_g;
        this.color_b = color_b;
        this.emblemType = emblemType;
        customEmblemData = emblem_data;
        if (this.emblemType.equals(LegionEmblemType.CUSTOM) && customEmblemData == null) {
            this.emblemId = 0;
            this.color_r = 0;
            this.color_g = 0;
            this.color_b = 0;
            this.emblemType = LegionEmblemType.DEFAULT;
        }
        setPersistentState(PersistentState.UPDATE_REQUIRED);
        defaultEmblem = false;
    }

    /**
     * @return the emblemId
     */
    public int getEmblemId() {
        return emblemId;
    }

    /**
     * @return the color_r
     */
    public int getColor_r() {
        return color_r;
    }

    /**
     * @return the color_g
     */
    public int getColor_g() {
        return color_g;
    }

    /**
     * @return the color_b
     */
    public int getColor_b() {
        return color_b;
    }

    /**
     * @return the defaultEmblem
     */
    public boolean isDefaultEmblem() {
        return defaultEmblem;
    }

    /**
     * @param isUploading
     *     the isUploading to set
     */
    public void setUploading(boolean isUploading) {
        this.isUploading = isUploading;
    }

    /**
     * @return the isUploading
     */
    public boolean isUploading() {
        return isUploading;
    }

    /**
     * @param emblemSize
     *     the emblemSize to set
     */
    public void setUploadSize(int emblemSize) {
        uploadSize = emblemSize;
    }

    /**
     * @return the emblemSize
     */
    public int getUploadSize() {
        return uploadSize;
    }

    /**
     */
    public void addUploadData(byte[] data) {
        byte[] newData = new byte[uploadedSize];
        int i = 0;
        if (uploadData != null && uploadData.length > 0) {
            for (byte dataByte : uploadData) {
                newData[i] = dataByte;
                i++;
            }
        }
        for (byte dataByte : data) {
            newData[i] = dataByte;
            i++;
        }
        uploadData = newData;
    }

    /**
     * @return the uploadData
     */
    public byte[] getUploadData() {
        return uploadData;
    }

    /**
     * @param uploadedSize
     *     the uploadedSize to set
     */
    public void addUploadedSize(int uploadedSize) {
        this.uploadedSize += uploadedSize;
    }

    /**
     * @return the uploadedSize
     */
    public int getUploadedSize() {
        return uploadedSize;
    }

    /**
     * @param emblemType
     *     the emblemType to set
     */
    public void setEmblemType(LegionEmblemType emblemType) {
        this.emblemType = emblemType;
    }

    /**
     * @return the emblemType
     */
    public LegionEmblemType getEmblemType() {
        return emblemType;
    }

    /**
     * This method will clear out all upload data
     */
    public void resetUploadSettings() {
        isUploading = false;
        uploadedSize = 0;
        uploadData = null;
    }

    /**
     * @param persistentState
     */
    public void setPersistentState(PersistentState persistentState) {
        switch (persistentState) {
            case UPDATE_REQUIRED:
                if (this.persistentState == PersistentState.NEW) {
                    break;
                }
            default:
                this.persistentState = persistentState;
        }
    }

    /**
     * @return the persistentState
     */
    public PersistentState getPersistentState() {
        return persistentState;
    }

}
