package com.ne.gs.model.templates.custom_quests;

import com.ne.gs.modules.pvpevent.PvpRewardList;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestRewards")
public class QuestRewards {
    @XmlElement(name = "rewards")
    private PvpRewardList rewards;
    @XmlAttribute(name = "complete_count")
    private int completeCount;

    public PvpRewardList getPollRewards() {
        return rewards;
    }

    public int getCompleteCount() {
        return completeCount;
    }
}