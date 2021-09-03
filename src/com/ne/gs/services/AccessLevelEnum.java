/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.services;

import com.ne.gs.configs.administration.AdminConfig;

/**
 *
 * @author userd
 */
public enum AccessLevelEnum {

    AccessLevel0(0, "WHITE PORTAL", "", new int[]{0}, "Welcome  %s"),
    AccessLevel1(1, "PORTAL", "\ue042SUPPORT\ue043", new int[]{174, 175}, "Welcome SUPPORT %s"),
    AccessLevel2(2, "PORTAL", "\ue042EVENT GM\ue043", new int[]{174, 175, 1904, 1911}, "Welcome EVENT GM %s"),
    AccessLevel3(3, "PORTAL", "\ue042GAME MASTER\ue043", new int[]{174, 175, 1904, 1911}, "Welcome GAME MASTER %s"),
    AccessLevel4(4, "PORTAL", "\ue042ADMINISTRATOR\ue043", new int[]{174, 175, 1904, 1911}, "Welcome ADMINISTRATOR %s"),
    AccessLevel5(5, "DREDGION", "\ue050DEVELOPER\ue050", new int[]{174, 175, 1904, 1911}, "Welcome DEVELOPER %s");

    private int level;
    private final String gmTag;
    private String newLegion;
    private int[] skills;
    private String notice;

    AccessLevelEnum(int level, String gmTag, String newLegion, int[] skills, String notice) {
        this.level = level;
        this.gmTag = gmTag;
        this.newLegion = newLegion;
        this.skills = skills;
        this.notice = notice;
    }

    public String getNotice(String name) {
        return String.format(notice, name);
    }

    public String getTagForName() {
        switch(gmTag){
            case "WHITE PORTAL":
                return "\uE065";
            case "PORTAL":
                return "\uE04C";
            case "DREDGION":
                return "\uE050";
            default:
                return "";
        }
    }

    public int getLevel() {
        return level;
    }

    public String getLegionName() {
        return newLegion;
    }

    public int[] getSkills() {
        return skills;
    }

    public int[] getSkills(int level) {
        for (AccessLevelEnum GmSkills : values()) {
            if(GmSkills.level == level){
                return GmSkills.skills;
            }
        }
        return new int[0];
    }



    public static AccessLevelEnum getAlType(int level) {
        switch (level) {
            case 1:
                return AccessLevelEnum.AccessLevel1;
            case 2:
                return AccessLevelEnum.AccessLevel2;
            case 3:
                return AccessLevelEnum.AccessLevel3;
            case 4:
                return AccessLevelEnum.AccessLevel4;
            case 5:
                return AccessLevelEnum.AccessLevel5;
            default:
                return AccessLevelEnum.AccessLevel0;
        }
    }
}
