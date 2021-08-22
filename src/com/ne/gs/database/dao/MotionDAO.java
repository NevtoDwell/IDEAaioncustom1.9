/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.database.dao;

import com.ne.commons.database.dao.DAO;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.motion.Motion;

/**
 * @author MrPoke
 */
public abstract class MotionDAO implements DAO {

    public abstract void loadMotionList(Player player);

    public abstract boolean storeMotion(int objectId, Motion motion);

    public abstract boolean updateMotion(int objectId, Motion motion);

    public abstract boolean deleteMotion(int objectId, int motionId);

    @Override
    public String getClassName() {
        return MotionDAO.class.getName();
    }
}
