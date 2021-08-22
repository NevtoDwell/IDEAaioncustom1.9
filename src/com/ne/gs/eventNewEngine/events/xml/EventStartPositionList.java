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
@XmlType(name = "EventStartPositions")
public class EventStartPositionList {

    @XmlAttribute(name = "use_group")
    protected boolean useGroup;
    @XmlElement(name = "position")
    protected List<EventStartPosition> positions;

    public List<EventStartPosition> getPositions() {
        return positions;
    }

    public List<EventStartPosition> getPositionForGroup(int id) {
        if (this.isUseGroup()) {
            List<EventStartPosition> rList = new ArrayList<>();
            for (EventStartPosition pos : this.positions) {
                if (pos.group == id) {
                    rList.add(pos);
                }
            }
            return rList;
        } else {
            return this.getPositions();
        }
    }

    public boolean isUseGroup() {
        return useGroup;
    }
}
