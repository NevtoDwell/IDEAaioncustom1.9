/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.team.legion;

/**
 * @author Simple
 */
public class LegionMember {

    private int objectId = 0;
    protected Legion legion = null;
    protected String nickname = "";
    protected String selfIntro = "";

    protected LegionRank rank = LegionRank.VOLUNTEER;

    /**
     * If player is defined later on this constructor is called
     */
    public LegionMember(int objectId) {
        this.objectId = objectId;
    }

    /**
     * This constructor is called when a legion is created
     */
    public LegionMember(int objectId, Legion legion, LegionRank rank) {
        setObjectId(objectId);
        setLegion(legion);
        setRank(rank);
    }

    /**
     * This constructor is called when a LegionMemberEx is called
     */
    public LegionMember() {
    }

    /**
     * @param legion
     *     the legion to set
     */
    public void setLegion(Legion legion) {
        this.legion = legion;
    }

    /**
     * @return the legion
     */
    public Legion getLegion() {
        return legion;
    }

    /**
     * @param rank
     *     the rank to set
     */
    public void setRank(LegionRank rank) {
        this.rank = rank;
    }

    /**
     * @return the rank
     */
    public LegionRank getRank() {
        return rank;
    }

    public boolean isBrigadeGeneral() {
        return rank == LegionRank.BRIGADE_GENERAL;
    }

    /**
     * @param nickname
     *     the nickname to set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param selfIntro
     *     the selfIntro to set
     */
    public void setSelfIntro(String selfIntro) {
        this.selfIntro = selfIntro;
    }

    /**
     * @return the selfIntro
     */
    public String getSelfIntro() {
        return selfIntro;
    }

    /**
     * @param objectId
     *     the objectId to set
     */
    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    /**
     * @return the objectId
     */
    public int getObjectId() {
        return objectId;
    }

    public boolean hasRights(LegionPermissionsMask permissions) {
        int legionarPermission = 0;
        switch (getRank()) {
            case BRIGADE_GENERAL:
                return true;
            case DEPUTY:
                legionarPermission = legion.getDeputyPermission();
                break;
            case CENTURION:
                legionarPermission = legion.getCenturionPermission();
                break;
            case LEGIONARY:
                legionarPermission = legion.getLegionaryPermission();
                break;
            case VOLUNTEER:
                legionarPermission = legion.getVolunteerPermission();
                break;
        }
        return permissions.can(legionarPermission);
    }
}
