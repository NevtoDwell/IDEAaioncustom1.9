/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.questEngine.handlers.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_SKILL_LIST;
import com.ne.gs.questEngine.handlers.QuestHandler;
import com.ne.gs.questEngine.model.QuestDialog;
import com.ne.gs.questEngine.model.QuestEnv;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.craft.CraftSkillUpdateService;

/**
 * @author Bobobear
 */
public class CraftingRewards extends QuestHandler {

    private static final Logger _log = LoggerFactory.getLogger(CraftingRewards.class);

    private final int questId;
    private final int startNpcId;
    private final int skillId;
    private final int levelReward;
    private final int questMovie;
    private final int endNpcId;

    public CraftingRewards(int questId, int startNpcId, int skillId, int levelReward, int endNpcId, int questMovie) {
        super(questId);
        this.questId = questId;
        this.startNpcId = startNpcId;
        this.skillId = skillId;
        this.levelReward = levelReward;
        if (endNpcId != 0) {
            this.endNpcId = endNpcId;
        } else {
            this.endNpcId = startNpcId;
        }
        this.questMovie = questMovie;
    }

    @Override
    public void register() {
        qe.registerQuestNpc(startNpcId).addOnQuestStart(questId);
        qe.registerQuestNpc(startNpcId).addOnTalkEvent(questId);
        if (questMovie != 0) {
            qe.registerOnMovieEndQuest(questMovie, questId);
        }
        if (endNpcId != startNpcId) {
            qe.registerQuestNpc(endNpcId).addOnTalkEvent(questId);
        }
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        QuestDialog dialog = env.getDialog();
        int targetId = env.getTargetId();

        if (player.getSkillList().getSkillEntry(skillId) == null) {
            _log.error("CraftingRewards: player " + player.getObjectId() + " dose not have skill " + skillId);
            return false;
        }

        int playerSkillLevel = player.getSkillList().getSkillEntry(skillId).getSkillLevel();

        if ((!canLearn(player)) && (playerSkillLevel != levelReward)) {
            return false;
        }
        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == startNpcId) {
                switch (dialog) {
                    case START_DIALOG: {
                        return sendQuestDialog(env, 1011);
                    }
                    default: {
                        return sendQuestStartDialog(env);
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            if (targetId == endNpcId) {
                switch (dialog) {
                    case START_DIALOG: {
                        return sendQuestDialog(env, 2375);
                    }
                    case SELECT_REWARD: {
                        qs.setQuestVar(0);
                        qs.setStatus(QuestStatus.REWARD);
                        updateQuestStatus(env);
                        if (questMovie != 0) {
                            playQuestMovie(env, questMovie);
                        } else {
                            player.getSkillList().addSkill(player, skillId, levelReward);
                        }
                        return sendQuestEndDialog(env);
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == endNpcId) {
                switch (dialog) {
                    case START_DIALOG: {
                        return sendQuestEndDialog(env);
                    }
                    default: {
                        return sendQuestEndDialog(env);
                    }
                }
            }
        }
        return false;
    }

    private boolean canLearn(Player player) {
        return levelReward == 500 ? CraftSkillUpdateService.canLearnMoreExpertCraftingSkill(player) : levelReward == 400 ? CraftSkillUpdateService.canLearnMoreMastertCraftingSkill(player) : true;
    }

    @Override
    public boolean onMovieEndEvent(QuestEnv env, int movieId) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs.getStatus() == QuestStatus.REWARD) {
            if (movieId == questMovie && canLearn(player)) {
                player.getSkillList().addSkill(player, skillId, levelReward);
                player.getRecipeList().autoLearnRecipe(player, skillId, levelReward);
                player.sendPck(new SM_SKILL_LIST(player.getSkillList().getSkillEntry(skillId), 1330064, false));
                return true;
            }
        }
        return false;
    }
}
