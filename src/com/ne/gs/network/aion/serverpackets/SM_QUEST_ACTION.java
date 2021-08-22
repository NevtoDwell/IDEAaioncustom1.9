/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.questEngine.model.QuestStatus;

/**
 * @author VladimirZ
 */
public class SM_QUEST_ACTION extends AionServerPacket {

    protected int questId;
    private int status;
    private int step;
    protected int action;
    private int timer;
    private int sharerId;
    private boolean unk;

    SM_QUEST_ACTION() {

    }

    /**
     * Accept Quest(1)
     *
     * @param questId
     * @param status
     * @param step
     */
    public SM_QUEST_ACTION(int questId, int status, int step) {
        action = 1;
        this.questId = questId;
        this.status = status;
        this.step = step;
    }

    /**
     * Quest Steps/Finish (2)
     *
     * @param questId
     * @param status
     * @param step
     */
    public SM_QUEST_ACTION(int questId, QuestStatus status, int step) {
        action = 2;
        this.questId = questId;
        this.status = status.value();
        this.step = step;
    }

    /**
     * Delete Quest(3)
     *
     * @param questId
     */
    public SM_QUEST_ACTION(int questId) {
        action = 3;
        this.questId = questId;
        status = 0;
        step = 0;
    }

    /**
     * Display Timer(4)
     *
     * @param questId
     * @param timer
     */
    public SM_QUEST_ACTION(int questId, int timer) {
        action = 4;
        this.questId = questId;
        this.timer = timer;
        step = 0;
    }

    public SM_QUEST_ACTION(int questId, int sharerId, boolean unk) {
        action = 5;
        this.questId = questId;
        this.sharerId = sharerId;
        this.unk = unk;
    }

    /**
     * Display Timer(4)
     *
     * @param questId
     */
    public SM_QUEST_ACTION(int questId, boolean fake) {
        action = 6;
        this.questId = questId;
        timer = 0;
        step = 0;
    }

    /*
     * (non-Javadoc)
     * @see com.ne.commons.network.mmocore.SendablePacket#writeImpl(com.ne.commons.network.mmocore.MMOConnection)
     */
    @Override
    protected void writeImpl(AionConnection con) {

        writeC(action);
        writeD(questId);
        switch (action) {
            case 1:
                writeC(status);// quest status goes by ENUM value
                writeC(0x0);
                writeD(step);// current quest step
                writeH(0);
                break;
            case 2:
                writeC(status);// quest status goes by ENUM value
                writeC(0x0);
                writeD(step);// current quest step
                writeH(0);
                break;
            case 3:
                writeC(status);// quest status goes by ENUM value
                writeC(0x0);
                writeD(step);// current quest step
                writeH(0);
                break;
            case 4:
                writeD(timer);// sets client timer ie 84030000 is 900 seconds/15 mins
                writeC(0x01);
                writeH(0x0);
                writeC(0x01);
            case 5:
                writeD(sharerId);
                writeD(0);
                break;
            case 6:
                writeH(0x01);// ???
                // writeD(0x0);// current quest step
                writeH(0x0);
                break;
        }
    }
}
