package com.ne.gs.model.templates.onlinebonus;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author ViAl
 * 
 */
@XmlRootElement(name = "bonus")
public class Bonus {

	@XmlElement(name = "reward_item")
	private List<RewardItem> rewardItems;
	@XmlAttribute(name = "time_mins")
	private int timeMins;
	@XmlAttribute(name = "reward_toll")
	private int rewardToll;
	@XmlAttribute(name = "reward_kinah")
	private int rewardKinah;
	@XmlAttribute(name = "random")
	private boolean random;
	@XmlAttribute(name = "reset_time")
	private boolean resetTime;

	public List<RewardItem> getRewardItems() {
		return rewardItems;
	}

	public int getTimeInMinutes() {
		return timeMins;
	}

	public int getRewardToll() {
		return rewardToll;
	}

	public int getRewardKinah() {
		return rewardKinah;
	}
	
	public boolean isRandomReward() {
		return random;
	}

	public boolean isResetTime() {
		return resetTime;
	}
}
