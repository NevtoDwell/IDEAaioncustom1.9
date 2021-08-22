package com.ne.gs.dataholders;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;

import com.ne.gs.model.templates.onlinebonus.Bonus;

/**
 * 
 * @author ViAl
 * 
 */
public class OnlineBonusData {
	@XmlElement(name = "bonus")
	private List<Bonus> bonuses;

	private final TIntObjectHashMap<Bonus> bonusData = new TIntObjectHashMap<>();
	
	public Bonus getBonusForTime(int timeInMinutes) {
		return bonusData.get(timeInMinutes);
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		bonusData.clear();
		for (Bonus bonus : bonuses)
			bonusData.put(bonus.getTimeInMinutes(), bonus);
		bonuses.clear();
		bonuses = null;
	}

	public int size() {
		return bonusData.size();
	}
}
