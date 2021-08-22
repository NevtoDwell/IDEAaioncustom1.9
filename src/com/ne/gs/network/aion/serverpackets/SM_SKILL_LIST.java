/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.skill.PlayerSkillEntry;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * In this packet Server is sending Skill Info?
 *
 * @author modified by ATracer,MrPoke
 */
public class SM_SKILL_LIST extends AionServerPacket {

    private final List<PlayerSkillEntry> skillList;
    private final int messageId;
    private int skillNameId;
    private String skillLvl;
    public static final int YOU_LEARNED_SKILL = 1300050;
    boolean isNew = false;

    /**
     * This constructor is used on player entering the world Constructs new <tt>SM_SKILL_LIST </tt> packet
     */
    public SM_SKILL_LIST(Player player) {
        skillList = player.getSkillList().getAllSkills();
        messageId = 0;
    }

    public SM_SKILL_LIST(PlayerSkillEntry skillListEntry, int messageId, boolean isNew) {
        skillList = Collections.singletonList(skillListEntry);
        this.messageId = messageId;
        skillNameId = DataManager.SKILL_DATA.getSkillTemplate(skillListEntry.getSkillId()).getNameId();
        skillLvl = String.valueOf(skillListEntry.getSkillLevel());
        this.isNew = isNew;
    }

    @Override
    protected void writeImpl(AionConnection con) {

        int size = skillList.size();
        writeH(size); // skills list size

        if (size > 0) {
            for (PlayerSkillEntry entry : skillList) {
                writeH(entry.getSkillId());// id
                writeH(entry.getSkillLevel());// lvl
                writeC(0x00);
                writeC(entry.getExtraLvl());
                if (isNew) {
                    writeD((int) (System.currentTimeMillis() / 1000)); // Learned date NCSoft......
                } else {
                    writeD(0);
                }
                writeC(entry.isStigma() ? 1 : 0); // stigma
            }
        }
        writeD(messageId);
        if (messageId != 0) {
            writeH(0x24); // unk
            writeD(skillNameId);
            writeH(0x00);
            writeS(skillLvl);
        }
    }
}
