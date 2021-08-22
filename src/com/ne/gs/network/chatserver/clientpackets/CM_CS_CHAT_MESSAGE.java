package com.ne.gs.network.chatserver.clientpackets;

import com.ne.gs.network.chatserver.CsClientPacket;
import com.ne.gs.services.custom.ChatServerLogService;

/**
 * 
 * @author KID
 * 
 */
public class CM_CS_CHAT_MESSAGE extends CsClientPacket {
	private int type;
	private int id;
	private String text;

	public CM_CS_CHAT_MESSAGE(int opcode) {
		super(opcode);
	}

	@Override
	protected void readImpl() {
		this.type = readC();
		this.id = readD();
		this.text = readS();
	}

	@Override
	protected void runImpl() {
		ChatServerLogService.getInstance().notifyChatMessage(this.type, this.id, this.text);
	}
}
