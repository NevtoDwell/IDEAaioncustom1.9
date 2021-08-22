/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.iteminfo;

import java.nio.ByteBuffer;

import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.templates.item.Stigma;
import com.ne.gs.network.aion.iteminfo.ItemInfoBlob.ItemBlobType;

/**
 * This blob contains stigma info.
 *
 * @author -Nemesiss-
 */
public class StigmaInfoBlobEntry extends ItemBlobEntry {

    StigmaInfoBlobEntry() {
        super(ItemBlobType.STIGMA_INFO);
    }

    @Override
    public void writeThisBlob(ByteBuffer buf) {
        Item item = parent.item;
        Stigma stigma = item.getItemTemplate().getStigma();
        if ((stigma.getSkills().get(0)).getSkillId() == 0) {
            writeH(buf, 0);
        } else {
            writeH(buf, (stigma.getSkills().get(0)).getSkillId());
        }
        writeH(buf, 0);
        if (stigma.getSkills().size() >= 2) {
            writeH(buf, (stigma.getSkills().get(1)).getSkillId());
        } else {
            writeH(buf, 0);
        }
        writeH(buf, 0);
        writeD(buf, stigma.getShard());
        skip(buf, 192);
        writeC(buf, 1);// 1
        skip(buf, 101);
    }
}
