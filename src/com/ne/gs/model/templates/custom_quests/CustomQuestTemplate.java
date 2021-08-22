package com.ne.gs.model.templates.custom_quests;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * @author ViAl
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustomQuestTemplate")
public class CustomQuestTemplate {

    @XmlElement(name = "schedule")
    private List<Schedule> schedule;
    @XmlElement(name = "start_properties")
    private StartProperties startProps;
    @XmlElement(name = "end_properties")
    private EndProperties endProps;
    @XmlElement(name = "announcements")
    private Announcements announcements;
    @XmlElement(name = "quest_rewards")
    private List<QuestRewards> rewards;
    @XmlAttribute(name = "id")
    private int id;
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "world_id")
    private int worldId;
    @XmlAttribute(name = "reset_after_days")
    private int resetAfterDays;
    @XmlAttribute(name = "amount_per_day")
    private int amountPerDay;
    @XmlAttribute(name = "solo_kill")
    private boolean soloKill = false;

    public List<Schedule> getSchedule() {
        return schedule;
    }

    public StartProperties getStartProps() {
        return startProps;
    }

    public EndProperties getEndProps() {
        return endProps;
    }

    public Announcements getAnnouncements() {
        return announcements;
    }

    public List<QuestRewards> getRewards() {
        return rewards;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getWorldId() {
        return worldId;
    }

    public int getResetAfterDays() {
        return resetAfterDays;
    }

    public int getAmountPerDay() {
        return amountPerDay;
    }

    public boolean isSoloKill() {
        return soloKill;
    }
}