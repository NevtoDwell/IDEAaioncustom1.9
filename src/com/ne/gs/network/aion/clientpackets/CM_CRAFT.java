/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.craft.CraftService;
import com.ne.gs.utils.MathUtil;

/**
 * @author Mr. Poke
 */
public class CM_CRAFT extends AionClientPacket {

    private int unk;
    private int targetTemplateId;
    private int recipeId;
    private int targetObjId;
    private int materialsCount;
    private int craftType;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        unk = readC();
        targetTemplateId = readD();
        recipeId = readD();
        targetObjId = readD();
        materialsCount = readH();
        craftType = readC();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (player == null || !player.isSpawned()) {
            return;
        }

        // disallow crafting in shutdown progress..
        if (player.getController().isInShutdownProgress()) {
            return;
        }

        if (unk != 129) {
            VisibleObject staticObject = player.getKnownList().getKnownObjects().get(targetObjId);
            if (staticObject == null || !MathUtil.isIn3dRange(player, staticObject, 10) || staticObject.getObjectTemplate().getTemplateId() != targetTemplateId) {
                return;
            }
        }
        CraftService.startCrafting(player, recipeId, targetObjId, craftType);
    }
}
