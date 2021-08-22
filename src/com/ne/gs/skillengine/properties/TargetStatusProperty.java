/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.mw.TempConst;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.skillengine.effect.AbnormalState;
import com.ne.gs.skillengine.model.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kecimis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetStatusProperty")
public final class TargetStatusProperty {

    /**
     * @param skill
     * @param properties
     *
     * @return
     */
    public static boolean set(Skill skill, Properties properties) {
        if (skill.getEffectedList().size() != 1)
            return false;

        List<String> targetStatus = properties.getTargetStatus();

        Creature effected = skill.getFirstTarget();

        for (String status : targetStatus) {
            if (effected.getEffectController().isAbnormalSet(AbnormalState.valueOf(status)))
                return true;
        }

        return false;
    }
}
