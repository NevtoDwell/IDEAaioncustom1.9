package com.ne.gs.services.custom;

import java.util.concurrent.ConcurrentHashMap;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.world.World;

/**
 * @author KID
 *
 */
public class ChatServerLogService {
	private static final ChatServerLogService controller = new ChatServerLogService();
	public static ChatServerLogService getInstance() {
		return controller;
	}
	
	private String[] textType = new String[] {"PUB", "TRD", "GRP", "JOB", "LNG",};
	
	private ConcurrentHashMap<Integer, Player> gms = new ConcurrentHashMap<>();

	public void notifyChatMessage(int type, int id, String text) {
		Player sender = World.getInstance().findPlayer(id);
		if(sender == null)
			return;
			
		for(Player gm: this.gms.values()) {
			gm.sendMsg("CS["+textType[type]+"]: "+sender.getName()+": "+text);
		}
	}
	
	public void evtLoggedIn(Player player) {
		this.gms.put(player.getObjectId(), player);
	}
	
	public void evtLoggedOut(Player player) {
		this.gms.remove(player.getObjectId());
	}
}
