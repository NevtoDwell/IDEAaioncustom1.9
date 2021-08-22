/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.model.team2;

/**
 *
 * @author userd
 */
public enum TeamType {

    GROUP(0x3F, 0),
    AUTO_GROUP(0x02, 1),
    ALLIANCE(0x3F, 0),
    ALLIANCE_DEFENCE(0x3F, 4),
    ALLIANCE_OFFENCE(0x02, 3);
    private int type;
    private int subType;

    private TeamType(int type, int subType) {
        this.type = type;
        this.subType = subType;
    }

    public int getType() {
        return type;
    }

    public int getSubType() {
        return subType;
    }

    public boolean isAutoTeam() {
        return this.getType() == 0x02;
    }

    public boolean isOffence() {
        return this.getSubType() == 3;
    }

    public boolean isDefence() {
        return this.getSubType() == 4;
    }

}
