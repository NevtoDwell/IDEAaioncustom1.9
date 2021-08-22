package com.ne.gs.model.templates.custom_quests;

import com.ne.gs.model.Race;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StartProperties")
public class StartProperties {

    @XmlAttribute(name = "min_level")
    private int minLevel;
    @XmlAttribute(name = "max_level")
    private int maxLevel;
    @XmlAttribute(name = "race")
    private Race race;

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public Race getRace() {
        return race;
    }
}