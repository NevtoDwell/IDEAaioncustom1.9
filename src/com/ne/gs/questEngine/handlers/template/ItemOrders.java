/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.template;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.ne.gs.questEngine.handlers.HandlerResult;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.QuestService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;

public class ItemOrders extends QuestHandler {

    private final int questId;
    private final int startItemId;
    private final int talkNpc1;
    private final int talkNpc2;
    private final int endNpcId;

    public ItemOrders(int questId, int startItemId, int talkNpc1, int talkNpc2, int endNpcId) {
        super(questId);
        this.startItemId = startItemId;
        this.questId = questId;
        this.talkNpc1 = talkNpc1;
        this.talkNpc2 = talkNpc2;
        this.endNpcId = endNpcId;
    }

    public void register() {
        qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
        qe.registerQuestItem(startItemId, questId);
        if (talkNpc1 != 0) {
            qe.registerQuestNpc(talkNpc1).addOnTalkEvent(questId);
        }
        if (talkNpc2 != 0) {
            qe.registerQuestNpc(talkNpc2).addOnTalkEvent(questId);
        }
    }

    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);

        int targetId = 0;
        if ((env.getVisibleObject() instanceof Npc)) {
            targetId = ((Npc) env.getVisibleObject()).getNpcId();
        }
        if (targetId == 0) {
            if (env.getDialogId() == 1002) {
                QuestService.startQuest(env);
                player.sendPck(new SM_DIALOG_WINDOW(0, 0));
                return true;
            }
        } else if (((targetId == talkNpc1) && (talkNpc1 != 0)) || ((targetId == talkNpc2) && (talkNpc2 != 0))) {
            if (qs != null) {
                if (env.getDialog() == QuestDialog.START_DIALOG) {
                    return sendQuestDialog(env, 1352);
                }
                if (env.getDialog() == QuestDialog.STEP_TO_1) {
                    qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
                    updateQuestStatus(env);
                    player.sendPck(new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
                    return true;
                }

                return sendQuestStartDialog(env);
            }
        } else if ((targetId == endNpcId) && (qs != null)) {
            if ((env.getDialog() == QuestDialog.START_DIALOG) && (qs.getStatus() == QuestStatus.START)) {
                return sendQuestDialog(env, 2375);
            }
            if ((env.getDialogId() == 1009) && (qs.getStatus() != QuestStatus.COMPLETE) && (qs.getStatus() != QuestStatus.NONE)) {
                removeQuestItem(env, startItemId, 1);
                qs.setQuestVar(1);
                qs.setStatus(QuestStatus.REWARD);
                updateQuestStatus(env);
                return sendQuestEndDialog(env);
            }

            return sendQuestEndDialog(env);
        }

        return false;
    }

    public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
        final Player player = env.getPlayer();
        final int id = item.getItemTemplate().getTemplateId();
        final int itemObjId = item.getObjectId();

        if (id != startItemId) {
            return HandlerResult.UNKNOWN;
        }
        PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);

        ThreadPoolManager.getInstance().schedule(new Runnable() {

            public void run() {
                PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);

                sendQuestDialog(env, 4);
            }
        }, 3000L);

        return HandlerResult.SUCCESS;
    }
}
