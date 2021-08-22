/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.ne.gs.model.Gender;
import com.ne.gs.model.PlayerClass;
import com.ne.gs.model.Race;
import com.ne.gs.model.templates.quest.*;

/**
 * @author MrPoke
 * @modified vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Quest"/*
                                                 * , propOrder = { "collectItems", "rewards", "questDrop", "startConds", "classPermitted",
												 * "genderPermitted", "questWorkItems", "fighterSelectableReward", "knightSelectableReward",
												 * "rangerSelectableReward", "assassinSelectableReward", "wizardSelectableReward",
												 * "elementalistSelectableReward", "priestSelectableReward", "chanterSelectableReward" }
												 */)
public class QuestTemplate {

    @XmlElement(name = "collect_items")
    protected CollectItems collectItems;
    @XmlElement(name = "inventory_items")
    protected InventoryItems inventoryItems;
    @XmlElement(name = "rewards")
    protected List<Rewards> rewards;
    @XmlElement(name = "bonus")
    protected List<QuestBonuses> bonus;
    @XmlElement(name = "extended_rewards")
    protected List<Rewards> extendedRewards;
    @XmlElement(name = "quest_drop")
    protected List<QuestDrop> questDrop;
    @XmlElement(name = "start_conditions")
    protected List<XMLStartCondition> startConds;
    @XmlList
    @XmlElement(name = "class_permitted")
    protected List<PlayerClass> classPermitted;
    @XmlElement(name = "gender_permitted")
    protected Gender genderPermitted;
    @XmlElement(name = "quest_work_items")
    protected QuestWorkItems questWorkItems;
    @XmlElement(name = "fighter_selectable_reward")
    protected List<QuestItems> fighterSelectableReward;
    @XmlElement(name = "knight_selectable_reward")
    protected List<QuestItems> knightSelectableReward;
    @XmlElement(name = "ranger_selectable_reward")
    protected List<QuestItems> rangerSelectableReward;
    @XmlElement(name = "assassin_selectable_reward")
    protected List<QuestItems> assassinSelectableReward;
    @XmlElement(name = "wizard_selectable_reward")
    protected List<QuestItems> wizardSelectableReward;
    @XmlElement(name = "elementalist_selectable_reward")
    protected List<QuestItems> elementalistSelectableReward;
    @XmlElement(name = "priest_selectable_reward")
    protected List<QuestItems> priestSelectableReward;
    @XmlElement(name = "chanter_selectable_reward")
    protected List<QuestItems> chanterSelectableReward;
    @XmlAttribute(name = "id", required = true)
    protected int id;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "nameId")
    protected Integer nameId;
    @XmlAttribute(name = "minlevel_permitted")
    protected Integer minlevelPermitted;
    @XmlAttribute(name = "maxlevel_permitted")
    protected int maxlevelPermitted;
    @XmlAttribute(name = "max_repeat_count")
    protected Integer maxRepeatCount;
    @XmlAttribute(name = "cannot_share")
    protected Boolean cannotShare;
    @XmlAttribute(name = "cannot_giveup")
    protected Boolean cannotGiveup;
    @XmlAttribute(name = "use_class_reward")
    protected Boolean useClassReward;
    @XmlAttribute(name = "race_permitted")
    protected Race racePermitted;
    @XmlAttribute(name = "combineskill")
    protected Integer combineskill;
    @XmlAttribute(name = "combine_skillpoint")
    protected Integer combineSkillpoint;
    @XmlAttribute(name = "timer")
    protected Boolean timer;
    @XmlAttribute(name = "category")
    protected QuestCategory category;
    @XmlAttribute(name = "repeat_cycle")
    protected QuestRepeatCycle repeatCycle;
    @XmlAttribute(name = "npcfaction_id")
    protected int npcFactionId;
    @XmlAttribute(name = "mentor_type")
    protected QuestMentorType mentorType = QuestMentorType.NONE;

    /**
     * Gets the value of the collectItems property.
     *
     * @return possible object is {@link CollectItems }
     */
    public CollectItems getCollectItems() {
        return collectItems;
    }

    public InventoryItems getInventoryItems() {
        return inventoryItems;
    }

    /**
     * Gets the value of the rewards property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the rewards property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getRewards().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link Rewards }
     */
    public List<Rewards> getRewards() {
        if (rewards == null) {
            rewards = new ArrayList<>();
        }
        return this.rewards;
    }

    public List<Rewards> getExtendedRewards() {
        if (extendedRewards == null) {
            extendedRewards = new ArrayList<>();
        }
        return this.extendedRewards;
    }

    public List<QuestBonuses> getBonus() {
        if (bonus == null) {
            bonus = new ArrayList<>();
        }
        return this.bonus;
    }

    /**
     * Gets the value of the questDrop property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the questDrop property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getQuestDrop().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestDrop }
     */
    public List<QuestDrop> getQuestDrop() {
        if (questDrop == null) {
            questDrop = new ArrayList<>();
        }
        return this.questDrop;
    }

    public List<XMLStartCondition> getXMLStartConditions() {
        if (startConds == null) {
            startConds = new ArrayList<>();
        }
        return startConds;
    }

    /**
     * Gets the value of the classPermitted property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the classPermitted property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getClassPermitted().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link PlayerClass }
     */
    public List<PlayerClass> getClassPermitted() {
        if (classPermitted == null) {
            classPermitted = new ArrayList<>();
        }
        return this.classPermitted;
    }

    /**
     * Gets the value of the genderPermitted property.
     *
     * @return possible object is {@link Gender }
     */
    public Gender getGenderPermitted() {
        return genderPermitted;
    }

    /**
     * Gets the value of the questWorkItems property.
     *
     * @return possible object is {@link QuestWorkItems }
     */
    public QuestWorkItems getQuestWorkItems() {
        return questWorkItems;
    }

    /**
     * Gets the value of the fighterSelectableReward property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the fighterSelectableReward property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getFighterSelectableReward().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getFighterSelectableReward() {
        if (fighterSelectableReward == null) {
            fighterSelectableReward = new ArrayList<>();
        }
        return this.fighterSelectableReward;
    }

    /**
     * Gets the value of the knightSelectableReward property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the knightSelectableReward property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getKnightSelectableReward().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getKnightSelectableReward() {
        if (knightSelectableReward == null) {
            knightSelectableReward = new ArrayList<>();
        }
        return this.knightSelectableReward;
    }

    /**
     * Gets the value of the rangerSelectableReward property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the rangerSelectableReward property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getRangerSelectableReward().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getRangerSelectableReward() {
        if (rangerSelectableReward == null) {
            rangerSelectableReward = new ArrayList<>();
        }
        return this.rangerSelectableReward;
    }

    /**
     * Gets the value of the assassinSelectableReward property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the assassinSelectableReward property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getAssassinSelectableReward().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getAssassinSelectableReward() {
        if (assassinSelectableReward == null) {
            assassinSelectableReward = new ArrayList<>();
        }
        return this.assassinSelectableReward;
    }

    /**
     * Gets the value of the wizardSelectableReward property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the wizardSelectableReward property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getWizardSelectableReward().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getWizardSelectableReward() {
        if (wizardSelectableReward == null) {
            wizardSelectableReward = new ArrayList<>();
        }
        return this.wizardSelectableReward;
    }

    /**
     * Gets the value of the elementalistSelectableReward property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the elementalistSelectableReward property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getElementalistSelectableReward().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getElementalistSelectableReward() {
        if (elementalistSelectableReward == null) {
            elementalistSelectableReward = new ArrayList<>();
        }
        return this.elementalistSelectableReward;
    }

    /**
     * Gets the value of the priestSelectableReward property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the priestSelectableReward property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getPriestSelectableReward().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getPriestSelectableReward() {
        if (priestSelectableReward == null) {
            priestSelectableReward = new ArrayList<>();
        }
        return this.priestSelectableReward;
    }

    /**
     * Gets the value of the chanterSelectableReward property.
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
     * the chanterSelectableReward property.
     * <p/>
     * For example, to add a new item, do as follows:
     * <p/>
     * <pre>
     * getChanterSelectableReward().add(newItem);
     * </pre>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link QuestItems }
     */
    public List<QuestItems> getChanterSelectableReward() {
        if (chanterSelectableReward == null) {
            chanterSelectableReward = new ArrayList<>();
        }
        return this.chanterSelectableReward;
    }

    /**
     * Gets the value of the id property.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the nameId property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getNameId() {
        return nameId;
    }

    /**
     * Gets the value of the minlevelPermitted property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getMinlevelPermitted() {
        return minlevelPermitted;
    }

    public int getMaxlevelPermitted() {
        return maxlevelPermitted;
    }

    /**
     * Gets the value of the maxRepeatCount property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getMaxRepeatCount() {
        if (maxRepeatCount == null || !(maxRepeatCount > 1)) {
            return 1;
        }
        return maxRepeatCount;
    }

    /**
     * Gets the value of the cannotShare property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isCannotShare() {
        if (cannotShare == null) {
            return false;
        } else {
            return cannotShare;
        }
    }

    /**
     * Gets the value of the cannotGiveup property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isCannotGiveup() {
        if (cannotGiveup == null) {
            return false;
        } else {
            return cannotGiveup;
        }
    }

    /**
     * Gets the value of the useClassReward property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isUseClassReward() {
        if (useClassReward == null) {
            return false;
        } else {
            return useClassReward;
        }
    }

    public boolean isRepeatable() {
        return getMaxRepeatCount() > 1;
    }

    /**
     * Gets the value of the racePermitted property.
     *
     * @return possible object is {@link Race }
     */
    public Race getRacePermitted() {
        return racePermitted;
    }

    /**
     * Gets the value of the combineskill property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getCombineSkill() {
        return combineskill;
    }

    /**
     * Gets the value of the combineSkillpoint property.
     *
     * @return possible object is {@link Integer }
     */
    public Integer getCombineSkillPoint() {
        return combineSkillpoint;
    }

    /**
     * Gets the value of the timer property.
     *
     * @return possible object is {@link Integer }
     */

    public boolean isTimer() {
        if (timer == null) {
            return false;
        } else {
            return timer;
        }
    }

    public QuestCategory getCategory() {
        if (category == null) {
            category = QuestCategory.QUEST;
        }
        return category;
    }

    /**
     * @return the mentor
     */
    public boolean isMentor() {
        return mentorType != QuestMentorType.NONE;
    }

    /**
     * @return the mentor
     */
    public QuestMentorType getMentorType() {
        return mentorType;
    }

    public QuestRepeatCycle getRepeatCycle() {
        return repeatCycle;
    }

    public int getNpcFactionId() {
        return npcFactionId;
    }

    public boolean isTimeBased() {
        return repeatCycle != null;
    }

    public boolean isDaily() {
        return isTimeBased() && repeatCycle == QuestRepeatCycle.ALL;
    }

    public boolean isWeekly() {
        return isTimeBased() && !isDaily();
    }

    public boolean isMaster() {
        return getCombineSkillPoint() != null && getCombineSkillPoint() == 399;
    }

	public boolean isExpert() {
		return getCombineSkillPoint() != null && getCombineSkillPoint() == 499;
	}
}
