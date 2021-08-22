/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;
import java.util.List;

import com.ne.commons.utils.collections.Partitioner;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author hex1r0
 */
public class SM_RECIPE_LIST extends AionServerPacket {

    private final Collection<Integer> _recipeIds;

    public SM_RECIPE_LIST(Collection<Integer> recipeIds) {
        _recipeIds = recipeIds;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeH(_recipeIds.size());
        for (int id : _recipeIds) {
            writeD(id);
            writeC(0);
        }
    }

    public static void sendTo(final Player player) {
        Partitioner.of(player.getRecipeList().getRecipeList(), 300).foreach(new Partitioner.Func<Integer>() {
            @Override
            public boolean apply(List<Integer> list) {
                player.sendPck(new SM_RECIPE_LIST(list));
                return true;
            }
        });
    }
}
