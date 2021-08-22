/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team.legion.Legion;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_LEGION_INFO;
import com.ne.gs.services.LegionService;
import com.ne.gs.services.NameRestrictionService;

/**
 * @author Simple
 */
public class CM_LEGION extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_LEGION.class);

    /**
     * exOpcode and the rest
     */
    private int exOpcode;
    private short deputyPermission;
    private short centurionPermission;
    private short legionarPermission;
    private short volunteerPermission;
    private int rank;
    private String legionName;
    private String charName;
    private String newNickname;
    private String announcement;
    private String newSelfIntro;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        exOpcode = readC();

        switch (exOpcode) {
            /** Create a legion **/
            case 0x00:
                readD(); // 00 78 19 00 40
                legionName = readS();
                break;
            /** Invite to legion **/
            case 0x01:
                readD(); // empty
                charName = readS();
                break;
            /** Leave legion **/
            case 0x02:
                readD(); // empty
                readH(); // empty
                break;
            /** Kick member from legion **/
            case 0x04:
                readD(); // empty
                charName = readS();
                break;
            /** Appoint a new Brigade General **/
            case 0x05:
                readD();
                charName = readS();
                break;
            /** Appoint Centurion **/
            case 0x06:
                rank = readD();
                charName = readS();
                break;
            /** Demote to Legionary **/
            case 0x07:
                readD(); // char id? 00 78 19 00 40
                charName = readS();
                break;
            /** Refresh legion info **/
            case 0x08:
                readD();
                readH();
                break;
            /** Edit announcements **/
            case 0x09:
                readD(); // empty or char id?
                announcement = readS();
                break;
            /** Change self introduction **/
            case 0x0A:
                readD(); // empty char id?
                newSelfIntro = readS();
                break;
            /** Edit permissions **/
            case 0x0D:
                deputyPermission = (short) readH();
                centurionPermission = (short) readH();
                legionarPermission = (short) readH();
                volunteerPermission = (short) readH();
                break;
            /** Level legion up **/
            case 0x0E:
                readD(); // empty
                readH(); // empty
                break;
            case 0x0F:
                charName = readS();
                newNickname = readS();
                break;
            default:
                log.info("Unknown Legion exOpcode? 0x" + Integer.toHexString(exOpcode).toUpperCase());
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();
        if (activePlayer.isLegionMember()) {
            Legion legion = activePlayer.getLegion();

            if (charName != null) {
                LegionService.getInstance().handleCharNameRequest(exOpcode, activePlayer, charName, newNickname, rank);
            } else {
                switch (exOpcode) {
                    /** Refresh legion info **/
                    case 0x08:
                        sendPacket(new SM_LEGION_INFO(legion));
                        break;
                    /** Edit announcements **/
                    case 0x09:
                        LegionService.getInstance().handleLegionRequest(exOpcode, activePlayer, announcement);
                        break;
                    /** Change self introduction **/
                    case 0x0A:
                        LegionService.getInstance().handleLegionRequest(exOpcode, activePlayer, newSelfIntro);
                        break;
                    /** Edit permissions **/
                    case 0x0D:
                        if (activePlayer.getLegionMember().isBrigadeGeneral()) {
                            LegionService.getInstance().changePermissions(legion, deputyPermission, centurionPermission, legionarPermission,
                                volunteerPermission);
                        }
                        break;
                    /** Misc. **/
                    default:
                        LegionService.getInstance().handleLegionRequest(exOpcode, activePlayer);
                        break;
                }
            }
        } else {
            switch (exOpcode) {
                /** Create a legion **/
                case 0x00:
                    if (NameRestrictionService.isForbiddenWord(legionName)) {
                        activePlayer.sendMsg("You are trying to use a forbidden name. Choose another one!");
                    } else {
                        LegionService.getInstance().createLegion(activePlayer, legionName);
                    }
                    break;
            }
        }
    }
}
