/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author userd
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EventStartCondition")
public class EventStartCondition {

    @XmlElement(name = "single_players_to_start")
    protected int singlePlayersToStart = 0;
    @XmlElement(name = "groups_to_start")
    protected int groupsToStart = 0;
    @XmlElement(name = "players_for_each_group_to_start")
    protected int playersForEachGroup = 0;

    public boolean isGroupCondition() {
        return this.groupsToStart > 0;
    }

    public int getSinglePlayersToStart() {
        return singlePlayersToStart;
    }

    public int getGroupsToStart() {
        return groupsToStart;
    }

    public int getPlayersForEachGroup() {
        return playersForEachGroup;
    }
}
