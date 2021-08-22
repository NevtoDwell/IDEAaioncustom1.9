/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.portal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

import com.ne.gs.configs.main.GSConfig;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PortalReq")
public class PortalReq {

    @XmlElement(name = "quest_req")
    protected List<QuestReq> questReq;

    @XmlElement(name = "item_req")
    protected List<ItemReq> itemReq;

    @XmlAttribute(name = "min_level")
    protected int minLevel;

    @XmlAttribute(name = "max_level")
    protected int maxLevel = GSConfig.PLAYER_MAX_LEVEL;

    @XmlAttribute(name = "kinah_req")
    protected int kinahReq;

    @XmlAttribute(name = "title_id")
    protected int titleId;

    @XmlAttribute(name = "err_level")
    protected int errLevel;

	@XmlAttribute(name = "err_message")
	protected int errMsg;

    public List<QuestReq> getQuestReq() {
        return questReq;
    }

    public List<ItemReq> getItemReq() {
        return itemReq;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int value) {
        minLevel = value;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int value) {
        maxLevel = value;
    }

    public int getKinahReq() {
        return kinahReq;
    }

    public void setKinahReq(int value) {
        kinahReq = value;
    }

    public int getTitleId() {
        return titleId;
    }

    public int getErrLevel() {
        return errLevel;
    }

	public int getErrMsg() {
		return errMsg;
	}
}
