/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author userd
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Rank")
public class EventRankTemplate {

    @XmlAttribute(name = "id", required = true)
    private int id;
    @XmlAttribute(name = "ap")
    private int ap = 0;
    @XmlAttribute(name = "gp")
    private int gp = 0;
    @XmlElement(name = "reward_item_group")
    private List<EventRewardItemGroup> rewards;

    public int getRankId() {
        return id;
    }

    public List<EventRewardItemGroup> getRewards() {
        if (this.rewards == null) {
            this.rewards = new ArrayList<>();
        }
        return rewards;
    }

    public int getAp() {
        return ap;
    }

    public int getGamePoint() {
        return gp;
    }
}
