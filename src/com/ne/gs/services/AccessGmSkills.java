package com.ne.gs.services;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

public class AccessGmSkills {

    public static void updateGmSkills(Player player) {

        if (player.getAccessLevel() < 1) {
            for (AccessLevelEnum skill : AccessLevelEnum.values()) {
                for (int skillId : skill.getSkills()) {
                    ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                            player.getSkillList().removeSkill(skillId);
                        }
                    },1000);
                }
            }
        }

        if(player.getAccessLevel() > 0) {
            for (AccessLevelEnum skill : AccessLevelEnum.values()) {
                for (int skillId : skill.getSkills(player.getAccessLevel())) {
                    ThreadPoolManager.getInstance().schedule(new Runnable() {
                        @Override
                        public void run() {
                             player.getSkillList().addTemporarySkill(player, skillId, 1);
                        }
                    },1000);
                }
            }
        }
    }

    public static void onEnterWorldGm(Player player) {
        updateGmSkills(player);
    }
}
