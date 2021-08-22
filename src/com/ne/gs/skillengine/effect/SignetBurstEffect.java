//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.ne.gs.skillengine.effect;

import com.ne.commons.utils.Rnd;
import com.ne.gs.controllers.attack.AttackUtil;
import com.ne.gs.skillengine.action.DamageType;
import com.ne.gs.skillengine.effect.modifier.ActionModifier;
import com.ne.gs.skillengine.model.Effect;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "SignetBurstEffect"
)
public class SignetBurstEffect extends DamageEffect {
    @XmlAttribute
    protected int signetlvl;
    @XmlAttribute
    protected String signet;
    private int level2 = 35;
    private int level3 = 95;

    public SignetBurstEffect() {
    }

    private void tryRemoveSignedEffect(Effect effect) {
        Effect signetEffect = effect.getEffected().getEffectController().getAnormalEffect(this.signet);
        if (signetEffect != null && (effect.getSubEffect() == null || !effect.getSuccessSubEffects().isEmpty())) {
            signetEffect.endEffect();
        }

    }

    public void applyEffect(Effect effect) {
        super.applyEffect(effect);
        this.tryRemoveSignedEffect(effect);
    }

    public void calculate(Effect effect) {
        if (effect.getSkillId() == 946) {
            if (!super.calculate(effect, DamageType.MAGICAL, true)) {
                return;
            }
        } else if (!super.calculate(effect, DamageType.MAGICAL)) {
            this.tryRemoveSignedEffect(effect);
            return;
        }

        Effect signetEffect = effect.getEffected().getEffectController().getAnormalEffect(this.signet);
        int valueWithDelta = this.value + this.delta * effect.getSkillLevel();
        int critAddDmg = this.critAddDmg2 + this.critAddDmg1 * effect.getSkillLevel();
        if (signetEffect == null) {
            valueWithDelta = (int)((float)valueWithDelta * 0.05F);
            AttackUtil.calculateMagicalSkillResult(effect, valueWithDelta, (ActionModifier)null, this.getElement(), true, true, false, this.getMode(), this.critProbMod2, critAddDmg);
            effect.setLaunchSubEffect(false);
        } else {
            int level = signetEffect.getSkillLevel();
            if (level < 3) {
                effect.setSubEffectAborted(true);
            }

            effect.setSignetBurstedCount(level);
            switch(level) {
            case 1:
                valueWithDelta = (int)((float)valueWithDelta * 0.2F);
                break;
            case 2:
                valueWithDelta = (int)((float)valueWithDelta * 0.5F);
                break;
            case 3:
                valueWithDelta = (int)((float)valueWithDelta * 1.0F);
                break;
            case 4:
                valueWithDelta = (int)((float)valueWithDelta * 1.2F);
                break;
            case 5:
                valueWithDelta = (int)((float)valueWithDelta * 1.5F);
            }

            int accmod = 0;
            int mAccurancy = effect.getEffector().getGameStats().getMAccuracy().getCurrent();
            switch(level) {
            case 1:
                accmod = (int)(-10.8F * (float)mAccurancy);
                break;
            case 2:
                accmod = (int)(-10.5F * (float)mAccurancy);
                break;
            case 3:
                accmod = 0;
                break;
            case 4:
                accmod = (int)(0.2F * (float)mAccurancy);
                break;
            case 5:
                accmod = (int)(0.5F * (float)mAccurancy);
            }

            if (level < 2) {
                effect.setLaunchSubEffect(false);
            }

            if (level == 2 && Rnd.get(0, 100) > this.level2) {
                effect.setLaunchSubEffect(false);
            }

            if (level >= 3 && Rnd.get(0, 100) > this.level3) {
                effect.setLaunchSubEffect(false);
            }

            effect.setAccModBoost(accmod);
            AttackUtil.calculateMagicalSkillResult(effect, valueWithDelta, (ActionModifier)null, this.getElement(), true, true, false, this.getMode(), this.critProbMod2, critAddDmg);
            if (signetEffect != null) {
                signetEffect.endEffect();
            }
        }

    }
}
