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

    AccessLevel0(0, "%s", "", new int[]{0}, "Welcome  %s"),
    AccessLevel1(1, AdminConfig.ADMIN_TAG_1, "\ue042SUPPORT\ue043", new int[]{174, 175}, "Welcome SUPPORT %s"),
    AccessLevel2(2, AdminConfig.ADMIN_TAG_2, "\ue042EVENT GM\ue043", new int[]{174, 175, 1904, 1911}, "Welcome EVENT GM %s"),
    AccessLevel3(3, AdminConfig.ADMIN_TAG_3, "\ue042GAME MASTER\ue043", new int[]{174, 175, 1904, 1911}, "Welcome GAME MASTER %s"),
    AccessLevel4(4, AdminConfig.ADMIN_TAG_4, "\ue042ADMINISTRATOR\ue043", new int[]{174, 175, 1904, 1911}, "Welcome ADMINISTRATOR %s"),
    AccessLevel5(5, AdminConfig.ADMIN_TAG_5, "\ue050DEVELOPER\ue050", new int[]{174, 175, 1904, 1911}, "Welcome DEVELOPER %s");

    private int level; //
    private final String nameLevel;
    private String status;
    private int[] skills;
    private String notice;

    AccessLevelEnum(int id, String name, String status, int[] skills, String notice) {
        this.level = id;
        this.nameLevel = name;
        this.status = status;
        this.skills = skills;
        this.notice = notice;
    }

    public String getNotice(String name) {
        return String.format(notice, name);
    }

    public String getName() {
        return nameLevel;
    }

    public int getLevel() {
        return level;
    }

    public String getStatusName() {
        return status;
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
