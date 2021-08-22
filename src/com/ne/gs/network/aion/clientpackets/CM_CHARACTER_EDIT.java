/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.PlayerAppearanceDAO;
import com.ne.gs.database.dao.PlayerDAO;
import com.ne.gs.model.Gender;
import com.ne.gs.model.account.Account;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerAppearance;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.player.PlayerEnterWorldService;
import com.ne.gs.services.player.PlayerService;

/**
 * In this packets aion client is requesting edit of character.
 *
 * @author IlBuono
 */
public class CM_CHARACTER_EDIT extends AionClientPacket {

    private int objectId;

    private boolean gender_change;

    private boolean check_ticket = true;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        AionConnection client = getConnection();
        Account account = client.getAccount();
        objectId = readD();
        readB(52);
        if (account.getPlayerAccountData(objectId) == null) {
            return;
        }
        Player player = PlayerService.getPlayer(objectId, account);
        if (player == null) {
            return;
        }
        PlayerCommonData playerCommonData = player.getCommonData();
        PlayerAppearance playerAppearance = player.getPlayerAppearance();
        // Before modify appearance, we do a check of ticket
        int gender = readD();
        gender_change = playerCommonData.getGender().getGenderId() == gender ? false : true;
        if (!gender_change) {
            if (player.getInventory().getItemCountByItemId(169650000) == 0 && player.getInventory().getItemCountByItemId(169650001) == 0) {
                check_ticket = false;
                return;
            }
        } else if (player.getInventory().getItemCountByItemId(169660000) == 0 && player.getInventory().getItemCountByItemId(169660001) == 0) {
            check_ticket = false;
            return;
        }
        playerCommonData.setGender(gender == 0 ? Gender.MALE : Gender.FEMALE);
        readD(); // race
        readD(); // player class

        playerAppearance.setVoice(readD());
        playerAppearance.setSkinRGB(readD());
        playerAppearance.setHairRGB(readD());
        playerAppearance.setEyeRGB(readD());
        playerAppearance.setLipRGB(readD());
        playerAppearance.setFace(readC());
        playerAppearance.setHair(readC());
        playerAppearance.setDeco(readC());
        playerAppearance.setTattoo(readC());
        playerAppearance.setFaceContour(readC());
        playerAppearance.setExpression(readC());
        readC(); // always 4 o0 // 5 in 1.5.x
        playerAppearance.setJawLine(readC());
        playerAppearance.setForehead(readC());

        playerAppearance.setEyeHeight(readC());
        playerAppearance.setEyeSpace(readC());
        playerAppearance.setEyeWidth(readC());
        playerAppearance.setEyeSize(readC());
        playerAppearance.setEyeShape(readC());
        playerAppearance.setEyeAngle(readC());

        playerAppearance.setBrowHeight(readC());
        playerAppearance.setBrowAngle(readC());
        playerAppearance.setBrowShape(readC());

        playerAppearance.setNose(readC());
        playerAppearance.setNoseBridge(readC());
        playerAppearance.setNoseWidth(readC());
        playerAppearance.setNoseTip(readC());

        playerAppearance.setCheek(readC());
        playerAppearance.setLipHeight(readC());
        playerAppearance.setMouthSize(readC());
        playerAppearance.setLipSize(readC());
        playerAppearance.setSmile(readC());
        playerAppearance.setLipShape(readC());
        playerAppearance.setJawHeigh(readC());
        playerAppearance.setChinJut(readC());
        playerAppearance.setEarShape(readC());
        playerAppearance.setHeadSize(readC());

        playerAppearance.setNeck(readC());
        playerAppearance.setNeckLength(readC());

        playerAppearance.setShoulderSize(readC());

        playerAppearance.setTorso(readC());
        playerAppearance.setChest(readC()); // only woman
        playerAppearance.setWaist(readC());
        playerAppearance.setHips(readC());

        playerAppearance.setArmThickness(readC());

        playerAppearance.setHandSize(readC());
        playerAppearance.setLegThicnkess(readC());

        playerAppearance.setFootSize(readC());
        playerAppearance.setFacialRate(readC());

        readC(); // always 0
        playerAppearance.setArmLength(readC());
        playerAppearance.setLegLength(readC()); // wrong??
        playerAppearance.setShoulders(readC()); // 1.5.x May be ShoulderSize
        playerAppearance.setFaceShape(readC());
        readC();
        readC();
        readC();
        playerAppearance.setHeight(readF());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        AionConnection client = getConnection();
        PlayerEnterWorldService.enterWorld(client, objectId);
        Player player = client.getActivePlayer();
        if (!check_ticket) {
            if (!gender_change) {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_EDIT_CHAR_ALL_CANT_NO_ITEM);
            } else {
                player.sendPck(SM_SYSTEM_MESSAGE.STR_EDIT_CHAR_GENDER_CANT_NO_ITEM);
            }
        } else {
            // Remove ticket and save appearance
            if (!gender_change) {
                if (player.getInventory().getItemCountByItemId(169650000) > 0) {
                    player.getInventory().decreaseByItemId(169650000, 1);
                } else if (player.getInventory().getItemCountByItemId(169650001) > 0) {
                    player.getInventory().decreaseByItemId(169650001, 1);
                }
            } else {
                if (player.getInventory().getItemCountByItemId(169660000) > 0) {
                    player.getInventory().decreaseByItemId(169660000, 1);
                } else if (player.getInventory().getItemCountByItemId(169660001) > 0) {
                    player.getInventory().decreaseByItemId(169660001, 1);
                }
                GDB.get(PlayerDAO.class).storePlayer(player); // save new gender
            }
            GDB.get(PlayerAppearanceDAO.class).store(player); // save new appearance
        }
    }
}
