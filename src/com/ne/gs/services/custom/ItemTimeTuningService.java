package com.ne.gs.services.custom;

import java.util.HashMap;
import java.util.Map;

import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.skillengine.model.Effect;


/**
 * 
 * @author ViAl
 *
 */
public class ItemTimeTuningService {
	
	private Map<Integer, Integer> itemTimes;
	
	private ItemTimeTuningService() {
		this.itemTimes = new HashMap<>();
		if(CustomConfig.ITEMS_TIME_OVERRIDE == null || CustomConfig.ITEMS_TIME_OVERRIDE.isEmpty()) {
			return;
		}
		
		String[] data = CustomConfig.ITEMS_TIME_OVERRIDE.split(",");
		if(data.length > 0) {
			for(String entry : data) {
				addTimeOverride(entry);
			}	
		}
		else {
			addTimeOverride(CustomConfig.ITEMS_TIME_OVERRIDE);
		}
	}
	
	private void addTimeOverride(String entry) {
		String[] itemInfo = entry.split(":");
		int itemId = Integer.parseInt(itemInfo[0]);
		int time = Integer.parseInt(itemInfo[1]);
		this.itemTimes.put(itemId, time);
	}
	
	/**
	 * Returns overriden time for item, if have such, or 0 if havent
	 * @param itemId
	 * @return
	 */
	private int getOverridenTime(int itemId) {
		if(itemTimes.containsKey(itemId))
			return itemTimes.get(itemId);
		else
			return 0;
	}
	
	public void onItemEffectCreate(Effect effect) {
        if(effect.getItemTemplate() != null) {
        	int durationOverride = getOverridenTime(effect.getItemTemplate().getTemplateId());
            if(durationOverride != 0) {
            	effect.setDuration(durationOverride * 1000);
                effect.setForcedDuration(true);
            }
        }
	}
	
    public static ItemTimeTuningService getInstance() {
        return SingletonHolder.instance;
    }
    
    private static final class SingletonHolder {
        protected static final ItemTimeTuningService instance = new ItemTimeTuningService();
    }
}
