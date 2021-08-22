/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.player;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.world.knownlist.Visitor;

public final class PlayerVisualStateService {

    public static void hideValidate(final Player hiden) {
        hiden.getKnownList().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player observer) {
                boolean canSee = observer.canSee(hiden);
                boolean isSee = observer.isSeePlayer(hiden);

                if (canSee && !isSee) {
                    observer.getKnownList().addVisualObject(hiden);
                } else if (!canSee && isSee) {
                    observer.getKnownList().delVisualObject(hiden, false);
                }
            }
        });
    }

    public static void seeValidate(final Player search) {
        search.getKnownList().doOnAllPlayers(new Visitor<Player>() {

            @Override
            public void visit(Player hide) {
                boolean canSee = search.canSee(hide);
                boolean isSee = search.isSeePlayer(hide);

                if (canSee && !isSee) {
                    search.getKnownList().addVisualObject(hide);
                } else if (!canSee && isSee) {
                    search.getKnownList().delVisualObject(hide, false);
                }
            }
        });
    }
}
