/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2.manager;

import com.ne.gs.ai2.AI2Logger;
import com.ne.gs.ai2.AISubState;
import com.ne.gs.ai2.NpcAI2;
import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.skill.NpcSkillEntry;
import com.ne.gs.model.skill.NpcSkillList;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.skillengine.model.SkillType;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public final class SkillAttackManager {

    /**
     * @param npcAI
     * @param delay
     */
    public static void performAttack(NpcAI2 npcAI, int delay) {
        if (npcAI.setSubStateIfNot(AISubState.CAST)) {
            if (delay > 0) {
                ThreadPoolManager.getInstance().schedule(new SkillAction(npcAI), delay);
            } else {
                skillAction(npcAI);
            }
        }
    }

    /**
     * @param npcAI
     */
    protected static void skillAction(NpcAI2 npcAI) {
        Creature target = (Creature) npcAI.getOwner().getTarget();
        if (target != null && !target.getLifeStats().isAlreadyDead()) {
            int skillId = npcAI.getSkillId();
            int skillLevel = npcAI.getSkillLevel();

            SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(skillId);
            int duration = template.getDuration();
            if (npcAI.isLogging()) {
                AI2Logger.info(npcAI, "Using skill " + skillId + " level: " + skillLevel + " duration: " + duration);
            }
            switch (template.getSubType()) {
                case BUFF:
                    switch (template.getProperties().getFirstTarget()) {
                        case ME:
                            if (npcAI.getOwner().getEffectController().isAbnormalPresentBySkillId(skillId)) {
                                afterUseSkill(npcAI);
                                return;
                            }
                            break;
                        default:
                            if (target.getEffectController().isAbnormalPresentBySkillId(skillId)) {
                                afterUseSkill(npcAI);
                                return;
                            }
                            break;
                    }
                    break;
            }
            boolean success = !npcAI.getOwner().canAttack() ? false : npcAI.getOwner().getController().useSkill(skillId, skillLevel);
            if (!success || duration == 0) {
                afterUseSkill(npcAI);
            } else {
                ThreadPoolManager.getInstance().schedule(new AfterSkillAction(npcAI), duration);
            }
        } else {
            npcAI.setSubStateIfNot(AISubState.NONE);
            npcAI.onGeneralEvent(AIEventType.TARGET_GIVEUP);
        }

    }

    /**
     * @param npcAI
     */
    protected static void afterUseSkill(NpcAI2 npcAI) {
        npcAI.setSubStateIfNot(AISubState.NONE);
        npcAI.onGeneralEvent(AIEventType.ATTACK_COMPLETE);
    }

    /**
     * @param npcAI
     *
     * @return
     */
    public static NpcSkillEntry chooseNextSkill(NpcAI2 npcAI) {
        if (npcAI.isInSubState(AISubState.CAST)) {
            return null;
        }

        Npc owner = npcAI.getOwner();
        NpcSkillList skillList = owner.getSkillList();
        if (skillList == null || skillList.size() == 0) {
            return null;
        }

        if (owner.getGameStats().canUseNextSkill()) {
            NpcSkillEntry npcSkill = skillList.getRandomSkill();
            int currentHpPercent = owner.getLifeStats().getHpPercentage();

            if (npcSkill.isReady(currentHpPercent, System.currentTimeMillis() - owner.getGameStats().getFightStartingTime())) {
                SkillTemplate template = npcSkill.getSkillTemplate();
                if ((template.getType() == SkillType.MAGICAL && owner.getEffectController().isAbnormalSet(AbnormalState.SILENCE))
                    || (template.getType() == SkillType.PHYSICAL && owner.getEffectController().isAbnormalSet(AbnormalState.BIND))
                    || (owner.getEffectController().isUnderFear())) {
                    return null;
                }
                npcSkill.setLastTimeUsed();
                return npcSkill;
            }
        }
        return null;
    }

    private final static class SkillAction implements Runnable {

        private NpcAI2 npcAI;

        SkillAction(NpcAI2 npcAI) {
            this.npcAI = npcAI;
        }

        @Override
        public void run() {
            skillAction(npcAI);
            npcAI = null;
        }
    }

    private final static class AfterSkillAction implements Runnable {

        private NpcAI2 npcAI;

        AfterSkillAction(NpcAI2 npcAI) {
            this.npcAI = npcAI;
        }

        @Override
        public void run() {
            afterUseSkill(npcAI);
            npcAI = null;
        }
    }

}
