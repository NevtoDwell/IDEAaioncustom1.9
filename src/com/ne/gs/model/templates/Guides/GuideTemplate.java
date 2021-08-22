/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.Guides;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GuideTemplate")
public class GuideTemplate {

    @XmlAttribute(name = "level")
    private int level;
    @XmlAttribute(name = "classType")
    private PlayerClass classType;
    @XmlAttribute(name = "title")
    private String title;
    @XmlAttribute(name = "race")
    private Race race;
    @XmlElement(name = "reward_info")
    private String rewardInfo = StringUtils.EMPTY;
    @XmlElement(name = "message")
    private String message = StringUtils.EMPTY;
    @XmlElement(name = "select")
    private String select = StringUtils.EMPTY;
    @XmlElement(name = "survey")
    private List<SurveyTemplate> surveys;
    @XmlAttribute(name = "rewardCount")
    private int rewardCount;
    @XmlTransient
    private boolean isActivated = true;

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @return the classId
     */
    public PlayerClass getPlayerClass() {
        return classType;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the race
     */
    public Race getRace() {
        return race;
    }

    /**
     * @return the surveys
     */
    public List<SurveyTemplate> getSurveys() {
        return surveys != null ? surveys : Collections.<SurveyTemplate>emptyList();
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the select
     */
    public String getSelect() {
        return select;
    }

    /**
     * @return the select
     */
    public String getRewardInfo() {
        return rewardInfo;
    }

    public int getRewardCount() {
        return rewardCount;
    }

    /**
     * @return the isActivated
     */
    public boolean isActivated() {
        return isActivated;
    }

    /**
     * @param isActivated
     *     the isActivated to set
     */
    public void setActivated(boolean isActivated) {
        this.isActivated = isActivated;
    }
}
