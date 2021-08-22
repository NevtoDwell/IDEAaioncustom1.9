package com.ne.gs.model.templates.onlinebonus;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author ViAl
 *
 */
@XmlRootElement(name = "reward_item")
public class RewardItem {
	@XmlAttribute(name = "id")
	private int id;
	@XmlAttribute(name = "amount")
	private int amount;
	
	public int getId() {
		return id;
	}
	public int getAmount() {
		return amount;
	}
}
