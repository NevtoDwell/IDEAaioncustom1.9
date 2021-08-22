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

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.questEngine.QuestEngine;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.services.ClassChangeService;

/**
 * @author KKnD , orz, avol
 */
public class CM_DIALOG_SELECT extends AionClientPacket {

    /**
     * Target object id that client wants to TALK WITH or 0 if wants to unselect
     */
    private int targetObjectId;
    private int dialogId;
    private int extendedRewardIndex;
    @SuppressWarnings("unused")
    private int lastPage;
    private int questId;

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(CM_DIALOG_SELECT.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        targetObjectId = readD();// empty
        dialogId = readH(); // total no of choice
        extendedRewardIndex = readH();
        lastPage = readH();
        questId = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (player.isTrading()) {
            return;
        }

        if (targetObjectId == 0 || targetObjectId == player.getObjectId()) {
            if (QuestEngine.getInstance().onDialog(new QuestEnv(null, player, questId, dialogId))) {
                return;
            }
            // FIXME client sends unk1=1, targetObjectId=0, dialogId=2 (trader) => we miss some packet to close window
            ClassChangeService.changeClassToSelection(player, dialogId);
            return;
        }

        VisibleObject obj = player.getKnownList().getObject(targetObjectId);

        if (obj != null && obj instanceof Creature) {
            Creature creature = (Creature) obj;
            creature.getController().onDialogSelect(dialogId, player, questId, extendedRewardIndex);
        }
        // log.info("id: "+targetObjectId+" dialogId: " + dialogId +" unk1: " + unk1 + " questId: "+questId);
    }
}
