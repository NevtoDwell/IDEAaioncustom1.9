/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player.emotion;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ne.gs.database.GDB;
import com.ne.gs.configs.main.MembershipConfig;
import com.ne.gs.database.dao.PlayerEmotionListDAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION_LIST;
import com.ne.gs.taskmanager.tasks.ExpireTimerTask;

/**
 * @author MrPoke
 */
public class EmotionList {

    private Map<Integer, Emotion> emotions;
    private final Player owner;

    /**
     * @param owner
     */
    public EmotionList(Player owner) {
        this.owner = owner;
    }

    public void add(int emotionId, int dispearTime, boolean isNew) {
        if (emotions == null) {
            emotions = new HashMap<>();
        }
        Emotion emotion = new Emotion(emotionId, dispearTime);
        emotions.put(emotionId, emotion);

        if (isNew) {
            if (emotion.getExpireTime() != 0) {
                ExpireTimerTask.getInstance().addTask(emotion, owner);
            }
            GDB.get(PlayerEmotionListDAO.class).insertEmotion(owner, emotion);
            owner.sendPck(new SM_EMOTION_LIST((byte) 1, Collections.singletonList(emotion)));
        }
    }

    public void remove(int emotionId) {
        emotions.remove(emotionId);
        GDB.get(PlayerEmotionListDAO.class).deleteEmotion(owner.getObjectId(), emotionId);
        owner.sendPck(new SM_EMOTION_LIST((byte) 0, getEmotions()));
    }

    public boolean contains(int emotionId) {
        if (emotions == null) {
            return false;
        }
        return emotions.containsKey(emotionId);
    }

    public boolean canUse(int emotionId) {
        return emotionId < 64 || emotionId > 129 || (emotions != null && emotions.containsKey(emotionId))
            || owner.havePermission(MembershipConfig.EMOTIONS_ALL);
    }

    public Collection<Emotion> getEmotions() {
        if (emotions == null) {
            return Collections.emptyList();
        }
        return emotions.values();
    }
}
