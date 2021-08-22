/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.drop;

import java.util.Collection;
import java.util.Set;

import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */
public interface DropCalculator {

    int dropCalculator(Set<DropItem> result, int index, float dropModifier, Race race, Collection<Player> groupMembers);
}
