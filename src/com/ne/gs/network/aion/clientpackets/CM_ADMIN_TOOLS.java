/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

//import com.ne.commons.Util;
//import com.ne.gs.model.account.AdminCommands;
//import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
//import com.ne.gs.network.aion.serverpackets.SM_MAIL_SERVICE;
//import com.ne.gs.network.aion.serverpackets.SM_TARGET_SELECTED;
///import com.ne.gs.network.aion.serverpackets.SM_TARGET_UPDATE;
//import com.ne.gs.utils.PacketSendUtility;
//import com.ne.gs.world.World;

/**
 * Created with IntelliJ IDEA.
 * User: Alexsis
 * Date: 14.12.12
 * Time: 20:12
 * To change this template use File | Settings | File Templates.
 */
public class CM_ADMIN_TOOLS extends AionClientPacket {

    private String request;
    private String[] forSplit;
    private int commandId;
    private String playerName;

    @Override
    protected void readImpl() {
//        request = readS();
//        forSplit = request.split(" ");
//        commandId = AdminCommands.valueOf(forSplit[0]).getValue();
//        playerName = forSplit[1];
        // FIXME
//        java.lang.IllegalArgumentException: No enum constant com.ne.gs.model.account.AdminCommands.kill
//	at java.lang.Enum.valueOf(Unknown Source) ~[na:1.7.0_21]
//	at com.ne.gs.model.account.AdminCommands.valueOf(AdminCommands.java:18) ~[gameserver-3.0.1.jar:3.0.1]
//	at com.ne.gs.network.aion.clientpackets.CM_ADMIN_TOOLS.readImpl(CM_ADMIN_TOOLS.java:39) ~[gameserver-3.0.1.jar:3.0.1]
//	at com.ne.commons.network.packet.BaseClientPacket.read(BaseClientPacket.java:87) ~[commons-3.0.1.jar:3.0.1]
//	at com.ne.gs.network.aion.AionConnection.processData(AionConnection.java:186) [gameserver-3.0.1.jar:3.0.1]
//	at com.ne.commons.network.Dispatcher.parse(Dispatcher.java:229) [commons-3.0.1.jar:3.0.1]
//	at com.ne.commons.network.Dispatcher.read(Dispatcher.java:189) [commons-3.0.1.jar:3.0.1]
//	at com.ne.commons.network.AcceptReadWriteDispatcherImpl.dispatch(AcceptReadWriteDispatcherImpl.java:71) [commons-3.0.1.jar:3.0.1]
//	at com.ne.commons.network.Dispatcher.run(Dispatcher.java:95) [commons-3.0.1.jar:3.0.1]
    }

    @Override
    protected void runImpl() {
//        Player admin = getConnection().getActivePlayer();
//        Player player = World.getInstance().findPlayer(Util.convertName(playerName));
//
//        if (player == null) {
//        	admin.sendMsg("Could not find an online player with that name.");
//            return;
//        }
//        switch (commandId) {
//            case 0:
//                //TODO Show mail box
//                player.sendPck(new SM_MAIL_SERVICE(player, player.getMailbox().getLetters()));
//                break;
//            case 1:
//                break;
//            case 3:
//                // TODO teleportto
//                break;
//            case 4:
//                //TODO Player Status
//                break;
//            case 5:
//                admin.setTarget(player);
//                sendPacket(new SM_TARGET_SELECTED(player));
//                PacketSendUtility.broadcastPacket(admin, new SM_TARGET_UPDATE(player));
//                break;
//            case 7:
//                //TODO Player Legion Info
//                break;
//            case 8:
//                //TODO gm friend list
//                break;
//            case 9:
//                //TODO recall
//            default:
//                break;
//        }
    }

}
