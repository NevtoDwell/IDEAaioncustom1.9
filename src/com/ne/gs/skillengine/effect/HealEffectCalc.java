/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */

package com.ne.gs.skillengine.effect;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.stats.container.StatEnum;
import com.ne.gs.skillengine.change.Change;
import com.ne.gs.skillengine.change.Func;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.HealType;

import java.util.List;

/**
 * This class ...
 *
 * @author hex1r0
 */
public final class HealEffectCalc {
    private HealEffectCalc() {}

    public static int calc(HealEffectApi tpl,
                           Effect effect,
                           HealType healType){
        return calc(tpl, effect, healType, true);
    }

    public static int calc(HealEffectApi tpl, Effect effect, HealType healType, boolean limit) {
        Creature effector = effect.getEffector();
        Creature effected = effect.getEffected();

        int valueWithDelta = tpl.getValue() + tpl.getDelta() * effect.getSkillLevel();
        int currentValue = tpl.getCurrentStatValue(effect);
        int maxCurValue = tpl.getMaxStatValue(effect);
        int healValue;
        if (tpl.isPercent()) {
            healValue = maxCurValue * valueWithDelta / 100;
        } else {
            healValue = valueWithDelta;
        }

        if (healType == HealType.HP ) {

            if(effect.getItemTemplate() == null) {
                int boostHealAdd = effector.getGameStats().getStat(StatEnum.HEAL_BOOST, 0).getCurrent();
                // Apply percent Heal Boost bonus (ex. Passive skills)
                int boostHeal = (effector.getGameStats().getStat(StatEnum.HEAL_BOOST, healValue).getCurrent() - boostHealAdd);
                // Apply Add Heal Boost bonus (ex. Skills like Benevolence)
                if (boostHealAdd > 0) {
                    boostHeal += boostHeal * boostHealAdd / 1000;
                }
                healValue = effected.getGameStats().getStat(StatEnum.HEAL_SKILL_BOOST, boostHeal).getCurrent();
            }
            /*else{

                // just a lame fix here
                int toBoost = 0;

                List<Effect> effects = effected.getEffectController().getAbnormalEffects();
                for (Effect ef : effects){

                    switch (ef.getSkillId()){

                        case 2285:
                            toBoost+= 50;
                            break;
                        case 1164:
                            toBoost+= 30;
                            break;

                        case 2259:
                            toBoost-= 50;
                            break;
                        case 1060:
                            toBoost-= 50;
                            break;

                    }
                }

                if(toBoost != 0)
                    healValue += (healValue * (toBoost / 100f));

            }*/
        }


        if (healValue < 0) {
            if (currentValue <= -healValue) {
                healValue = -currentValue;
            }
        } else if(limit){
            healValue = limit(tpl, effect, healValue);
        }

        if (limit && healType == HealType.HP && effected.getEffectController().isAbnormalSet(AbnormalState.DISEASE)) {
            healValue = 0;
        }

        return healValue;
    }

    public static int limit(HealEffectApi tpl, Effect effect, int healValue) {
        if (healValue <= 0) { return healValue; }

        int currentValue = tpl.getCurrentStatValue(effect);
        int maxCurValue = tpl.getMaxStatValue(effect);

        int delta = maxCurValue - currentValue;
        if (delta < healValue) {
            healValue = delta;
        }

        return healValue;
    }

    interface HealEffectApi {

        int getValue();

        int getDelta();

        boolean isPercent();

        int getCurrentStatValue(Effect effect);

        int getMaxStatValue(Effect effect);

        int getPosition();
    }
}
