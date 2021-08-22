/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.templates.gather.GatherableTemplate;
import com.ne.gs.model.templates.gather.Material;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author ATracer, orz
 */
public class SM_GATHER_UPDATE extends AionServerPacket {

    private final GatherableTemplate template;
    private final int action;
    private final int itemId;
    private final int success;
    private final int failure;
    private final int nameId;

    public SM_GATHER_UPDATE(GatherableTemplate template, Material material, int success, int failure, int action) {
        this.action = action;
        this.template = template;
        itemId = material.getItemid();
        this.success = success;
        this.failure = failure;
        nameId = material.getNameid();
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeH(template.getSkillLevel());
        writeC(action);
        writeD(itemId);

        switch (action) {
            case 0: {
                writeD(template.getSuccessAdj());
                writeD(template.getFailureAdj());
                writeD(0);
                writeD(1200); // timer??
                writeD(1330011); // ??text??skill??
                writeH(0x24); // 0x24
                writeD(nameId);
                writeH(0); // 0x24
                break;
            }
            case 1: {
                writeD(success);
                writeD(failure);
                writeD(700); // unk timer??
                writeD(1200); // unk timer??
                writeD(0); // unk timer??writeD(700);
                writeH(0);
                break;
            }
            case 2: {
                writeD(template.getSuccessAdj());
                writeD(failure);
                writeD(700);// unk timer??
                writeD(1200); // unk timer??
                writeD(0); // unk timer??writeD(700);
                writeH(0);
                break;
            }
            case 5: // you have stopped gathering
            {
                writeD(0);
                writeD(0);
                writeD(700);// unk timer??
                writeD(1200); // unk timer??
                writeD(1330080); // unk timer??writeD(700);
                writeH(0);
                break;
            }
            case 6: {
                writeD(template.getSuccessAdj());
                writeD(failure);
                writeD(700); // unk timer??
                writeD(1200); // unk timer??
                writeD(0); // unk timer??writeD(700);
                writeH(0);
                break;
            }
            case 7: {
                writeD(success);
                writeD(template.getFailureAdj());
                writeD(0);
                writeD(1200); // timer??
                writeD(1330079); // ??text??skill??
                writeH(0x24); // 0x24
                writeD(nameId);
                writeH(0); // 0x24
                break;
            }
        }
    }

}
