/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.ai2;

import com.ne.gs.ai2.event.AIEventType;
import com.ne.gs.ai2.poll.AIAnswer;
import com.ne.gs.ai2.poll.AIQuestion;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public interface AI2 {

    void onCreatureEvent(AIEventType event, Creature creature);

    void onCustomEvent(int eventId, Object... args);

    void onGeneralEvent(AIEventType event);

    /**
     * If already handled dialog return true.
     */
    boolean onDialogSelect(Player player, int dialogId, int questId, int exp);

    void think();

    boolean canThink();

    AIState getState();

    AISubState getSubState();

    String getName();

    boolean poll(AIQuestion question);

    AIAnswer ask(AIQuestion question);

    boolean isLogging();

    long getRemainigTime();

    int modifyDamage(int damage);

    int modifyOwnerDamage(int paramInt);

    int modifyReflectedDamage(int reflectedDamage);

    int modifyHealValue(int value);
}
