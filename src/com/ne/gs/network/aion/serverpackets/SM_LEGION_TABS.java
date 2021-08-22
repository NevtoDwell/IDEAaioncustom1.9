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

import com.ne.gs.model.team.legion.LegionHistory;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Simple, KID
 */
public class SM_LEGION_TABS extends AionServerPacket {

    private final int page;
    private final Collection<LegionHistory> legionHistory;
    private final int tabId;

    public SM_LEGION_TABS(Collection<LegionHistory> legionHistory, int tabId) {
        this.legionHistory = legionHistory;
        page = 0;
        this.tabId = tabId;
    }

    public SM_LEGION_TABS(Collection<LegionHistory> legionHistory, int page, int tabId) {
        this.legionHistory = legionHistory;
        this.page = page;
        this.tabId = tabId;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        /**
         * If history size is less than page*8 return
         */
        int size = legionHistory.size();
        if (size < page * 8) {
            return;
        }

        // TODO: Formula's could use a refactor
        int hisSize = size - page * 8;
        if (size > (page + 1) * 8) {
            hisSize = 8;
        }
        writeD(size);
        writeD(page); // current page
        writeD(hisSize);

        int i = 0;
        for (LegionHistory history : legionHistory) {
            if (i >= (page * 8) && i <= (8 + (page * 8))) {
                writeD((int) (history.getTime().getTime() / 1000));
                writeC(history.getLegionHistoryType().getHistoryId());
                writeC(0);
                writeS(history.getName(), 64);
                writeH(0);
                writeS(history.getDescription(), 64);
                writeD(0); // unk
            }
            i++;
            if (i >= (8 + (page * 8))) {
                break;
            }
        }
        writeC(tabId);
        writeC(0);
    }
}
