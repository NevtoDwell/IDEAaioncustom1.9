package com.ne.gs.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection.State;
import com.ne.gs.network.aion.serverpackets.SM_CHAT_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.world.World;


/**
 * @author prix
 */
public class CM_CHAT_WINDOW extends AionClientPacket {

	private String playerName;

	private int unk;
	protected Logger log = LoggerFactory.getLogger(getClass().getName());
	

	@Override
	protected void readImpl() {
		this.playerName = readS();
		log.info("pocket read player name = " + this.playerName + " unk = " + this.unk);
		
	}

	@Override
	protected void runImpl() {
		final Player player = getConnection().getActivePlayer();
		final Player target = World.getInstance().findPlayer(this.playerName); 					
		if (target == null)
		{
			 PacketSendUtility.sendPck(player, SM_SYSTEM_MESSAGE.STR_MSG_ASK_PCINFO_LOGOFF);
			 log.info("target null");
			 
			 return;
		}
		
		int a = 0;
		if(target.isInGroup2())
		{
			  a = 2;
		}
		

					PacketSendUtility.sendPck(player, new SM_CHAT_WINDOW(target,a));
					log.info("PacketSend target = " + target + " group =");
			
		
	}
}
