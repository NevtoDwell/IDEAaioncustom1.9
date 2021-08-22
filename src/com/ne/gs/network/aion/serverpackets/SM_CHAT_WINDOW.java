package com.ne.gs.network.aion.serverpackets;


import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team2.alliance.PlayerAlliance;
import com.ne.gs.model.team2.alliance.PlayerAllianceMember;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author prix
 */
public class SM_CHAT_WINDOW extends AionServerPacket {

	private Player target;
	private int IsGroup = 0;
	protected Logger log = LoggerFactory.getLogger(getClass().getName());
	public SM_CHAT_WINDOW(Player target,int is) {
		this.target = target;
		this.IsGroup=is;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (target == null)
			return;

		PlayerGroup group = null;
		if (this.IsGroup == 1)
		{
				group = target.getPlayerGroup2();
		}
		else if(this.IsGroup == 2)
		{
			writeC(1);
			writeS(this.target.getName());
		      writeS(this.target.getLegion() != null ? this.target.getLegion().getLegionName() : "");
		      writeC(this.target.getLevel());
		      writeH(this.target.getPlayerClass().getClassId());
		      writeS(this.target.getCommonData().getNote());
		      writeD(1);		    
		}
		if (group == null) {
			log.info("nogrup packet send");
			writeC(1);
		      writeS(this.target.getName());
		      writeS(this.target.getLegion() != null ? this.target.getLegion().getLegionName() : "");
		      writeC(this.target.getLevel());
		      writeH(this.target.getPlayerClass().getClassId());
		      writeS(this.target.getCommonData().getNote());
		      writeD(1);
		}
		else {
			if (this.target.isInGroup2())
		      {
				log.info("grup packet send");
		        writeC(2);
		        writeS(this.target.getName());
		        
		        PlayerGroup group1 = this.target.getPlayerGroup2();
		        
		        writeD(group1.getTeamId().intValue());
		        writeS((group1.getLeader()).getName());
		        
		        Collection<Player> members = group1.getMembers();
		        for (Player groupMember : members) {
		          writeC(groupMember.getLevel());
		        }
		        for (int i = group1.size(); i < 6; i++) {
		          writeC(0);
		        }
		        for (Player groupMember : members) {
		          writeC(groupMember.getPlayerClass().getClassId());
		        }
		        for (int i = group1.size(); i < 6; i++) {
		          writeC(0);
		        }
		      }
		      else if (this.target.isInAlliance2())
		      {
		    	  log.info("alliance packet send");
		        writeC(2);
		        writeS(this.target.getName());
		        
		        PlayerAlliance alliance = this.target.getPlayerAlliance2();
		        
		        writeD(alliance.getTeamId().intValue());
		        writeS(((PlayerAllianceMember)alliance.getLeader()).getName());
		        
		        Collection<Player> members = alliance.getMembers();
		        for (Player groupMember : members) {
		          writeC(groupMember.getLevel());
		        }
		        for (int i = alliance.size(); i < 24; i++) {
		          writeC(0);
		        }
		        for (Player groupMember : members) {
		          writeC(groupMember.getPlayerClass().getClassId());
		        }
		        for (int i = alliance.size(); i < 24; i++) {
		          writeC(0);
		        }
		      }
		      else
		      {
		    	  log.info("undef packet send");
		        writeC(4);
		        writeS(this.target.getName());
		        writeD(0);
		        writeC(this.target.getPlayerClass().getClassId());
		        writeC(this.target.getLevel());
		        writeC(0);
		      }
		}
	}
}
