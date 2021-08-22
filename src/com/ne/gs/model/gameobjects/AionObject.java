/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects;

import com.google.common.base.Function;

import com.ne.commons.annotations.NotNull;
import com.ne.commons.annotations.Nullable;
import com.ne.commons.utils.*;

/**
 * This is the base class for all "in-game" objects, that player can interact with, such as: npcs, monsters, players, items.<br>
 * <br>
 * Each AionObject is uniquely identified by objectId.
 *
 * @author -Nemesiss-, SoulKeeper
 */
public abstract class AionObject {

    public static Function<AionObject, Integer> OBJECT_TO_ID_TRANSFORMER = new Function<AionObject, Integer>() {
        @Override
        public Integer apply(@Nullable AionObject input) {
            return input != null ? input.getObjectId() : null;
        }
    };

    /**
     * Unique id, for all game objects such as: items, players, monsters.
     */
    private final Integer objectId;
    private final Conditioner _conditioner = new Conditioner();
    private final Chainer _chainer = new Chainer();
    private final EventNotifier _notifier = new EventNotifier();
    private final VarMap _vars = new SyncVarMap();

    private final Implementator _implementator = new Implementator();

    public AionObject(@NotNull Integer objectId) {
        this.objectId = objectId;
    }

    /**
     * Returns unique ObjectId of AionObject
     *
     * @return Int ObjectId
     */
    @NotNull
    public Integer getObjectId() {
        return objectId;
    }

    public Conditioner getConditioner() {
        return _conditioner;
    }

    public Chainer getChainer() {
        return _chainer;
    }

    public EventNotifier getNotifier() {
        return _notifier;
    }

    public VarMap getVars() {
        return _vars;
    }

    public Implementator getImplementator() {
        return _implementator;
    }

    /**
     * Returns name of the object.<br>
     * Unique for players, common for NPCs, items, etc
     *
     * @return name of the object
     */
    public abstract String getName();

    // FIXME uncomment after tests
//    @Override
//    public int hashCode() {
//        return objectId.hashCode();
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        return obj instanceof AionObject && objectId.equals(((AionObject) obj).objectId);
//    }
}
