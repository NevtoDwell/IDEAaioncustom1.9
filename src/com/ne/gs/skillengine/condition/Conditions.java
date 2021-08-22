/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.stats.calc.Stat2;
import com.ne.gs.model.stats.calc.functions.IStatFunction;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Conditions", propOrder = {"conditions"})
public class Conditions {

    @XmlElements({
        @XmlElement(name = "abnormal", type = AbnormalStateCondition.class),
        @XmlElement(name = "target", type = TargetCondition.class),
        @XmlElement(name = "mp", type = MpCondition.class),
        @XmlElement(name = "hp", type = HpCondition.class),
        @XmlElement(name = "dp", type = DpCondition.class),
        @XmlElement(name = "playermove", type = PlayerMovedCondition.class),
        @XmlElement(name = "arrowcheck", type = ArrowCheckCondition.class),
        @XmlElement(name = "onfly", type = OnFlyCondition.class),
        @XmlElement(name = "weapon", type = WeaponCondition.class),
        @XmlElement(name = "noflying", type = NoFlyingCondition.class),
        @XmlElement(name = "shield", type = ShieldCondition.class),
        @XmlElement(name = "armor", type = ArmorCondition.class),
        @XmlElement(name = "charge", type = ChargeCondition.class),
        @XmlElement(name = "targetflying", type = TargetFlyingCondition.class),
        @XmlElement(name = "selfflying", type = SelfFlyingCondition.class),
        @XmlElement(name = "combatcheck", type = CombatCheckCondition.class),
        @XmlElement(name = "chain", type = ChainCondition.class),
        @XmlElement(name = "back", type = BackCondition.class),
        @XmlElement(name = "form", type = FormCondition.class)})
    protected List<Condition> conditions;

    /**
     * Gets the value of the conditions property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the conditions property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getConditions().add(newItem);
     * </pre>
     */
    public List<Condition> getConditions() {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        return this.conditions;
    }

    public boolean validate(Skill skill) {
        if (conditions != null) {
            for (Condition condition : getConditions()) {
                if (!condition.validate(skill)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validate(Stat2 stat, IStatFunction statFunction) {
        if (conditions != null) {
            for (Condition condition : getConditions()) {
                if (!condition.validate(stat, statFunction)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validate(Effect effect) {
        if (conditions != null) {
            for (Condition condition : getConditions()) {
                if (!condition.validate(effect)) {
                    return false;
                }
            }
        }
        return true;
    }
}
