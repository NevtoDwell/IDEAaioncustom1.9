/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author userd
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rItem")
public class EventRewardItem {

    @XmlAttribute(name = "item_id", required = true)
    private int itemId;
    @XmlAttribute(name = "count")
    private long count = 1;

    public long getCount() {
        return count;
    }

    public int getItemId() {
        return itemId;
    }
}
