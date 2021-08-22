package com.ne.gs.services.custom;

import java.util.HashSet;
import java.util.Set;

import com.ne.gs.configs.main.CustomConfig;

/**
 * 
 * @author ViAl
 *
 */
public class KeyDropTuningService {

	private Set<Integer> excludedKeys;
	
	private KeyDropTuningService() {
		this.excludedKeys = new HashSet<>();
		if(CustomConfig.KEYS_WITH_ORIGINAL_CHANCE == null || CustomConfig.KEYS_WITH_ORIGINAL_CHANCE.isEmpty()) {
			return;
		}
		
		String[] data = CustomConfig.KEYS_WITH_ORIGINAL_CHANCE.split(",");
		if(data.length > 0) {
			for(String entry : data) {
				try {
					Integer keyId = Integer.parseInt(entry);
					this.excludedKeys.add(keyId);
				}
				catch(NumberFormatException e) {
					
				}
			}	
		}
	}
	
	public float getModifiedChance(int keyItemId, float originalChance) {
		if(this.excludedKeys.contains(keyItemId))
			return originalChance;
		else
			return 100f;
	}
	
    public static KeyDropTuningService getInstance() {
        return SingletonHolder.instance;
    }
    
    private static final class SingletonHolder {
        protected static final KeyDropTuningService instance = new KeyDropTuningService();
    }
}
