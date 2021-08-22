package com.ne.gs.model.templates.custom_quests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EndProperties")
public class EndProperties {

    @XmlAttribute(name = "player_kills")
    private int playerKills;
    @XmlAttribute(name = "monster_kills")
    private int monsterKills;
    @XmlAttribute(name = "monster_id")
    private String monsterId;

    public int getPlayerKills() {
        return playerKills;
    }

    public int getMonsterKills() {
        return monsterKills;
    }

    public String getMonsterId() {
        return monsterId;
    }

}
