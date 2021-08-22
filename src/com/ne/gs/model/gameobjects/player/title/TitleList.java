/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.gameobjects.player.title;

import java.util.Collection;
import java.util.Map;
import gnu.trove.map.hash.THashMap;

import com.ne.gs.database.GDB;
import com.ne.gs.database.dao.PlayerTitleListDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.stats.listeners.TitleChangeListener;
import com.ne.gs.model.templates.TitleTemplate;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_TITLE_INFO;
import com.ne.gs.taskmanager.tasks.ExpireTimerTask;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author xavier, cura, xTz
 */
public class TitleList {

    private final Map<Integer, Title> titles;
    private Player owner;

    public TitleList() {
        titles = new THashMap<>(0);
        owner = null;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public boolean contains(int titleId) {
        return titles.containsKey(titleId);
    }

    public void addEntry(int titleId, int remaining) {
        TitleTemplate tt = DataManager.TITLE_DATA.getTitleTemplate(titleId);
        if (tt == null) {
            throw new IllegalArgumentException("Invalid title id " + titleId);
        }
        titles.put(titleId, new Title(tt, titleId, remaining));
    }

    public boolean addTitle(int titleId, boolean questReward, int time) {
        TitleTemplate tt = DataManager.TITLE_DATA.getTitleTemplate(titleId);
        if (tt == null) {
            throw new IllegalArgumentException("Invalid title id " + titleId);
        }
        if (owner != null) {
            if (owner.getRace() != tt.getRace() && tt.getRace() != Race.PC_ALL) {
                owner.sendMsg("This title is not available for your race.");
                return false;
            }
            Title entry = new Title(tt, titleId, time);
            if (!titles.containsKey(titleId)) {
                titles.put(titleId, entry);
                if (time != 0) {
                    ExpireTimerTask.getInstance().addTask(entry, owner);
                }
                GDB.get(PlayerTitleListDAO.class).storeTitles(owner, entry);
            } else {
                owner.sendPck(SM_SYSTEM_MESSAGE.STR_TOOLTIP_LEARNED_TITLE);
                return false;
            }
            if (questReward) {
                owner.sendPck(SM_SYSTEM_MESSAGE.STR_QUEST_GET_REWARD_TITLE(tt.getNameId()));
            } else {
                owner.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_GET_CASH_TITLE(tt.getNameId()));
            }

            owner.sendPck(new SM_TITLE_INFO(owner));
            return true;
        }
        return false;
    }

    public void setTitle(int titleId) {
        owner.sendPck(new SM_TITLE_INFO(titleId));
        PacketSendUtility.broadcastPacketAndReceive(owner, (new SM_TITLE_INFO(owner, titleId)));
        if (owner.getCommonData().getTitleId() > 0) {
            if (owner.getGameStats() != null) {
                TitleChangeListener.onTitleChange(owner.getGameStats(), owner.getCommonData().getTitleId(), false);
            }
        }
        owner.getCommonData().setTitleId(titleId);
        if (titleId > 0 && owner.getGameStats() != null) {
            TitleChangeListener.onTitleChange(owner.getGameStats(), titleId, true);
        }
    }

    public void removeTitle(int titleId) {
        if (!titles.containsKey(titleId)) {
            return;
        }
        if (owner.getCommonData().getTitleId() == titleId) {
            setTitle(-1);
        }
        titles.remove(titleId);
        owner.sendPck(new SM_TITLE_INFO(owner));
        GDB.get(PlayerTitleListDAO.class).removeTitle(owner.getObjectId(), titleId);
    }

    public int size() {
        return titles.size();
    }

    public Collection<Title> getTitles() {
        return titles.values();
    }
}
