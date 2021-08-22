/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Mr. Poke
 */
public class SM_CRAFT_UPDATE extends AionServerPacket {

    private final int skillId;
    private final int itemId;
    private final int action;
    private final int success;
    private final int failure;
    private final int nameId;
    private final int executionSpeed;

    /**
     * @param skillId
     * @param item
     * @param success
     * @param failure
     * @param action
     */
    public SM_CRAFT_UPDATE(int skillId, ItemTemplate item, int success, int failure, int action) {
        this.action = action;
        this.skillId = skillId;
        itemId = item.getTemplateId();
        this.success = success;
        this.failure = failure;
        nameId = item.getNameId();

        if (skillId == 40009) {
            executionSpeed = 1500;
        } else {
            executionSpeed = 700;
        }
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeH(skillId);
        writeC(action);
        writeD(itemId);

        switch (action) {
            case 0: // init
            {
                writeD(success);
                writeD(failure);
                writeD(0);
                writeD(1200); // timer??
                writeD(1330048);
                writeH(0x24); // 0x24
                writeD(nameId);
                writeH(0);
                break;
            }
            case 1: // update
            case 2: // crit
            {
                writeD(success);
                writeD(failure);
                writeD(executionSpeed);
                writeD(1200);
                writeD(0);
                writeH(0);
                break;
            }
            case 3: // crit
            {
                writeD(success);
                writeD(failure);
                writeD(0);
                writeD(1200);
                writeD(1330048); // message
                writeH(0x24);
                writeD(nameId);
                writeH(0);
                break;
            }
            case 4: // cancel
            {
                writeD(success);
                writeD(failure);
                writeD(0);
                writeD(0);
                writeD(1330051);
                writeH(0);
                break;
            }
            case 5: // sucess
            {
                writeD(success);
                writeD(failure);
                writeD(0);
                writeD(0);
                writeD(1300788);
                writeH(0x24);
                writeD(nameId);
                writeH(0);
                break;
            }
            case 6: // failed
            {
                writeD(success);
                writeD(failure);
                writeD(0);
                writeD(0);
                writeD(1330050);
                writeH(0x24);
                writeD(nameId);
                writeH(0);
                break;
            }
            case 7: {
                writeD(success);
                writeD(failure);
                writeD(0);
                writeD(1200);
                writeD(1330050);
                writeH(0x24);
                writeD(nameId);
                writeH(0);
                break;
            }
        }
    }
}
