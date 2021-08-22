package com.ne.gs.model.templates.custom_quests;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Announcements")
public class Announcements {

    @XmlAttribute(name = "on_quest_start")
    private String onQuestStart;
    @XmlAttribute(name = "on_quest_end")
    private String onQuestEnd;
    @XmlAttribute(name = "on_quest_restart")
    private String onQuestRestart;

    public String getOnQuestRestart() {
        return onQuestRestart;
    }

    public String getOnQuestStart() {
        return onQuestStart;
    }

    public String getOnQuestEnd() {
        return onQuestEnd;
    }

}