/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.model;

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.questEngine.QuestEngine;

/**
 * @author MrPoke
 */
public class QuestEnv {

    private VisibleObject visibleObject;
    private Player player;
    private int questId;
    private int dialogId;
    private int extendedRewardIndex;

    public QuestEnv(VisibleObject visibleObject, Player player, int questId, int dialogId) {
        this.visibleObject = visibleObject;
        this.player = player;
        this.questId = questId;
        this.dialogId = dialogId;
    }

    public VisibleObject getVisibleObject() {
        return visibleObject;
    }

    public void setVisibleObject(VisibleObject visibleObject) {
        this.visibleObject = visibleObject;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getQuestId() {
        return questId;
    }

    public void setQuestId(Integer questId) {
        this.questId = questId;
    }

    public int getDialogId() {
        return dialogId;
    }

    public void setDialogId(Integer dialogId) {
        this.dialogId = dialogId;
    }

    public QuestDialog getDialog() {
        QuestDialog dialog = QuestEngine.getInstance().getDialog(dialogId);
        if (dialog == null) {
            return QuestDialog.NULL;
        }
        return dialog;
    }

    public int getTargetId() {
        return visibleObject != null ? visibleObject.getObjectTemplate().getTemplateId() : 0;
    }

    public int getExtendedRewardIndex() {
        return this.extendedRewardIndex;
    }

    public void setExtendedRewardIndex(int index) {
        this.extendedRewardIndex = index;
    }
}
