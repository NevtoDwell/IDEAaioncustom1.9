/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.template;

import java.util.Iterator;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.QuestTemplate;
import com.ne.gs.model.templates.quest.CollectItem;
import com.ne.gs.model.templates.quest.CollectItems;
import com.ne.gs.model.templates.quest.QuestItems;
import com.ne.gs.model.templates.quest.QuestWorkItems;
import com.ne.gs.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.handlers.models.WorkOrdersData;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.QuestService;
import com.ne.gs.services.RecipeService;
import com.ne.gs.services.item.ItemService;

/**
 * @author Mr. Poke
 */
public class WorkOrders extends QuestHandler {

    private final WorkOrdersData workOrdersData;

    /**
     */
    public WorkOrders(WorkOrdersData workOrdersData) {
        super(workOrdersData.getId());
        this.workOrdersData = workOrdersData;
    }

    @Override
    public void register() {
        Iterator<Integer> iterator = workOrdersData.getStartNpcIds().iterator();
        while (iterator.hasNext()) {
            int startNpc = iterator.next();
            qe.registerQuestNpc(startNpc).addOnQuestStart(workOrdersData.getId());
            qe.registerQuestNpc(startNpc).addOnTalkEvent(workOrdersData.getId());
        }
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        if (workOrdersData.getStartNpcIds().contains(targetId)) {
            QuestState qs = player.getQuestStateList().getQuestState(workOrdersData.getId());
            if (qs == null || qs.getStatus() == QuestStatus.NONE || qs.canRepeat()) {
                switch (env.getDialog()) {
                    case START_DIALOG: {
                        return sendQuestDialog(env, 4);
                    }
                    case ACCEPT_QUEST: {
                        if (RecipeService.validateNewRecipe(player, workOrdersData.getRecipeId()) != null) {
                            if (QuestService.startQuest(env)) {
                                if (ItemService.addQuestItems(player, workOrdersData.getGiveComponent())) {
                                    RecipeService.addRecipe(player, workOrdersData.getRecipeId(), false);
                                    player.sendPck(new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
                                }
                                return true;
                            }
                        }
                    }
                }
            } else if (qs.getStatus() == QuestStatus.START) {
                if (env.getDialog() == QuestDialog.START_DIALOG) {
                    int var = qs.getQuestVarById(0);
                    if (QuestService.collectItemCheck(env, false)) {
                        changeQuestStep(env, var, var, true); // reward
                        QuestWorkItems qwi = DataManager.QUEST_DATA.getQuestById(workOrdersData.getId()).getQuestWorkItems();
                        long count;
                        if (qwi != null) {
                            count = 0L;
                            for (QuestItems qi : qwi.getQuestWorkItem()) {
                                if (qi != null) {
                                    count = player.getInventory().getItemCountByItemId(qi.getItemId());
                                    if (count > 0L) {
                                        player.getInventory().decreaseByItemId(qi.getItemId(), count);
                                    }
                                }
                            }
                        }
                        return sendQuestDialog(env, 5);
                    }

                    return sendQuestSelectionDialog(env);
                }

            } else if (qs.getStatus() == QuestStatus.REWARD) {
                QuestTemplate template = DataManager.QUEST_DATA.getQuestById(workOrdersData.getId());
                CollectItems collectItems = template.getCollectItems();
                long count = 0L;
                for (CollectItem collectItem : collectItems.getCollectItem()) {
                    count = player.getInventory().getItemCountByItemId(collectItem.getItemId());
                    if (count > 0L) {
                        player.getInventory().decreaseByItemId(collectItem.getItemId(), count);
                    }
                }
                player.getRecipeList().deleteRecipe(player, workOrdersData.getRecipeId());
                if (env.getDialogId() == -1) {
                    QuestService.finishQuest(env, 0);
                    env.setQuestId(workOrdersData.getId());
                    return sendQuestDialog(env, 1008);
                }

                return sendQuestEndDialog(env);
            }
        }
        return false;
    }
}
