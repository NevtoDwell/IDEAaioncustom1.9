/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ne.gs.eventNewEngine.events.xml;

import com.ne.commons.utils.Rnd;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javolution.util.FastList;

/**
 *
 * @author userd
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rItemGroup")
public class EventRewardItemGroup {

    // Если true - то из всей группы предметов будет выбрано указанное кол-во предметов 
    @XmlAttribute(name = "random")
    public boolean isRandom = false;
    @XmlAttribute(name = "random_count")
    public int randomCount = 1;
    @XmlElement(name = "item")
    public List<EventRewardItem> items;

    public boolean isRandom() {
        return isRandom;
    }

    public int getRandomCount() {
        return randomCount;
    }

    public List<EventRewardItem> getItems() {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        if (this.isRandom) {
            List<EventRewardItem> result = new FastList<>(items);
            while (result.size() > this.randomCount) {
                result.remove(Rnd.get(0, result.size() - 1));
            }
            return result;
        }
        return items;
    }
}
